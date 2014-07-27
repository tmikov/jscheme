/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.math.BigInteger;

public final class SchemeBigInt extends SchemeInteger
{
private static final long serialVersionUID = -4597969288068484935L;

public final BigInteger value;

private SchemeBigInt ( final BigInteger value )
{
  this.value = value;
}

public static SchemeBigInt makeBigInt ( BigInteger value )
{
  return new SchemeBigInt( value );
}

public BigInteger toJavaBigInteger ()
{
  return value;
}

static final BigInteger s_biMIN_JAVA_INT = BigInteger.valueOf(Integer.MIN_VALUE);
static final BigInteger s_biMAX_JAVA_INT = BigInteger.valueOf(Integer.MAX_VALUE);

static final BigInteger s_biMIN_JAVA_LONG = BigInteger.valueOf(Long.MIN_VALUE);
static final BigInteger s_biMAX_JAVA_LONG = BigInteger.valueOf(Long.MAX_VALUE);

public int toJavaInt () throws ArithmeticException
{
  if (value.compareTo(s_biMIN_JAVA_INT) >= 0 &&
      value.compareTo(s_biMAX_JAVA_INT) <= 0)
  {
    return value.intValue();
  }
  throw new ArithmeticException();
}

public long toJavaLong () throws ArithmeticException
{
  if (value.compareTo(s_biMIN_JAVA_LONG) >= 0 &&
      value.compareTo(s_biMAX_JAVA_LONG) <= 0)
  {
    return value.longValue();
  }
  throw new ArithmeticException();
}

public SchemeInteger quotient ( final SchemeInteger o ) throws ArithmeticException
{
  if (o.getRank() > RANK_BIGINT)
    return o.quotientr( this );
  return make( value.divide( o.toJavaBigInteger() ) );
}

public SchemeInteger quotientr ( final SchemeInteger o ) throws ArithmeticException
{
  return make( o.toJavaBigInteger().divide( value ) );
}

public SchemeInteger remainder ( final SchemeInteger o ) throws ArithmeticException
{
  if (o.getRank() > RANK_BIGINT)
    return o.quotientr( this );
  return make( value.remainder( o.toJavaBigInteger() ) );
}

public SchemeInteger remainderr ( final SchemeInteger o ) throws ArithmeticException
{
  return make( o.toJavaBigInteger().remainder( value ) );
}

public SchemeInteger gcd ( final SchemeInteger o )
{
  if (o.getRank() > RANK_BIGINT)
    return o.gcd( this );
  return make( value.gcd( o.toJavaBigInteger() ) );
}

public int getRank ()
{
  return RANK_BIGINT;
}

public boolean isExact ()
{
  return true;
}

public int signum ()
{
  return value.signum();
}

public boolean isZero ()
{
  return value.signum() == 0;
}

public final boolean isOne ()
{
  return value.equals(BigInteger.ONE);
}

public double toJavaDouble () throws ArithmeticException
{
  return value.doubleValue();
}

public SchemeNumber toInexact ()
{
  return SchemeReal.make( toJavaDouble() );
}

public int cmp ( final SchemeNumber o )
{
  if (o.getRank() > RANK_BIGINT)
    return -o.cmp(this);
  return value.compareTo( ((SchemeInteger)o).toJavaBigInteger() );
}

public SchemeInteger neg () throws ArithmeticException
{
  if (isZero())
    return SchemeFixInt.ZERO;
  else
    return makeBigInt( value.negate() );
}

public SchemeNumber add ( final SchemeNumber o ) throws ArithmeticException
{
  if (o.getRank() > RANK_BIGINT)
    return o.add( this );
  return makeBigInt( value.add( ((SchemeInteger)o).toJavaBigInteger() ) );
}

public SchemeNumber sub ( final SchemeNumber o ) throws ArithmeticException
{
  if (o.getRank() > RANK_BIGINT)
    return o.subr( this );
  return makeBigInt( value.subtract( ((SchemeInteger)o).toJavaBigInteger() ) );
}

protected SchemeNumber subr ( final SchemeNumber o ) throws ArithmeticException
{
  return makeBigInt( ((SchemeInteger)o).toJavaBigInteger().subtract(value) );
}

public SchemeNumber mul ( final SchemeNumber o ) throws ArithmeticException
{
  if (o.getRank() > RANK_BIGINT)
    return o.mul( this );
  if (((SchemeInteger)o).isOne())
    return this;
  return makeBigInt( value.multiply( ((SchemeInteger)o).toJavaBigInteger() ) );
}

public SchemeNumber div ( final SchemeNumber o ) throws ArithmeticException
{
  if (o.getRank() > RANK_BIGINT)
    return o.divr( this );
  return SchemeRational.make( this, (SchemeInteger)o );
}

protected SchemeNumber divr ( final SchemeNumber o ) throws ArithmeticException
{
  return SchemeRational.make( (SchemeInteger)o, this );
}

// FIXME: implement the spec precisely
public String numberToString ( final int radix, final int precision ) throws SchemeError
{
  return this.value.toString( radix );
}

public boolean equals ( final Object o )
{
  return this == o || o instanceof SchemeBigInt && value.equals(((SchemeBigInt) o).value);
}

public int hashCode ()
{
  return value.hashCode();
}

public String toString ()
{
  return value.toString();
}

private static final double LOG10_2 = 0.3010299956639812;
public int countDecimalDigits ()
{
  // TODO: I am not quite sure how accurate this is
  return (int)Math.ceil( value.bitLength() * LOG10_2 );
}
} // class

