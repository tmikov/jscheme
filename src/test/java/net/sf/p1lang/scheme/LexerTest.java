package net.sf.p1lang.scheme;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import net.sf.p1lang.scheme.Lexer.Token;

/**
 * Lexer Tester.
 *
 * @author T.Mikov
 * @since <pre>04/03/2008</pre>
 * @version 1.0
 */
public class LexerTest extends TestCase
{
public LexerTest(String name)
{
  super(name);
}

private Lexer m_lexer;
private SymbolMap m_map;

public void setUp() throws Exception
{
  super.setUp();
}

public void tearDown() throws Exception
{
  m_lexer = null;
  m_map = null;
  super.tearDown();
}

private void lexer ( String str )
{
  m_lexer = TestUtils.lexer( str );
  m_map = m_lexer.m_symbolMap;
}

private void n ( Token tok )
{
  assertEquals( tok, m_lexer.nextToken() );
}

private void needError ()
{
  try
  {
    m_lexer.nextToken();
    fail( "Lexer did not generate an error" );
  }
  catch(TooManyErrors ignored) {};
}

private void checkIdent ( String name )
{
  n( Token.IDENT );
  assertEquals( m_map.newSymbol(name), m_lexer.m_valueIdent );
}

private void checkString ( String str )
{
  n( Token.STR );
  assertEquals( str, m_lexer.m_valueString );
}

private void checkChar ( char ch )
{
  n( Token.CHAR );
  assertEquals( ch, m_lexer.m_valueChar );
}

private void checkNumber ( long n )
{
  n( Token.NUMBER );
  assertEquals( SchemeFixInt.make(n), m_lexer.m_valueNumber );
}

private void checkNumber ( double n )
{
  n( Token.NUMBER );
  assertEquals( SchemeReal.make(n), m_lexer.m_valueNumber );
}

private void checkNumber ( long a, long b )
{
  n( Token.NUMBER );
  assertEquals( SchemeRational.make(SchemeFixInt.make(a), SchemeFixInt.make(b)),
                m_lexer.m_valueNumber );
}

private void checkComplex ( double a, double b )
{
  n( Token.NUMBER );
  assertEquals( SchemeComplex.make(SchemeReal.make(a),SchemeReal.make(b)),
                m_lexer.m_valueNumber );
}

private void checkComplex ( long a, long b )
{
  n( Token.NUMBER );
  assertEquals( SchemeComplex.make(SchemeFixInt.make(a), SchemeFixInt.make(b)),
                m_lexer.m_valueNumber );
}


// Just a simple test to get things going
public void testSimple () throws Exception
{
  lexer( "(set! my-var (+ 10 20/3))" );
  n( Token.LPAR );
  checkIdent( "set!" );
  checkIdent( "my-var" );
  n( Token.LPAR );
  checkIdent( "+" );
  n( Token.NUMBER );
  n( Token.NUMBER );
  n( Token.RPAR );
  n( Token.RPAR );
  n( Token.EOF );
}

public void testBoolean () throws Exception
{
  lexer( "#t #T #f #F" );
  n( Token.BOOL ); assertEquals( m_lexer.m_valueBool, true );
  n( Token.BOOL ); assertEquals( m_lexer.m_valueBool, true );
  n( Token.BOOL ); assertEquals( m_lexer.m_valueBool, false );
  n( Token.BOOL ); assertEquals( m_lexer.m_valueBool, false );
  n( Token.EOF );
}

public void testString ()
{
  lexer( "\"abc\\\"def\"" );
  checkString( "abc\"def" );
  n( Token.EOF );
}

public void testChar ()
{
  lexer( "#\\a #\\newline #\\x0C" );
  checkChar( 'a' );
  checkChar( '\n' );
  checkChar( (char)0xC );
  n( Token.EOF );
}

public void testNumber ()
{
  // Quick test to see that it is mostly OK
  lexer( "1 10 244556 1. .3 1.23 -20 +23 -1.56 1/2 -1/2 10/3 #i1/2 #e0.3 1.+2.i 1+2i" );
  checkNumber( 1 );
  checkNumber( 10 );
  checkNumber( 244556 );
  checkNumber( 1.0 );
  checkNumber( 0.3 );
  checkNumber( 1.23 );
  checkNumber( -20 );
  checkNumber( +23 );
  checkNumber( -1.56 );
  checkNumber( 1, 2 );
  checkNumber( -1, 2 );
  checkNumber( 10, 3 );
  checkNumber( 0.5 );
  checkNumber( 3, 10 );
  checkComplex( 1.0, 2.0 );
  checkComplex( 1, 2 );
  n( Token.EOF );

  // Some quick errors to see that they work at all
  lexer( "#e#i#e1" ); needError();

  // Prefixes
  lexer(
    " #e1 #E1 #i1 #I1"+
    " #b10 #B10 #o10 #O10 #d10 #D10 #x10 #X10"+
    " #e#b10 #b#e10 #b#i10 #i#b10"+
    ""
  );
  checkNumber( 1 ); checkNumber( 1 ); checkNumber( 1.0 ); checkNumber( 1.0 );
  checkNumber( 2 ); checkNumber( 2 ); checkNumber( 8 ); checkNumber( 8 );
  checkNumber( 10 ); checkNumber( 10 ); checkNumber( 16 ); checkNumber( 16 );
  checkNumber( 2 ); checkNumber( 2 ); checkNumber( 2.0 ); checkNumber( 2.0 );
  n( Token.EOF );
}

public static Test suite()
{
  return new TestSuite(LexerTest.class);
}

;

} // LexerTest
