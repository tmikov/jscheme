package net.sf.p1lang.scheme;

import java.math.BigDecimal;
import java.math.MathContext;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * SchemeRational Tester.
 *
 * @author T.Mikov
 * @since <pre>04/03/2008</pre>
 * @version 1.0
 */
public class SchemeRationalTest extends TestCase
{
public SchemeRationalTest(String name)
{
  super(name);
}

public void setUp() throws Exception
{
  super.setUp();
}

public void tearDown() throws Exception
{
  super.tearDown();
}

private void needOverflow ( Runnable r )
{
  try
  {
    r.run();
    fail( "ArithmeticException not thrown" );
  }
  catch (ArithmeticException ignored) {};
}

private SchemeRational rat ( long a, long b )
{
  return (SchemeRational)SchemeRational.make( SchemeFixInt.make(a), SchemeFixInt.make(b) );
}

private SchemeNumber nrat ( long a, long b )
{
  return SchemeRational.make( SchemeFixInt.make(a), SchemeFixInt.make(b) );
}

private void checkRat( long a, long b, long x, long y )
{
  SchemeRational o = rat( a, b );
  assertEquals( o.numerator, SchemeFixInt.make(x) );
  assertEquals( o.denominator, SchemeFixInt.make(y) );
}

public void testMulti() throws Exception
{
  // make
  checkRat( 1, 2, 1, 2 );
  checkRat( 20, 6, 10, 3 );
  checkRat( -20, 6, -10, 3 );
  checkRat( 20, -6, -10, 3 );
  checkRat( -20, -6, 10, 3 );
  assertEquals( nrat( 20, 4 ), SchemeFixInt.make(5) );
  nrat( Long.MIN_VALUE, -1 );

  // getRank()
  assertEquals( SchemeRational.NAN.getRank(), SchemeNumber.RANK_RATIONAL );

  // toReal()
  assertEquals( nrat( 2, 4 ).toJavaDouble(), 0.5 );

  // toInteger()
  needOverflow( new Runnable() { public void run () {
    nrat( 1, 2 ).toInteger();
  }});

  // neg()
  assertEquals( nrat( 10, 3 ).neg(), nrat(-10, 3) );

  // add()
  assertEquals( nrat(10,3).add( nrat(10,3 ) ), nrat( -20, -3 ) );
  assertEquals( nrat(3, 7).add( nrat(4,7) ), SchemeFixInt.ONE );

  // sub()
  assertEquals( nrat(10,3).sub( nrat(-10,3 ) ), nrat( 20, 3 ) );
  assertEquals( nrat(3, 7).sub( nrat(4,7) ), nrat( -1, 7 ) );
  assertEquals( nrat(3, 7).sub( nrat(3,7) ), SchemeFixInt.ZERO );

  // subr()
  assertEquals( nrat(10,3).subr( SchemeFixInt.ONE ), nrat( -7, 3 ) );
  assertEquals( nrat(3, 7).subr( SchemeFixInt.ONE ), nrat( 4, 7 ) );
  assertEquals( nrat(3, 7).subr( SchemeFixInt.ZERO ), nrat(-3, 7) );

  // mul()
  assertEquals( nrat(10,3).mul(SchemeFixInt.make(3)), SchemeFixInt.make(10) );
  assertEquals( nrat(10,3).mul(nrat(2,3)), nrat(20,9) );

  // div()
  assertEquals( nrat(10,3).div(nrat(1,2)), nrat(20,3) );

  // divr()
  assertEquals( nrat(10,3).divr(SchemeFixInt.make(5)), nrat(3,2) );
}

public void testConv () throws Exception
{
  assertEquals( nrat(1,2).add(SchemeFixInt.ONE), nrat(3,2) );
  assertEquals( nrat(1,2).sub(SchemeFixInt.ONE), nrat(-1,2) );
  assertEquals( nrat(1,2).mul(SchemeFixInt.ONE), nrat(1,2) );
  assertEquals( nrat(1,2).div(SchemeFixInt.ONE), nrat(1,2) );

  assertEquals( nrat(1,2).add(SchemeReal.ONE), SchemeReal.make(1.5) );
  assertEquals( nrat(1,2).sub(SchemeReal.ONE), SchemeReal.make(-0.5) );
  assertEquals( nrat(1,2).mul(SchemeReal.ONE), SchemeReal.make(0.5) );
  assertEquals( nrat(1,2).div(SchemeReal.ONE), SchemeReal.make(0.5) );
}

public void testToJavaDouble () throws Exception
{
  assertEquals( 0.5, nrat(1,2).toJavaDouble() );
  assertEquals( BigDecimal.ONE.divide(BigDecimal.valueOf(Long.MAX_VALUE), MathContext.DECIMAL64).doubleValue(),
                nrat(1,Long.MAX_VALUE).toJavaDouble() );
}

public static Test suite()
{
  return new TestSuite(SchemeRationalTest.class);
}

} // SchemeRationalTest
