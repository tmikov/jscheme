/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.math.BigInteger;
import java.io.Serializable;

public final class SchemeFixInt extends SchemeInteger implements Serializable
{
private static final long serialVersionUID = -3835958958822950584L;

public final long value;

static final BigInteger s_biMIN_VALUE = BigInteger.valueOf(Long.MIN_VALUE);
static final BigInteger s_biMAX_VALUE = BigInteger.valueOf(Long.MAX_VALUE);

private static final int MIN_PREALLOC = -100;
private static final int MAX_PREALLOC = 1000;
private static final SchemeFixInt[] s_prealloc = new SchemeFixInt[MAX_PREALLOC-MIN_PREALLOC+1];
static {
  for ( int i = MIN_PREALLOC; i <= MAX_PREALLOC; ++i )
    s_prealloc[i-MIN_PREALLOC] = new SchemeFixInt(i);
}

public static final SchemeFixInt ZERO = make(0);
public static final SchemeFixInt ONE = make(1);
public static final SchemeFixInt MINUS_ONE = make(-1);


static final int MAX_FIXINT_EXP = 18;
static final SchemeFixInt[] s_fixIntPow10 = new SchemeFixInt[MAX_FIXINT_EXP+1];

static {
  long val = 1;
  for ( int i = 0; i <= MAX_FIXINT_EXP; ++i )
  {
    assert( val > 0 );
    s_fixIntPow10[i] = SchemeFixInt.make( val );
    val *= 10;
  }
}

private SchemeFixInt ( final long value )
{
  this.value = value;
}

public BigInteger toJavaBigInteger ()
{
  return BigInteger.valueOf(value);
}

public int toJavaInt () throws ArithmeticException
{
  if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE)
    throw new ArithmeticException();
  return (int)value;
}

public long toJavaLong () throws ArithmeticException
{
  return value;
}

public final boolean equals ( final Object o )
{
  return this == o || o instanceof SchemeFixInt && value == ((SchemeFixInt)o).value;
}

public int hashCode ()
{
  return (int) (value ^ (value >>> 32));
}

public final int signum ()
{
  long v = value;
  return v < 0 ? -1 : v == 0 ? 0 : +1;
}

public int getRank ()
{
  return RANK_FIXINT;
}

public boolean isExact ()
{
  return true;
}

public final boolean isZero ()
{
  return value == 0;
}

public double toJavaDouble () throws ArithmeticException
{
  return value;
}

public SchemeNumber toInexact ()
{
  return SchemeReal.make( value );
}

public int cmp ( final SchemeNumber o )
{
  if (o.getRank() > RANK_FIXINT)
    return -o.cmp( this );
  long a = value;
  long b = ((SchemeFixInt)o).value;
  return a < b ? -1 : a == b ? 0 : +1;
}

public final SchemeInteger neg ()  throws ArithmeticException
{
  long x = value;
  if (x == 0)
    return this;
  if (x != -x) // check for overflow. -MIN_INT==MIN_INT
    return make( -x );
  else
    return SchemeBigInt.makeBigInt(BigInteger.valueOf(x).negate());
}

public int countDecimalDigits ()
{
  // TODO: there must be a much better way to do this. A binary decision tree ?
  if (value == 0)
    return 0;
  if (value == Long.MIN_VALUE) // Cant calc abs(Long.MIN_VALUE)
    return 19;
  return (int)Math.ceil(Math.log10(Math.abs(value)));
}

public SchemeNumber add ( final SchemeNumber o ) throws ArithmeticException
{
  if (o.getRank() > RANK_FIXINT)
    return o.add( this );

  // We know there is no lesser rank than us, so we optimize a little
  long a = value;
  long b = ((SchemeFixInt)o).value;
  if (b >= 0 && a + b < a || b < 0 && a + b > a)
    return SchemeBigInt.makeBigInt(BigInteger.valueOf(a).add(BigInteger.valueOf(b)));
  else
    return make( a + b );
}

public SchemeNumber sub ( final SchemeNumber o ) throws ArithmeticException
{
  if (o.getRank() > RANK_FIXINT)
    return o.subr( this );

  // We know there is no lesser rank than us, so we optimize a little
  long a = value;
  long b = ((SchemeFixInt)o).value;
  if (b >= 0 && a - b > a || b < 0 && a - b < a)
    return SchemeBigInt.makeBigInt(BigInteger.valueOf(a).subtract(BigInteger.valueOf(b)));
  else
    return make( a - b );
}

protected SchemeNumber subr ( final SchemeNumber o ) throws ArithmeticException
{
  // We know there is no lesser rank than us, so we optimize a little
  long a = ((SchemeFixInt)o).value;
  long b = value;
  if (b >= 0 && a - b > a || b < 0 && a - b < a)
    return SchemeBigInt.makeBigInt(BigInteger.valueOf(a).subtract(BigInteger.valueOf(b)));
  else
    return make( a - b );
}

public SchemeNumber mul ( final SchemeNumber o ) throws ArithmeticException
{
  if (o.getRank() > RANK_FIXINT)
    return o.mul( this );

  // We know there is no lesser rank than us, so we optimize a little
  long a = value;
  long b = ((SchemeFixInt)o).value;
  if (a == 0 || b == 0)
    return ZERO;
  if (b == 1)
    return this;

  boolean ovf = false;

  if (a > 0 && b > 0)
  {
    if (b > Long.MAX_VALUE / a) ovf = true;
  }
  else if (a > 0 && b < 0)
  {
    if (b < Long.MIN_VALUE / a) ovf = true;
  }
  else if (a < 0 && b > 0)
  {
    if (a < Long.MIN_VALUE / b) ovf = true;
  }
  else
  {
    if (b < Long.MAX_VALUE / a) ovf = true;
  }

  if (ovf)
    return SchemeBigInt.makeBigInt(BigInteger.valueOf(a).multiply(BigInteger.valueOf(b)));
  else
    return make( a * b );
}

public SchemeNumber div ( final SchemeNumber o ) throws ArithmeticException
{
  if (o.getRank() > RANK_FIXINT)
    return o.divr( this );

  // We know there is no lesser rank than us, so we optimize a little
  return SchemeRational.make( this, (SchemeFixInt)o );
}

protected SchemeNumber divr ( final SchemeNumber o ) throws ArithmeticException
{
  return SchemeRational.make( (SchemeFixInt)o, this );
}

// FIXME: implement the spec precisely
public String numberToString ( final int radix, final int precision ) throws SchemeError
{
  return Long.toString( this.value, radix );
}

public final boolean isOne ()
{
  return value == 1;
}

/**
 * Return a non-negative GCD.
 *
 */
public final SchemeInteger gcd ( SchemeInteger o )
{
  if (o.getRank() > RANK_FIXINT)
    return o.gcd( this );

  long a = value;
  long b = ((SchemeFixInt)o).value;

  if (a < 0)
    a = -a;
  if (b < 0)
    b = -b;
  if (a < b)
  {
    long t = a;
    a = b;
    b = t;
  }

  while (b != 0)
  {
    long t = b;
    b = a % b;
    a = t;
  }

  return make( a );
}

public SchemeInteger quotient ( SchemeInteger o ) throws ArithmeticException
{
  if (o.getRank() > RANK_FIXINT)
    return o.quotientr( this );

  return make( value / ((SchemeFixInt)o).value );
}

public SchemeInteger quotientr ( SchemeInteger o ) throws ArithmeticException
{
  return make( ((SchemeFixInt)o).value / value );
}

public SchemeInteger remainder ( final SchemeInteger o ) throws ArithmeticException
{
  if (o.getRank() > RANK_FIXINT)
    return o.quotientr( this );

  return make( value % ((SchemeFixInt)o).value );
}

public SchemeInteger remainderr ( final SchemeInteger o ) throws ArithmeticException
{
  return make( ((SchemeFixInt)o).value % value );
}

public static SchemeFixInt make ( long val )
{
  if (val >= MIN_PREALLOC && val <= MAX_PREALLOC)
    return s_prealloc[(int)(val)-MIN_PREALLOC];
  else
    return new SchemeFixInt( val );
}

@Override
public String toString ()
{
  return Long.toString(value);
}

} // class

