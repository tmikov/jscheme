package net.sf.p1lang.scheme;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * SchemeReal Tester.
 *
 * @author T.Mikov
 * @since <pre>04/03/2008</pre>
 * @version 1.0
 */
public class SchemeRealTest extends TestCase
{
public SchemeRealTest(String name)
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

public void testMulti() throws Exception
{
  // getRank()
  assertEquals(SchemeReal.ZERO.getRank(), SchemeNumber.RANK_REAL );

  // isZero
  assertTrue(SchemeReal.ZERO.isZero() );
  assertTrue(SchemeReal.make(0).isZero() );
  assertFalse(SchemeReal.ONE.isZero() );
  assertFalse(SchemeReal.make(1).isZero() );

  // toReal
  assertTrue(SchemeReal.ZERO.toJavaDouble() == 0 );
  assertTrue(SchemeReal.ONE.toJavaDouble() == 1 );

  // toInteger
  assertEquals( SchemeReal.ZERO.toInteger(), SchemeFixInt.ZERO );
  assertEquals( SchemeReal.ONE.toInteger(), SchemeFixInt.ONE );
  assertEquals( SchemeReal.make(32768).toInteger(), SchemeFixInt.make(32768) );
  needOverflow( new Runnable() { public void run () {
    SchemeReal.make(1.2).toInteger();
  }});

  // neg()
  assertEquals(SchemeReal.ZERO.neg(), SchemeReal.ZERO);
  assertEquals(SchemeReal.ONE.neg(), SchemeReal.MINUS_ONE);
  assertEquals(SchemeReal.make(1.2).neg(), SchemeReal.make(-1.2));

  // add
  assertEquals(SchemeReal.ONE.add(SchemeReal.ZERO), SchemeReal.ONE );
  assertEquals(SchemeReal.ONE.add(SchemeReal.ONE), SchemeReal.make(2.0) );

  // sub
  assertEquals(SchemeReal.ONE.sub(SchemeReal.ZERO), SchemeReal.ONE );
  assertEquals(SchemeReal.ONE.sub(SchemeReal.MINUS_ONE), SchemeReal.make(2.0) );

  // subr
  assertEquals(SchemeReal.ONE.subr(SchemeReal.ZERO), SchemeReal.MINUS_ONE );
  assertEquals(SchemeReal.ONE.subr(SchemeReal.MINUS_ONE), SchemeReal.make(-2.0) );

  // mul
  assertEquals(SchemeReal.ONE.mul(SchemeReal.ZERO), SchemeReal.ZERO );
  assertEquals(SchemeReal.ONE.mul(SchemeReal.MINUS_ONE), SchemeReal.make(-1) );

  // div
  assertEquals(SchemeReal.ZERO.div(SchemeReal.ONE), SchemeReal.ZERO );
  assertEquals(SchemeReal.ONE.div(SchemeReal.MINUS_ONE), SchemeReal.make(-1) );
  assertEquals(SchemeReal.ZERO.div(SchemeReal.ZERO), SchemeReal.NAN );

  // divr
  assertEquals(SchemeReal.ZERO.divr(SchemeReal.ONE), SchemeReal.POS_INF );
  assertEquals(SchemeReal.ONE.divr(SchemeReal.MINUS_ONE), SchemeReal.make(-1) );
  assertEquals(SchemeReal.ZERO.divr(SchemeReal.ZERO), SchemeReal.NAN );

}

private SchemeNumber nrat ( long a, long b )
{
  return SchemeRational.make( SchemeFixInt.make(a), SchemeFixInt.make(b) );
}

public void testConv () throws Exception
{
  assertEquals( SchemeReal.ONE.add(nrat(1,2)), SchemeReal.make(1.5) );
  assertEquals( SchemeReal.ONE.sub(nrat(1,2)), SchemeReal.make(0.5) );
  assertEquals( SchemeReal.ONE.mul(nrat(1,2)), SchemeReal.make(0.5) );
  assertEquals( SchemeReal.ONE.div(nrat(1,2)), SchemeReal.make(2) );

  assertEquals( SchemeReal.ONE.add(SchemeFixInt.ONE), SchemeReal.make(2) );
  assertEquals( SchemeReal.ONE.sub(SchemeFixInt.ONE), SchemeReal.make(0) );
  assertEquals( SchemeReal.ONE.mul(SchemeFixInt.ONE), SchemeReal.make(1) );
  assertEquals( SchemeReal.ONE.div(SchemeFixInt.ONE), SchemeReal.make(1) );
}

public void testExact ()
{
  assertEquals(SchemeRational.NAN, SchemeReal.make( Double.NaN ).toExact() );
  assertEquals(SchemeRational.POS_INF, SchemeReal.make( Double.POSITIVE_INFINITY ).toExact() );
  assertEquals(SchemeRational.NEG_INF, SchemeReal.make( Double.NEGATIVE_INFINITY ).toExact() );

  assertEquals( nrat(1,1), SchemeReal.make( 1.0 ).toExact() );
  assertEquals( nrat(2,1), SchemeReal.make( 2.0 ).toExact() );
  assertEquals( nrat(-2,1), SchemeReal.make( -2.0 ).toExact() );
  assertEquals( nrat(1,2), SchemeReal.make( 0.5 ).toExact() );
  assertEquals( nrat(105,10), SchemeReal.make( 10.5 ).toExact() );
  assertEquals( nrat(-105,10), SchemeReal.make( -10.5 ).toExact() );

  // Test denormals
  double d1 = Double.longBitsToDouble( (1L << 51) );
  assertEquals( d1, SchemeReal.make( d1 ).toExact().toJavaDouble() );

  double d2 = Double.longBitsToDouble( (1L << 51)+100 );
  assertEquals( d2, SchemeReal.make( d2 ).toExact().toInexact().toJavaDouble() );
}

public static Test suite()
{
  return new TestSuite(SchemeRealTest.class);
}

} // SchemeRealTest
