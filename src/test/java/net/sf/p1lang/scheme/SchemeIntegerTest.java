package net.sf.p1lang.scheme;

import java.math.BigInteger;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * SchemeInteger Tester.
 *
 * @author T.Mikov
 * @since <pre>04/03/2008</pre>
 * @version 1.0
 */
public class SchemeIntegerTest extends TestCase
{
public SchemeIntegerTest(String name)
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

private static void badd ( long a, long b )
{
  assertEquals(
    SchemeBigInt.makeBigInt(BigInteger.valueOf(a).add(BigInteger.valueOf(b))),
    SchemeFixInt.make(a).add(SchemeFixInt.make(b))
  );
}

private static void bsub ( long a, long b )
{
  assertEquals(
    SchemeBigInt.makeBigInt(BigInteger.valueOf(a).subtract(BigInteger.valueOf(b))),
    SchemeFixInt.make(a).sub(SchemeFixInt.make(b))
  );
}
private static void bsubr ( long a, long b )
{
  assertEquals(
    SchemeBigInt.makeBigInt(BigInteger.valueOf(b).subtract(BigInteger.valueOf(a))),
    SchemeFixInt.make(a).subr(SchemeFixInt.make(b))
  );
}
private static void bmul ( long a, long b )
{
  assertEquals(
    SchemeBigInt.makeBigInt(BigInteger.valueOf(a).multiply(BigInteger.valueOf(b))),
    SchemeFixInt.make(a).mul(SchemeFixInt.make(b))
  );
}
private static void bdiv ( long a, long b )
{
  assertEquals(
    SchemeBigInt.makeBigInt(BigInteger.valueOf(a).divide(BigInteger.valueOf(b))),
    SchemeFixInt.make(a).div(SchemeFixInt.make(b))
  );
}

private static final long MI = Long.MIN_VALUE;
private static final long MA = Long.MAX_VALUE;

public void testMulti () throws Exception
{
  // signum()
  assertEquals(SchemeFixInt.ZERO.signum(), 0 );
  assertEquals(SchemeFixInt.ONE.signum(), 1 );
  assertEquals(SchemeFixInt.MINUS_ONE.signum(), -1 );

  // getRank()
  assertEquals(SchemeFixInt.ZERO.getRank(), SchemeNumber.RANK_FIXINT);

  // isZero()
  assertTrue(SchemeFixInt.ZERO.isZero() );
  assertFalse(SchemeFixInt.ONE.isZero() );

  // toReal
  assertEquals( SchemeFixInt.make(32768).toJavaDouble(), 32768.0 );

  // toInteger
  assertEquals( SchemeFixInt.ONE.toInteger(), SchemeFixInt.ONE );

  // neg()
  assertEquals( SchemeFixInt.ONE.neg(), SchemeFixInt.MINUS_ONE );
  assertEquals( SchemeFixInt.MINUS_ONE.neg(), SchemeFixInt.ONE );
  assertTrue( SchemeFixInt.ZERO.neg().isZero() );
  SchemeFixInt.make( MA ).neg();
  assertEquals( SchemeFixInt.make( MI ).neg(), SchemeBigInt.makeBigInt(BigInteger.valueOf(MI).negate()));

  // add()
  assertEquals( SchemeFixInt.ONE.add(SchemeFixInt.ONE), SchemeFixInt.make(2) );
  assertEquals( SchemeFixInt.ONE.add(SchemeFixInt.MINUS_ONE), SchemeFixInt.make(0) );
  SchemeFixInt.make(MA-1).add( SchemeFixInt.ONE );
  SchemeFixInt.make(MA ).add(SchemeFixInt.make(MI));
  badd(MA,1);
  badd(MA,MA );
  badd(MI,-1);
  badd(MI,MI);

  // sub()
  assertEquals( SchemeFixInt.ONE.sub(SchemeFixInt.ONE), SchemeFixInt.make(0) );
  assertEquals( SchemeFixInt.ONE.sub(SchemeFixInt.MINUS_ONE), SchemeFixInt.make(2) );
  SchemeFixInt.make(MI+1).sub( SchemeFixInt.ONE );
  bsub(MI,1);
  bsub(MI,MA);
  bsub(MA,-1);
  bsub(MA,MI);

  // subr()
  assertEquals( SchemeFixInt.ONE.subr(SchemeFixInt.ONE), SchemeFixInt.make(0) );
  assertEquals( SchemeFixInt.ONE.subr(SchemeFixInt.MINUS_ONE), SchemeFixInt.make(-2) );
  SchemeFixInt.ONE.subr( SchemeFixInt.make(MI+1) );
  bsubr(1,MI);
  bsubr(MA,MI);
  bsubr(-1,MA);
  bsubr(MI,MA);

  // mul
  assertEquals(SchemeFixInt.ONE.mul(SchemeFixInt.ONE), SchemeFixInt.make(1) );
  assertEquals(SchemeFixInt.ONE.mul(SchemeFixInt.ZERO), SchemeFixInt.make(0) );
  assertEquals(SchemeFixInt.MINUS_ONE.mul(SchemeFixInt.ONE), SchemeFixInt.make(-1) );
  assertEquals(SchemeFixInt.MINUS_ONE.mul(SchemeFixInt.MINUS_ONE), SchemeFixInt.make(1) );
  bmul(MA,2);
  bmul(MI,2);
  bmul(MA,MI);
  bmul(MI,MA);
  bmul(MA/2,3);

  // isOne
  assertTrue( SchemeFixInt.ONE.isOne() );
  assertTrue( SchemeFixInt.make(1).isOne() );
  assertFalse( SchemeFixInt.ZERO.isOne() );
  assertFalse( SchemeFixInt.make(0).isOne() );

  // pow10
  assertEquals(SchemeFixInt.pow10(5), SchemeFixInt.make(100000) );
  {
    SchemeNumber cur = SchemeFixInt.ONE;
    for ( int i = 0; i < 6; ++i )
      cur = cur.mul( SchemeFixInt.make(10));
    assertEquals( cur, SchemeFixInt.pow10(6) );
  }

  // toString()
  assertEquals( SchemeFixInt.make(123456).toString(), "123456" );
}


private SchemeNumber nrat ( long a, long b )
{
  return SchemeRational.make( SchemeFixInt.make(a), SchemeFixInt.make(b) );
}

public void testConv () throws Exception
{
  assertEquals( SchemeFixInt.ONE.add( nrat(1,2) ), nrat(3,2) );
  assertEquals( SchemeFixInt.ONE.sub( nrat(1,2) ), nrat(1,2) );
  assertEquals( SchemeFixInt.ONE.mul( nrat(1,2) ), nrat(1,2) );
  assertEquals( SchemeFixInt.ONE.div( nrat(1,2) ), nrat(2,1) );

  assertEquals( SchemeFixInt.ONE.add(SchemeReal.ONE), SchemeReal.make(2) );
  assertEquals( SchemeFixInt.ONE.sub(SchemeReal.ONE), SchemeReal.make(0) );
  assertEquals( SchemeFixInt.ONE.mul(SchemeReal.ONE), SchemeReal.make(1) );
  assertEquals( SchemeFixInt.ONE.div(SchemeReal.ONE), SchemeReal.make(1) );
}




public static Test suite()
{
  return new TestSuite(SchemeIntegerTest.class);
}

} // SchemeIntegerTest
