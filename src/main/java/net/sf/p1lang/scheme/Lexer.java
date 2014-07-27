/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.io.Reader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

// TODO: nested comments
// TODO: the numbers are parsed but not stored and returned

public final class Lexer
{

public static enum Token
{
  EOF("<EOF>"),

  IDENT("identifier"),
  BOOL("#t or #f"),
  NUMBER("number"),
  CHAR("character"),
  STR("string"),
  LPAR("("), RPAR(")"),         // ( )
  LSQUARE("["), RSQUARE("]"),   // [ ]
  HASH_LPAR("#("),              // #(
  APOSTR("'"), ACCENT("`"),     // ' `
  COMMA(","),                   // ,
  COMMA_AT(",@"),               // ,@
  DOT("."),                     // .
  HASH_APOSTR("#'"),            // #'
  HASH_ACCENT("#`"),            // #`
  HASH_COMMA("#,"),             // #,
  HASH_COMMA_AT("#,@"),         // #,@
  DATUM_COMMENT("#;"),          // #;

  NESTED_COMMENT_START("#|"), // internal use only !
  NESTED_COMMENT_END("|#"); // internal use only !

  public final String repr;

  Token ( final String repr )
  {
    this.repr = repr;
  }
}

public final SymbolMap m_symbolMap;
private IErrorReporter m_errors;
private Reader m_in;

private boolean m_inNestedComment;
private int m_curChar;
/** Coordinates of the last character returned by {@link #nextChar()} */
private final SourceCoords m_coords = new SourceCoords();

/**
 * Used internally from {@link #nextChar()} . Saved characters haven't been processed by
 * {@link #nextChar()}.
 */
private int m_savedChar = -1;

/**
 * Used from higher level to "undo" the last character returned by {@link #nextChar()} .
 *
 * @see #ungetChar(int)
 */
private int m_ungetChar = -1;

public boolean m_valueBool;
public char m_valueChar;
public Symbol m_valueIdent;
public String m_valueString;
public SchemeNumber m_valueNumber;

/** The last token */
public Token m_curToken;
/** The coordinates of {@link #m_curToken} */
public final SourceCoords m_tokCoords = new SourceCoords();


private static final int HT = '\u0009';
private static final int LF = '\n';
private static final int CR = '\r';
private static final int VT = '\u000B';
private static final int FF = '\u000C';
private static final int U_NEXT_LINE = '\u0085';
private static final int U_LINE_SEP = '\u2028';
private static final int U_PARA_SEP = '\u2029';

public Lexer ( Reader in, String fileName, SymbolMap symbolMap, IErrorReporter errors )
{
  m_in = in;
  m_coords.fileName = fileName;
  m_coords.line = 1;
  m_symbolMap = symbolMap;
  m_errors = errors;
  nextChar();
}

public final IErrorReporter getErrorReporter ()
{
  return m_errors;
}

private final void error ( Throwable cause, String message, Object ... args )
{
  m_errors.error( m_coords, cause, message, args );
}

private static StringBuilder escapeStringChar ( StringBuilder buf, char ch )
{
  // TODO: we shouldn't escape printable Unicode characters, but I am not sure how
  if (ch >= 32 && ch <= 127)
    return buf.append( ch );
  switch (ch)
  {
  case 0x07: return buf.append( "\\a" );
  case '\b': return buf.append( "\\b" );
  case '\t': return buf.append( "\\t" );
  case '\n': return buf.append( "\\n" );
  case 0x0B: return buf.append( "\\v" );
  case '\f': return buf.append( "\\f" );
  case '\r': return buf.append( "\\r" );
  case '\"': return buf.append( "\\\"" );
  case '\\': return buf.append( "\\\\" );
  default:
    return buf.append( String.format( "\\x%04x;", (int)ch ) );
  }
}

/**
 * Generate a properly escaped string representation of a code point sequence.
 * @return
 */
public static String escapeToString ( String str )
{
  StringBuilder res = new StringBuilder(str.length()+str.length()/4);
  for ( int i = 0; i < str.length(); ++i )
    escapeStringChar( res, str.charAt(i) );
  return res.toString();
}

/**
 * Generate a properly escaped string representation of a code point sequence.
 * @param codePoints
 * @param offset
 * @param count
 * @return
 */
private static String escapeToString ( int [] codePoints, int offset, int count )
{
  char chars[] = new char[4];
  StringBuilder res = new StringBuilder(count+count/4);
  for ( int end = offset+count; offset < end; ++offset )
  {
    int cp = codePoints[offset];
    if (cp >= 0 && cp <= Character.MAX_CODE_POINT)
    {
      int cplen = Character.toChars( codePoints[offset], chars, 0 );
      for ( int i = 0; i < cplen; ++i )
        escapeStringChar( res, chars[i] );
    }
    else
      res.append( "\\????;" );
  }
  return res.toString();
}

/**
 * Unget the character in {@link #m_curChar} and replace it with another one. The next
 * {@link #nextChar()} will return the value that used to be in {@link #m_curChar}. It function
 * <b>MUST</b> not be used to unget a line feed!
 *
 * <p>Source coordinates of the "ungotten" character are determined by assuming that it is the
 * previous character, before {@link #m_curChar}, on the current line. They are the current column - 1.
 * (That is why line feed must not be ungotten).
 *
 * <p>In general this function is needed for convenience, to enable an extra character lookahead in
 * some rare cases. We need just one char. In theory it could be avoided with the cost of
 * significantly more code, expanding the DFA. The pattern of usage is:
 * <pre>
 *   if (m_curChar = '1')
 *   {
 *     nextChar();
 *     if (m_curChar == '2'))
 *     {
 *       ungetChar( '1' );
 *       scan(); // scan() will first see '1' (in m_curChar) and only afterwards will obtain '2'
 *     }
 *   }
 * </pre>
 *
 *
 * @param ch the char to unget.
 */
private final void ungetChar ( int ch )
{
  assert( m_ungetChar < 0 );

  m_ungetChar = m_curChar;
  m_curChar = ch;
  // ch is the previous character on the same line, so we just go one column back.
  --m_coords.column;
}

/**
 * Read and return the next character. If we are at EOF or on any I/O error returns and keep
 * returning -1. {@link #m_coords} are updated with the coordinates of the
 * returned char.
 *
 * <p>All line end characters and combination are translated to '\n' ({@link #LF}).
 *
 * <p>This routine handles UTF-16 surrogate pairs.
 *
 * @return the next character, or -1 on EOF or any I/O error
 */
private final int nextChar ()
{
  int ch;

  if (m_curChar < 0) // if at EOF or error keep returning EOF
    return -1;

  // if there was a character saved by the caller, return it
  if (m_ungetChar >= 0)
  {
    m_curChar = m_ungetChar;
    m_ungetChar = -1;

    // we decremented the column in {@link #ungetChar(int)} to refer to the previous character
    // now we restore it
    ++m_coords.column;

    return m_curChar;
  }

  // if we have saved a character ourselves, process it
  if (m_savedChar >= 0)
  {
    ch = m_savedChar;
    m_savedChar = -1;
  }
  else
    ch = readCodePoint();

  // Translate CR, CR LF, CR U_NEXT_LINE, U_NEXT_LINE, U_LINE_SEP into LF and update the line number
  switch (ch)
  {
  case CR:
    {
      // Must peek into the next char. If it is LF or U_NEXT_LINE, collapse it
      int next = readCodePoint();
      if (!(next == LF || next == U_NEXT_LINE))
      {
        // Nope. Just a single CR. We must unput the next character.
        m_savedChar = next;
      }
    }
    // FALL
  case U_NEXT_LINE:
  case U_LINE_SEP:
    ch = LF;
    // FALL
  case LF:
    ++m_coords.line;      // new lines reset the column and increment the line
    m_coords.column = 0;
    break;

  default:         // all other characters increment the column
    ++m_coords.column;
    break;
  }

  return m_curChar = ch;
}

/**
 * Read s code point, which could consist of two characters
 * @return the code point, or -1 on EOF or I/O error
 */
private final int readCodePoint ()
{
  int h;
  try
  {
    h = m_in.read();
    if (h < 0) // EOF ?
      h = -1;
    else if (Character.isHighSurrogate((char)h)) // Handle UTF-16
    {
      int l = m_in.read();
      if (Character.isLowSurrogate((char)l))
        h = Character.toCodePoint( (char)h, (char)l );
      else
      {
        if (l < 0) // EOF  in the middle of surrogate pair ?
        {
          error( null, "EOF in the middle of a surrogate pair" );
          h = -1;
        }
        else
        {
          error( null, "Invalid low-surrogate code unit 0x%04x", l );
          h = ' '; // Simple error recovery
        }
      }
    }
    else
      h = validateCodePoint( h );
  }
  catch (IOException e)
  {
    error( e, "I/O Error" );
    h = -1;
  }
  return h;
}

private final int validateUnicodeChar ( int ch )
{
  if (ch < 0 ||
      ch >= Character.MIN_SURROGATE && ch <= Character.MAX_SURROGATE || // Is it any surrogate ?
      ch >= Character.MIN_SUPPLEMENTARY_CODE_POINT)
  {
    error( null, "Invalid Unicode character 0x%04x", ch );
    ch = ' '; // Simple error recovery
  }
  return ch;
}

private final int validateCodePoint ( int ch )
{
  if (ch < 0 ||
      ch >= Character.MIN_SURROGATE && ch <= Character.MAX_SURROGATE || // Is it any surrogate ?
      ch > Character.MAX_CODE_POINT)
  {
    error( null, "Invalid Unicode character 0x%04x", ch );
    ch = ' '; // Simple error recovery
  }
  return ch;
}

private static final boolean isDelimiter ( int ch )
{
  switch (ch)
  {
  case '(': case ')': case '[': case ']': case '"': case ';': case '#':
  case -1: // EOF is also a delimiter
    return true;
  default:
    return Character.isWhitespace( ch );
  }
}

private static final boolean isBaseDigit ( int base, int ch )
{
  switch (base)
  {
  case 2: return ch == '0' || ch == '1';
  case 8: return ch >= '0' && ch <= '7';
  case 10: return ch >= '0' && ch <= '9';
  case 16: ch |= 32; return ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'f';
  }
  assert false : "Unsupported number base "+base;
  return false;
}

/**
 * Convert a digit in bases 2,8,10,16 to int. Note that we don't have to specify the base.
 * @param ch
 * @return
 */
private static final int baseDigitToInt ( int ch )
{
  ch |= 32;
  return ch <= '9' ? ch - '0'  : ch - ('a' - 10);
}

/**
 * Save the coordinates of the last character into the token coordinates.
 */
private final void saveCoords ()
{
  m_tokCoords.assign( m_coords );
}

public final Token nextToken ()
{
  return m_curToken = _nextToken();
}

private final Token _nextToken ()
{
  for(;;)
  {
    Token res;
    saveCoords(); // save the coords at the start of the token
    switch (m_curChar)
    {
    case -1: return Token.EOF;

    case '(': nextChar(); return Token.LPAR;
    case ')': nextChar(); return Token.RPAR;
    case '[': nextChar(); return Token.LSQUARE;
    case ']': nextChar(); return Token.RSQUARE;
    case '\'': nextChar(); return Token.APOSTR;
    case '`': nextChar(); return Token.ACCENT;

    // nested commend end handling.
    case '|':
      nextChar(); // consume the '|'
      if (!m_inNestedComment || m_curChar != '#')
        error( null, "\"|\" cannot start a lexeme" );
      else
      {
        nextChar(); // consume the '#'
        return Token.NESTED_COMMENT_END;
      }
      break;

    case ',':
      nextChar();
      if (m_curChar == '@')
      {
        nextChar();
        return Token.COMMA_AT;
      }
      else
        return Token.COMMA;

    // <string>
    case '"':
      nextChar();
      if ( (res = scanString()) != null)
        return res;
      break;

    // <comment>
    case ';':
      // Skip until we reach line end, paragraph separator or EOF
      do
        nextChar();
      while (m_curChar >= 0 && m_curChar != LF && m_curChar != U_PARA_SEP);
      break;

    // <digit>
    case '0': case '1': case '2': case '3': case '4':
    case '5': case '6': case '7': case '8': case '9':
      if ( (res = scanNumber()) != null)
        return res;
      break;

    case '#':
      nextChar();
      switch (m_curChar)
      {
/*
      case '%': // Extension: system identifiers
        nextChar();
        if ( (res = scanRestIdentifier( '#', '%' )) != null)
          return res;
        break;
*/
      case 'i': case 'I': case 'e': case 'E': // number exactness prefix
      case 'b': case 'B': // binary number
      case 'o': case 'O': // octal
      case 'd': case 'D': // decimal
      case 'x': case 'X': // hex
        {
          ungetChar( '#' );
          if ( (res = scanNumber()) != null)
            return res;
        }
        break;

      case '|': // #| Nested comment
        nextChar();
        if (!m_inNestedComment)
          scanNestedComment();
        else
          return Token.NESTED_COMMENT_START;
        break;

/* TODO:
    case '!': // #!r6rs
      ...
*/

      case ';': // #; datum comment
        nextChar();
        return Token.DATUM_COMMENT;


      case '(':  /* #( */ nextChar(); return Token.HASH_LPAR;
      case '\'': /* #' */ nextChar(); return Token.HASH_APOSTR;
      case '`':  /* #` */ nextChar(); return Token.HASH_ACCENT;
      case ',':  /* #, #,@ */
        nextChar();
        if (m_curChar == '@') // #,@
        {
          nextChar();
          return Token.HASH_COMMA_AT;
        }
        else
          return Token.HASH_COMMA;

      case 't': case 'T': // #t #T
        nextChar();
        if (!isDelimiter(m_curChar))
          error( null, "Bad #x form" );
        m_valueBool = true;
        return Token.BOOL;
      case 'f': case 'F': // #f #F
        nextChar();
        if (!isDelimiter(m_curChar))
          error( null, "Bad #x form" );
        m_valueBool = false;
        return Token.BOOL;

      case '\\': // #\ character
        nextChar();
        if ( (res = scanCharacter()) != null)
          return res;
        break;

      default:
        error( null, "Illegal lexeme \"%s\"", escapeToString(new int[]{ '#', m_curChar }, 0, 2 ));
        nextChar();
        break;
      }
      break;

    // <identifier>
    case '!': case '$': case '%': case '&': case '*': case '/': case ':': case '<':
    case '=': case '>': case '?': case '^': case '_': case '~':
      {
        int saveCh = m_curChar;
        nextChar();
        if ( (res = scanRestIdentifier(saveCh)) != null)
          return res;
      }
      break;

    // <peculiar identifier> "+"
    case '+':
      nextChar();
      if (isDelimiter(m_curChar))
      {
        if ( (res = identifier( "+" )) != null)
          return res;
      }
      else
      {
        ungetChar('+');
        if ( (res = scanNumber()) != null)
          return res;
      }
      break;

    // <peculiar identifier> "-" "->"
    case '-':
      nextChar();
      if (isDelimiter(m_curChar)) // just '-' ?
      {
        if ( (res = identifier("-")) != null)
          return res;
      }
      else if (m_curChar == '>') // "->" ?
      {
        nextChar();
        if ( (res = scanRestIdentifier('-', '>')) != null)
          return res;
      }
      else
      {
        ungetChar('-');
        if ( (res = scanNumber()) != null)
          return res;
      }
      break;

    // <peculiar identifier> "..."
    case '.':
      nextChar();
      if (isDelimiter(m_curChar))
        return Token.DOT;
      else if (m_curChar >= '0' && m_curChar <= '9')
      {
        ungetChar( '.' );
        if ( (res = scanNumber()) != null)
          return res;
      }
      else
      {
        if ( (res = scanRestIdentifier('.')) != null)
          return res;
      }
      break;

    // <inline hex escape>
    case '\\':
      nextChar();
      if (m_curChar == 'x') // \x -> identifier starting with an inline hex escape
      {
        nextChar();
        if ( (res = scanRestIdentifier( scanInlineHexEscape() )) != null)
          return res;
      }
      else
        error( null, "\"\\\" cannot start a lexeme" );
      break;

    default:
      if (Character.isWhitespace(m_curChar))
      {
        // Consume all whitespace here for efficiency
        do
          nextChar();
        while (Character.isWhitespace(m_curChar));
      }
      else if (Character.isLetter(m_curChar))
      {
        int saveCh = m_curChar;
        nextChar();
        if ( (res = scanRestIdentifier(saveCh)) != null)
          return res;
      }
      else
      {
        error( null, "\"%s\" cannot start a lexeme", escapeToString( new int[]{ m_curChar }, 0, 1 ) );
        nextChar();
      }
      break;
    }
  }
}

private final SourceCoords m_nestedCommentStart = new SourceCoords();

/**
 * Scan a nested multiline comment. In theory it is defined in
 * <a href="http://srfi.schemers.org/srfi-30/">SRFI-30</a>. However, as defined there, a comment
 * can be terminated within a string. I think that is very unintuitive, so in this implementation
 * the comment terminator is treated as a token within a stream of tokens. Thus we won't be looking
 * within strings.
 *
 * <p>It works by resetting the error reporter to one that ignores all errors and calling the
 * scanner recursively. It however does not rely on recursion to handle the nested comments themselves
 */
private final void scanNestedComment ()
{
  m_nestedCommentStart.assign( m_tokCoords ); // remember where the comment started

  assert( !m_inNestedComment );

  IErrorReporter saveReporter = m_errors;
  m_errors = s_nullReporter;
  m_inNestedComment = true;
  try
  {
    int level = 1;
    loop: for(;;)
    {
      switch (nextToken())
      {
      case NESTED_COMMENT_START:
        ++level;
        break;
      case NESTED_COMMENT_END:
        if (--level == 0)
          break loop;
        break;
      case EOF:
        break loop; // we must report the error after we have restored the error reporter
      default:
        break;
      }
    }

  }
  finally
  {
    m_errors = saveReporter;
    m_inNestedComment = false;
  }

  if (m_curToken == Token.EOF)
    error( null, "EOF in comment started on line "+ m_nestedCommentStart.line );
}

/**
 * A "null" error reporter used when scanning inside comments.
 */
private static final IErrorReporter s_nullReporter = new IErrorReporter()
{
  public void error ( final ISourceCoords coords, final Throwable cause, final String message,
                      final Object... args ) throws TooManyErrors
  {}

  public List<ErrorInfo> getErrorList ()
  {
    return null;
  }

  public int getErrorCount ()
  {
    return 0;
  }
};

/**
 * It is invoked with current character one of "0123456789.#+-"
 *
 * @return the token
 */
private final Token scanNumber ()
{
  int base = 0;
  int exact = 0; // -1:inexact, +1: exact

  // Handle the prefix
  //
  while (m_curChar == '#')
  {
    nextChar();
    switch (m_curChar)
    {
    case 'i': case 'I':
      if (exact != 0) error( null, "More than one exactness #prefix in a number" ); exact = -1; break;
    case 'e': case 'E':
      if (exact != 0) error( null, "More than one exactness #prefix in a number" ); exact = +1; break;
    case 'b': case 'B':
      if (base != 0) error( null, "More than one base #prefix in a number" ); base = 2; break;
    case 'o': case 'O':
      if (base != 0) error( null, "More than one base #prefix in a number" ); base = 8; break;
    case 'd': case 'D':
      if (base != 0) error( null, "More than one base #prefix in a number" ); base = 10; break;
    case 'x': case 'X':
      if (base != 0) error( null, "More than one base #prefix in a number" ); base = 16; break;

    default:
      error( null, "Invalid number prefix" ); break;
    }
    nextChar();
  }

  if (base == 0) // assume base 10 if none specified
    base = 10;

  SchemeNumber res;
  if ( (res = scanComplex( base, exact )) == null)
    return null;

  if (!isDelimiter( m_curChar ))
    error( null, "No delimiter after number" );

  m_valueNumber = res;
  return Token.NUMBER;
}

private final SchemeNumber scanComplex ( int base, int exact )
{
  int saveSignChar = 0;

  if (m_curChar == '+' || m_curChar == '-')
  {
    saveSignChar = m_curChar;
    nextChar();
    if ( (m_curChar | 32) == 'i')
    {
      // +i -i
      if (exact >= 0)
        return saveSignChar == '+' ? SchemeComplex.EXACT_I : SchemeComplex.EXACT_MINUS_I;
      else
        return saveSignChar == '+' ? SchemeComplex.INEXACT_I : SchemeComplex.INEXACT_MINUS_I;
    }
    else
      ungetChar( saveSignChar );
  }

  SchemeNumber a;
  if ( (a = scanReal( base, exact )) == null)
    return null;

  SchemeNumber b;
  switch (m_curChar)
  {
  case '@':
    nextChar();
    if ( (b = scanReal( base, exact )) == null)
      return null;
    error( null, "@ not implemented" ); // FIXME: implement it
    return null; // a @ b

  case 'i':
    if (saveSignChar == 0)
      error( null, "Invalid complex number format" );
    nextChar();
    if (saveSignChar < 0)
      a = a.neg();
    if (exact == 0) // if exactness was unspecified, infer it from the number
      exact = a.isExact() ? +1 : -1;
    return SchemeComplex.make( exact > 0 ? SchemeFixInt.ZERO : SchemeReal.ZERO, a ); // +ai, -ai

  case '+':
  case '-':
    saveSignChar = m_curChar;
    nextChar();
    if (m_curChar == 'i')
    {
      nextChar();

      if (exact == 0) // if exactness was unspecified, infer it from the number
        exact = a.isExact() ? +1 : -1;

      // a+i, a-i
      return exact > 0 ?
        SchemeComplex.make( a, saveSignChar == '+' ? SchemeFixInt.ONE : SchemeFixInt.MINUS_ONE ) :
        SchemeComplex.make( a, saveSignChar == '+' ? SchemeReal.ONE : SchemeReal.MINUS_ONE );
    }
    else
    {
      ungetChar( saveSignChar );
      if ( (b = scanReal( base, exact )) == null)
        return null;

      if (m_curChar == 'i')
        nextChar();
      else
        error( null, "Invalid complex number format" );

      // a+bi, a-bi
      if (saveSignChar < 0)
        b = b.neg();
      return SchemeComplex.make( a, b );
    }

  default:
    return a;
  }
}

private final SchemeNumber scanReal ( int base, int exact )
{
  int sign = 1;

  // Handle the sign
  //
  boolean sawSign = false;
  if (m_curChar == '-')
  {
    sign = -1;
    sawSign = true;
    nextChar();
  }
  else if (m_curChar == '+')
  {
    sawSign = true;
    nextChar();
  }

  // Check for +/- inf.0/nan.0
  //
  if (sawSign && ((m_curChar | 32) == 'i' || (m_curChar | 32) == 'n'))
  {
    // Parse the inf.0/nan.0 as regular identifiers
    int saveCh = m_curChar;
    nextChar();
    if (scanRestIdentifier( saveCh ) != Token.IDENT) // This shouldn't really happen
    {
      error( null, "Unsupported number syntax" );
      return null;
    }

    // I prefer not to use string operations for this comparison
    String t = m_valueIdent.name;
    if (t != null && t.length() == 5 && t.charAt(3) == '.' && t.charAt(4) == '0')
    {
      if ((t.charAt(0) | 32) == 'i' && (t.charAt(1) | 32) == 'n' && (t.charAt(2) | 32) == 'f')
      {
        if (exact > 0)
          return sign > 0 ? SchemeRational.POS_INF : SchemeRational.NEG_INF; // inf.0 !!
        else
          return sign > 0 ? SchemeReal.POS_INF : SchemeReal.NEG_INF; // inf.0 !!
      }
      else if ((t.charAt(0) | 32) == 'n' && (t.charAt(1) | 32) == 'a' && (t.charAt(2) | 32) == 'n')
      {
        if (exact > 0)
          return SchemeRational.NAN;  // nan.0 !!
        else
          return sign > 0 ? SchemeReal.POS_NAN : SchemeReal.NEG_NAN; // nan.0 !!
      }
    }

    error( null, "Unsupported number syntax \"%s\"", t );
    return null;
  }

  return scanUReal( base, exact, sign );
}

private final SchemeNumber scanUReal ( int base, int exact, int sign )
{
  if (m_curChar == '.')
    return scanDecimal( base, exact, sign, null );

  SchemeInteger whole;
  if ( (whole = scanUInteger( base )) == null)
    return null;

  // Check of this is a decimal number (999[.ef...])
  switch (m_curChar)
  {
  case '.':
  case 'e': case 'E': case 's': case 'S': case 'f': case 'F':
  case 'd': case 'D': case 'l': case 'L':
  case '|': // <mantissa width>
    return scanDecimal( base, exact, sign, whole );

  case '/':
    {
      nextChar();

      // Scan the denominator
      SchemeInteger denom;
      if ( (denom = scanUInteger( base )) == null)
        return null;

      if (sign < 0) whole = whole.neg();
      // Return an exact or inexact result
      if (exact >= 0)
        return SchemeRational.make(whole, denom);
      else
        return whole.toInexact().div(denom.toInexact());
    }
  }

  if (sign < 0) whole = whole.neg();
  // This is a plain old integer. However it may be inexact
  return exact >= 0 ? whole : whole.toInexact();
}

/**
 *
 * @param base
 * @param exact
 * @param sign
 * @param whole
 * @return
 */
private final SchemeNumber scanDecimal ( int base, int exact, int sign, SchemeInteger whole )
{
  int exponentSign = 1;
  int exponent;
  int mantissaWidth;
  SchemeInteger fract;

  if (base != 10)
  {
    error( null, "Real numbers must use base 10" );
    base = 10;
  }

  // The ^ signifies where we are:
  //
  // ^ '.' <digit 10>+ <suffix> <mantissa width>                // whole == null
  //
  // <uinteger 10> ^ <suffix> <mantissa width>                  // whole != null
  // <digit 10>+ ^ '.' <digit 10>* <suffix> <mantissa width>    // whole != null

  if (whole == null)
  {
    // ^ '.' <digit 10>+ <suffix> <mantissa width>                // whole == null
    assert( m_curChar == '.' );
    nextChar();

    if ( (fract = scanUInteger( 10 )) == null)
      return null;
  }
  else
  {
    if (m_curChar == '.')
    {
      // <digit 10>+ ^ '.' <digit 10>* <suffix> <mantissa width>    // whole != null
      nextChar();
      if (isBaseDigit( base, m_curChar ))
      {
        if ( (fract = scanUInteger( 10 )) == null)
          return null;
      }
      else
        fract = null;
    }
    else
    {
      // <uinteger 10> ^ <suffix> <mantissa width>                  // whole != null
      fract = null;
    }
  }

  // Process the suffix. Note that in Java we  support just one precision IEEE double
  switch (m_curChar)
  {
  case 'e': case 'E': // default precision
  case 's': case 'S': // short
  case 'f': case 'F': // single
  case 'd': case 'D': // double
  case 'l': case 'L': // long
    nextChar();
    if (m_curChar == '+')
      nextChar();
    else if (m_curChar == '-')
    {
      exponentSign = -1;
      nextChar();
    }
    if ( (exponent = scanSmallNonNegInt10()) < 0)
      return null;
    break;

  default:
    exponent = 0;
    break;
  }

  exponent *= exponentSign;

  // Process the mantissa width (which we actually ignore)
  if (m_curChar == '|')
  {
    nextChar();
    if ( (mantissaWidth = scanSmallNonNegInt10()) < 0)
      return null;
  }
  else
    mantissaWidth = 0;

  if (exact > 0)
  {
    // An exact result was explicitly requested. Depending on the exponent sign we represent it
    // either as a very big integer, or as a rational fraction num/10**exp

    // NOTE: we could convert this number to real and then use the normal toExact() routine,
    // which works pretty good, but we would lose precision.

    // TODO: I am sure there is a better way to do this, but what ???

    if (fract == null)
      fract = SchemeFixInt.ZERO;
    if (whole == null)
      whole = SchemeFixInt.ZERO;

    int flen = fract.countDecimalDigits();
    if (flen <= exponent)
    {
      if (exponent > flen)
          fract = (SchemeInteger)fract.mul(SchemeInteger.pow10(exponent - flen));
      if (exponent > 0)
        whole = (SchemeInteger)whole.mul(SchemeInteger.pow10(exponent));
      whole.add( fract );
      if (sign < 0) whole = whole.neg();
      return whole;
    }
    else
    {
      if (flen > 0)
        whole = (SchemeInteger)whole.mul(SchemeInteger.pow10(flen));
      whole = (SchemeInteger)whole.add( fract );
      if (sign < 0) whole = whole.neg();
      return SchemeRational.make( whole, SchemeInteger.pow10(flen-exponent));
    }
  }
  else
  {
    // An inexact result was requested or implied. Convert it to double using Java's string
    // routines
    StringBuilder buf = new StringBuilder(16);

    if (sign < 0)
      buf.append( '-' );
    if (whole != null)
      buf.append( whole.toString() );
    if (fract != null)
    {
      buf.append('.');
      buf.append( fract.toString() );
    }
    if (exponent != 0)
      buf.append( 'e' ).append( exponent );

    try
    {
      return SchemeReal.valueOf( buf.toString() );
    }
    catch (NumberFormatException e)
    {
      error( e, "Invalid real number" ); // shouldn't happen
      return SchemeReal.ONE; // return just any number for error recovery
    }
  }
}

private final char[] m_preallocUintegerBuf = new char[32];

/**
 * Scan and return an unsigned integer in the specified radix.
 *
 * @param radix number base
 * @return false on error
 */
private final SchemeInteger scanUInteger ( int radix )
{
  if (!isBaseDigit(radix, m_curChar ))
  {
    error( null, "Invalid number" );
    return null;
  }

  /* This is valid, bit too allocation intensive. Several allocations per digit !!

    SchemeInteger sbase = SchemeFixInt.make( base );
    SchemeInteger res = SchemeFixInt.make( baseDigitToInt(m_curChar) );
    while (isBaseDigit( base, nextChar() ))
      res = (SchemeInteger)res.mul( sbase ).add(SchemeFixInt.make(baseDigitToInt(m_curChar)));
  */

  char[] buf = m_preallocUintegerBuf;
  int len = 0;
  do
  {
    if (len == buf.length-1) // Reallocate ? (leaving space for the terminator)
    {
      char[] newBuf = new char[buf.length*2];
      System.arraycopy( buf, 0, newBuf, 0, len );
      buf = newBuf;
    }
    buf[len++] = (char)m_curChar;
  }
  while (isBaseDigit(radix, nextChar() ));

  return SchemeInteger.makeInteger(radix, buf, len );
}

/**
 * Scan a small non-negative integer int base 10. It should fit in an int.
 * Overflows are reported internally.
 *
 * @return -1 on error, the exponent otherwise.
 */
private final int scanSmallNonNegInt10 ()
{
  if (!isBaseDigit(10, m_curChar ))
  {
    error( null, "Invalid number" );
    return -1;
  }

  int limit = Integer.MAX_VALUE / 10; // maximum int we can safely multiply by 10
  int val = baseDigitToInt(m_curChar);
  while (isBaseDigit(10, nextChar()))
  {
    int digit = baseDigitToInt( m_curChar );
    if (val <= limit)
    {
      val *= 10;
      if (val + digit >= val)
      {
        val += digit;
        continue;
      }
    }

    error( null, "Number overflow" );
    // Consume the rest of the number
    while (isBaseDigit(10, nextChar()))
      {};
    return 0;
  }

  return val;
}

private final Token scanString ()
{
  StringBuilder buf = new StringBuilder(8);

  loop: for(;;)
  {
    if (m_curChar == '"')
    {
      nextChar();
      break;
    }
    else if (m_curChar < 0)
    {
      error( null, "Unterminated string lexeme at end of input" );
      break;
    }
    else if (m_curChar == '\\')
    {
      nextChar();
      switch (m_curChar)
      {
      case -1: error( null, "Unterminated string escape at end of input" ); break loop;

      case 'a': buf.append( (char)7 ); nextChar(); break;
      case 'b': buf.append( '\b' ); nextChar(); break;
      case 't': buf.append( '\t' ); nextChar(); break;
      case 'n': buf.append( '\n' ); nextChar(); break;
      case 'v': buf.append( (char)11 ); nextChar(); break;
      case 'f': buf.append( '\f' ); nextChar(); break;
      case 'r': buf.append( '\r' ); nextChar(); break;
      case '"': buf.append( '"' ); nextChar(); break;
      case '\\': buf.append( '\\' ); nextChar(); break;

      case 'x':
        nextChar();
        buf.appendCodePoint( scanInlineHexEscape() );
        break;

      default:
        // '\\' <intraline whitespace> '\n' <intraline whitespace> must be ignored
        while (m_curChar != '\n' && Character.isWhitespace(m_curChar))
          nextChar();
        if (m_curChar != '\n')
        {
          error( null, "Unterminated string escape at end of input" );
          break loop;
        }
        nextChar();
        while (m_curChar != '\n' && Character.isWhitespace(m_curChar))
          nextChar();
        break;
      }
    }
    else
    {
      buf.appendCodePoint( m_curChar );
      nextChar();
    }
  }

  m_valueString = buf.toString();
  return Token.STR;
}

private final Token scanRestIdentifier ( int firstChar )
{
  return scanRestIdentifier( firstChar, -1 );
}

private final Token scanRestIdentifier ( int firstChar1, int firstChar2 )
{
  StringBuilder buf = new StringBuilder( 16 );
  buf.appendCodePoint( firstChar1 );
  if (firstChar2 >= 0)
    buf.appendCodePoint( firstChar2 );

  loop: for(;;)
  {
    switch (m_curChar)
    {
    // <digit>
    case '0': case '1': case '2': case '3': case '4':
    case '5': case '6': case '7': case '8': case '9':
    // <special subsequent>
    case '+': case '-': case '.': case '@':
    // <special initial>
    case '!': case '$': case '%': case '&': case '*': case '/': case ':': case '<':
    case '=': case '>': case '?': case '^': case '_': case '~':
      buf.appendCodePoint(m_curChar);
      nextChar();
      break;

    // <inline hex escape>
    case '\\':
      nextChar();
      if (m_curChar == 'x') // \x -> identifier starting with an inline hex escape
      {
        nextChar();
        buf.appendCodePoint(scanInlineHexEscape());
      }
      else
      {
        error( null, "Invalid escape in an identifier" );
        // Leave the character to be processed in the next iteration
      }
      break;

    default:
      if (Character.isLetter(m_curChar))
      {
        buf.appendCodePoint(m_curChar);
        nextChar();
        break;
      }
      else
        break loop;
    }
  }

  String name = buf.toString();

  if (!isDelimiter(m_curChar))
    error( null, "Identifier \"%s\" not terminated by a delimiter", escapeToString(name) );

  return identifier( name );
}

private final Token identifier ( String str )
{
  m_valueIdent = m_symbolMap.newSymbol( str );
  return Token.IDENT;
}

/**
 * Called after the \x has been scanned, to scan the rest of the inline hex character.
 * Returns its validated value. Note that the character could be a supplementary code
 * point (> 0xFFFF).
 *
 * @return the validated value of the inline hex character.
 */
private final int scanInlineHexEscape ()
{
  int resultCodePoint = 0;
  boolean err = false;

  if (!isBaseDigit(16, m_curChar))
  {
    error( null, "Invalid inline hex escape" );
    return ' ';
  }

  // Sequence of hex digits up to ';'
  do
  {
    if (!err)
    {
      int newResult = (resultCodePoint << 4) + baseDigitToInt(m_curChar);
      if (newResult >= resultCodePoint)
        resultCodePoint = newResult;
      else
      {
        error( null, "Inline hex character overflow" );
        err = true;
        resultCodePoint = ' ';
      }
    }
  }
  while(isBaseDigit(16, nextChar()));

  if (m_curChar == ';')
    nextChar();
  else
  {
    error( null, "Inline hex character must be terminated with #\\;" );
    resultCodePoint = ' ';
  }

  return validateCodePoint( resultCodePoint );
}

private static final HashMap<String,Integer> s_charNames = new HashMap<String, Integer>();
static {
  s_charNames.put( "nul", 0 );
  s_charNames.put( "alarm", 7 );
  s_charNames.put( "backspace", 8 );
  s_charNames.put( "tab", 9 );
  s_charNames.put( "linefeed", 10 );
  s_charNames.put( "newline", 10 );
  s_charNames.put( "vtab", 11 );
  s_charNames.put( "page", 12 );
  s_charNames.put( "return", 13 );
  s_charNames.put( "esc", 0x1B );
  s_charNames.put( "space", 32 );
  s_charNames.put( "delete", 0x7F );
}

private final int m_charBuf[] = new int[16]; // The longest character name is "linefeed"

private final Token scanCharacter ()
{
  int len = 0;
  int resultChar;

  // Collect the character. The first character can be anything, even a delimiter. The rest are
  // terminated by a delimiter. So, we get the first character separately

  if (m_curChar < 0)
  {
    error( null, "Unterminated character lexeme at end of input" );
    return null;
  }
  m_charBuf[len++] = m_curChar;

  // R6RS specifies that a character like #\x000000000000000000001 is valid, so we can't just
  // gather the character in a buffer. We must read the first two characters, decide if it
  // is a hex format and proceed

  nextChar(); // Second character
  if (!isDelimiter(m_curChar))
  {
    m_charBuf[len++] = m_curChar;
    nextChar();
  }

  // Now check for x[hex-digit]
  if (len == 2 && m_charBuf[0] == 'x' && isBaseDigit(16, m_charBuf[1]))
  {
    // A hex-encoded character!
    resultChar = baseDigitToInt( m_charBuf[1] );
    boolean err = false;
    while (!isDelimiter(m_curChar))
    {
      if (!err)
      {
        if (!isBaseDigit(16, m_curChar))
        {
          error( null, "Invalid hex digit in #\\x" );
          err = true;
        }
        else
        {
          resultChar = (resultChar<<4)+ baseDigitToInt(m_curChar);
          if (resultChar > 0xFFFF)
          {
            error( null, "Character value overflow" );
            err = true;
          }
        }
      }
      nextChar();
    }

    if (err)
      resultChar = ' ';
  }
  else
  {
    // This is a regular named character. Collect the rest
    while (!isDelimiter(m_curChar))
    {
      if (len < m_charBuf.length)
        m_charBuf[len] = m_curChar;
      ++len;
      nextChar();
    }

    if (len == 1) // Just a plain old char
    {
      resultChar = m_charBuf[0];
    }
    else // Must look up a character name
    {
      String name = new String( m_charBuf, 0, len );
      Integer code;
      if (len <= m_charBuf.length) // check for name overflow
        code = s_charNames.get( name );
      else
        code = null; // The character name was too long and we ignored the rest

      if (code != null)
        resultChar = code;
      else
      {
        error( null, "Invalid character #\\"+name );
        resultChar = ' ';
      }
    }
  }

  resultChar = validateUnicodeChar( resultChar );

  m_valueChar = (char)resultChar;
  return Token.CHAR;
}

} // class

