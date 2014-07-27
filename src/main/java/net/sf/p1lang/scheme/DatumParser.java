/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import net.sf.p1lang.scheme.Lexer.Token;

public final class DatumParser
{
private final Lexer m_lex;

public static final Object EOF = new Object();
/** Special return value used only internally for DATUM_COMMENT */
private static final Object DAT_COM = new Object();

private final SourceCoords m_coords = new SourceCoords();

DatumParser ( final Lexer lex )
{
  m_lex = lex;
  next();
}

private final Token next ()
{
  m_coords.assign( m_lex.m_tokCoords );
  return m_lex.nextToken();
}

private static boolean setContains ( int set, Token tok )
{
  return (set & (1 << tok.ordinal())) != 0;
}

private static int setAdd ( int set, Token tok )
{
  return set | (1 << tok.ordinal());
}

/**
 *
 * @return {@link #EOF} on EOF
 */
public final Object parseDatum ()
{
  return readSkipDatCom(0);
}

private Object readSkipDatCom ( int termSet )
{
  // Ignore DATUM_COMMENT-s
  Object res;
  while ( (res = read(0)) == DAT_COM)
    {}
  return res;
}

/**
 *
 * @param termSet
 * @return {@link #EOF} on EOF
 */
private Object read ( int termSet )
{
  boolean inError = false;

  for(;;)
  {
    Object res;
    switch (m_lex.m_curToken)
    {
    case EOF: return EOF;

    case BOOL: res = m_lex.m_valueBool;  next(); return res;
    case NUMBER: res = m_lex.m_valueNumber;  next(); return res;
    case CHAR: res = m_lex.m_valueChar;  next(); return res;
    case STR: res = m_lex.m_valueString;  next(); return res;
    case IDENT: res = m_lex.m_valueIdent;  next(); return res;

    case LPAR:
    case LSQUARE:
    case HASH_LPAR:
    case APOSTR:
    case ACCENT:
    case COMMA:
    case COMMA_AT:
    case HASH_APOSTR:
    case HASH_ACCENT:
    case HASH_COMMA:
    case HASH_COMMA_AT:
      return compound( termSet );

    case DATUM_COMMENT:
      next();
      read( termSet ); // Ignore the next datum
      return DAT_COM;

    case NESTED_COMMENT_END:
    case NESTED_COMMENT_START:
      assert(false);
    case DOT:
    case RPAR:
    case RSQUARE:
      // Skip invalid tokens, reporting only the first one
      if (!inError)
      {
        error( "'%s' isn't allowed here", m_lex.m_curToken.repr );
        inError = true;
      }
      if (setContains(termSet,m_lex.m_curToken))
        return Pair.NULL;
      next();
      break;
    }
  }
}

private final Object compound ( int termSet )
{
  Object res;
  switch (m_lex.m_curToken)
  {
  case LPAR:      next(); res = list( Token.RPAR, termSet ); return res;
  case LSQUARE:   next(); res = list( Token.RSQUARE, termSet ); return res;
  case HASH_LPAR: next(); res = vector( Token.RPAR, termSet ); return res;

  case APOSTR:         next(); return abbrev( m_lex.m_symbolMap.sym_quote, termSet );
  case ACCENT:         next(); return abbrev( m_lex.m_symbolMap.sym_quasiquore, termSet );
  case COMMA:          next(); return abbrev( m_lex.m_symbolMap.sym_unquote, termSet );
  case COMMA_AT:       next(); return abbrev( m_lex.m_symbolMap.sym_unquote_splicing, termSet );
  case HASH_APOSTR:    next(); return abbrev( m_lex.m_symbolMap.sym_syntax, termSet );
  case HASH_ACCENT:    next(); return abbrev( m_lex.m_symbolMap.sym_quasisyntax, termSet );
  case HASH_COMMA:     next(); return abbrev( m_lex.m_symbolMap.sym_unsyntax, termSet );
  case HASH_COMMA_AT:  next(); return abbrev( m_lex.m_symbolMap.sym_unsyntax_splicing, termSet );

  default:
    assert(false);
    return Pair.NULL;
  }
}

private final Pair abbrev ( Symbol sym, int termSet )
{
  SourceCoords savedCoords = new SourceCoords( m_coords );
  Object datum;
  if ( (datum = readSkipDatCom( termSet )) == EOF)
    error( "Unterminated abbreviation" );

  return
    new PositionedPair(
      sym, new PositionedPair(datum, Pair.NULL).setCoords(m_coords)
    ).setCoords( savedCoords );
}

// TODO: this routine uses stack proportional to the size of the list.
// Ways to address that:
//   - mutable pairs
//   - build the list in reverse and reverse it again
//   - use a different intermediate storage and build the list in the end
//
private final Pair list ( Token terminator, int termSet )
{
  Object car, cdr;
  termSet = setAdd(termSet,terminator);
  int carTermSet = setAdd( termSet, Token.DOT );

  // Check for an empty list. It is complicated by having to skip DATUM_COMMENT-s
  do
  {
    if (m_lex.m_curToken == terminator)
    {
      next();
      return Pair.NULL;
    }
  }
  while ( (car = read( carTermSet )) == DAT_COM);

  SourceCoords savedCoords = new SourceCoords( m_coords );

  if (car == EOF)
  {
    error( "Unterminated list" );
    return Pair.NULL;
  }
  if (m_lex.m_curToken == Token.DOT)
  {
    next();
    if ( (cdr = readSkipDatCom(termSet)) == EOF)
    {
      error( "Unterminated list" );
      return Pair.NULL;
    }
    if (m_lex.m_curToken != terminator)
    {
      error( "Expected %s", terminator.repr );
    }
    next();
  }
  else
    cdr = list( terminator, termSet );

  return new PositionedPair( car, cdr ).setCoords( savedCoords );
}

private static final Object[] s_nullVec = new Object[0];

private final Object vector ( Token terminator, int termSet )
{
  Object[] vec = null;
  int count = 0;
  termSet = setAdd(termSet,terminator);

  while (m_lex.m_curToken != terminator)
  {
    Object elem;
    if ( (elem = read( termSet )) == DAT_COM) // skip DATUM_COMMENT-s
      continue;
    if ( elem == EOF)
    {
      error( "Unterminated vector" );
      return new Object[0]; // Return an empty vector just for error recovery
    }

    if (vec == null)
      vec = new Object[4];
    else if (count == vec.length)
    {
      Object[] newVec = new Object[vec.length*2];
      System.arraycopy( vec, 0, newVec, 0, count);
      vec = newVec;
    }
    vec[count++] = elem;
  }
  next(); // skip the closing parren

  if (vec == null)
    return s_nullVec;
  else if (count == vec.length)
    return vec;
  else
  {
    Object[] res = new Object[count];
    System.arraycopy( vec, 0, res, 0, count);
    return res;
  }
}

private final void needSkip ( Token tok, int termSet )
{
  if (m_lex.m_curToken != tok)
    error( "Expected %s", tok.repr );

  while (m_lex.m_curToken != Token.EOF)
  {
    if (m_lex.m_curToken == tok)
    {
      next();
      return;
    }
    if (setContains(termSet,m_lex.m_curToken))
      return;
  }
}

private final void error ( String message, Object ... args )
{
  m_lex.getErrorReporter().error( m_lex.m_tokCoords, null, message, args );
}

} // class

