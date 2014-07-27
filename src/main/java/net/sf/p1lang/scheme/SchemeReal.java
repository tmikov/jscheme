/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class SchemeReal extends SchemeNumber
{
private static final long serialVersionUID = -8005972525450398963L;

public static final SchemeReal ZERO = new SchemeReal( 0 );
public static final SchemeReal ONE = new SchemeReal( 1 );
public static final SchemeReal MINUS_ONE = new SchemeReal( -1 );

public static final SchemeReal NAN = new SchemeReal( Double.NaN );
public static final SchemeReal POS_NAN = NAN;
public static final SchemeReal NEG_NAN = NAN;
public static final SchemeReal POS_INF = new SchemeReal( Double.POSITIVE_INFINITY );
public static final SchemeReal NEG_INF = new SchemeReal( Double.NEGATIVE_INFINITY );

public final double value;

private SchemeReal ( final double value )
{
  this.value = value;
}

public static SchemeReal make ( double val )
{
  if (val == 0)
    return ZERO;
  else if (val == 1)
    return ONE;
  else if (val == -1)
    return MINUS_ONE;
  else
  //noinspection ConstantConditions
  if (val != val)
    return NAN;
  else if (val == Double.POSITIVE_INFINITY)
    return POS_INF;
  else if (val == Double.NEGATIVE_INFINITY)
    return NEG_INF;
  else
    return new SchemeReal( val );
}

public static SchemeReal valueOf ( String str ) throws NumberFormatException
{
  return make( Double.parseDouble( str ) );
}

public final int getRank ()
{
  return RANK_REAL;
}

public boolean isExact ()
{
  return false;
}

public int signum ()
{
  return Double.compare( value, 0 );
}

public final boolean isZero ()
{
  return value == 0;
}

public double toJavaDouble ()
{
  return value;
}

public SchemeInteger toInteger () throws ArithmeticException
{
  long lv = (long)value;
  if (lv == value)
    return SchemeFixInt.make( lv );
  else if (Math.floor( value ) != value)
    throw new ArithmeticException();
  else
  {
    // TODO: there should be a faster (and more accurate?) way to do this
    return SchemeBigInt.makeBigInt( new BigDecimal( value ).toBigInteger() );
  }
}

public SchemeNumber toInexact ()
{
  return this;
}

public static final long REAL_EXP_BIT_MASK = 0x7FF0000000000000L;
public static final int  REAL_EXP_BIAS = 1023;
public static final int  REAL_MANTISSA_WIDTH = 52;
public static final long REAL_MANTISSA_BIT_MASK = 0x000FFFFFFFFFFFFFL;
public static final long REAL_SIGN_BIT_MASK = 0x8000000000000000L;

public SchemeNumber toExact ()
{
  long db = Double.doubleToRawLongBits(value);
  int exp;
  long mant;

  if ((db & ~REAL_SIGN_BIT_MASK) == 0)
  {
    return SchemeFixInt.ZERO;
  }
  else
  {
    // biased exponent
    mant = (db & REAL_MANTISSA_BIT_MASK);

    if ((db & REAL_EXP_BIT_MASK) == REAL_EXP_BIT_MASK)
    {
      if (mant == 0) // +/- INF
        return db >= 0 ? SchemeRational.POS_INF : SchemeRational.NEG_INF;
      else // NaN
        return SchemeRational.NAN;
    }
    else if ((db & REAL_EXP_BIT_MASK) != 0)
    {
      // Normalized number
      exp = (int)((db & REAL_EXP_BIT_MASK) >> REAL_MANTISSA_WIDTH) - REAL_EXP_BIAS -
            REAL_MANTISSA_WIDTH;
      mant += 1L << REAL_MANTISSA_WIDTH; // add the implicit 1
    }
    else
    {
      // if (mant == 0) ... // zero, but we checked earlier

      // denormalized number
      exp = -REAL_EXP_BIAS +1- REAL_MANTISSA_WIDTH;
    }

    // Normalize
    // note1: we checked earlier for zero, so this is safe)
    // note2: we explicitly want to stop if the exponent already is positive
    while (exp < 0 && (mant & 1) == 0)
    {
      mant >>= 1;
      ++exp;
    }

    if (db < 0)
      mant = -mant;
  }

  BigInteger biNum = BigInteger.valueOf( mant );

  if (exp >= 0)
  {
    if (exp > 0)
      biNum = biNum.shiftLeft( exp );
    return SchemeInteger.make( biNum );
  }
  else
  {
    BigInteger biDenom = BigInteger.ONE.shiftLeft(-exp);
    return SchemeRational.make(SchemeInteger.make(biNum), SchemeInteger.make(biDenom));
  }
}

public int cmp ( final SchemeNumber o )
{
  if (o.getRank() > RANK_REAL)
    return -o.cmp(this);
  return Double.compare( value, o.toJavaDouble() );
}

public final SchemeReal neg ()
{
  return value != 0 ? make( -value ) : this;
}

public SchemeNumber add ( final SchemeNumber o ) throws ArithmeticException
{
  if (o.getRank() > RANK_REAL)
    return o.add( this );
  return make( value+o.toJavaDouble() );
}

public SchemeNumber sub ( final SchemeNumber o ) throws ArithmeticException
{
  if (o.getRank() > RANK_REAL)
    return o.subr( this );
  return make( value-o.toJavaDouble() );
}

protected SchemeNumber subr ( final SchemeNumber o ) throws ArithmeticException
{
  return make( o.toJavaDouble() - value );
}

public SchemeNumber mul ( final SchemeNumber o ) throws ArithmeticException
{
  if (o.getRank() > RANK_REAL)
    return o.mul( this );
  return make( value*o.toJavaDouble() );
}

public SchemeNumber div ( final SchemeNumber o ) throws ArithmeticException
{
  if (o.getRank() > RANK_REAL)
    return o.divr( this );
  return make( value/o.toJavaDouble() );
}

protected SchemeNumber divr ( final SchemeNumber o ) throws ArithmeticException
{
  return make( o.toJavaDouble() / value );
}

public SchemeNumber numerator ()
{
  // TODO: inefficient ?
  return toExact().numerator().toInexact();
}

public SchemeNumber denominator ()
{
  // TODO: inefficient ?
  return toExact().denominator().toInexact();
}

public SchemeNumber imagPart ()
{
  return ZERO;
}

public SchemeNumber expt ( final SchemeNumber power )
{
  if (power.getRank() == SchemeNumber.RANK_COMPLEX)
    throw new ArithmeticException( "Complex power not implemented" ); // FIXME: implement
  return make( Math.pow( value, power.toJavaDouble() ) );
}

// FIXME: implement the spec precisely
public String numberToString ( final int radix, final int precision ) throws SchemeError
{
  if (radix != 10)
    throw new SchemeError( "number->string: invalid radix for inexact number" );
  return Double.toString( this.value );
}

public boolean equals ( final Object o )
{
  return this == o || o instanceof SchemeReal && Double.compare(((SchemeReal)o).value, value) == 0;
}

public int hashCode ()
{
  long temp = value != +0.0d ? Double.doubleToLongBits(value) : 0L;
  return (int) (temp ^ (temp >>> 32));
}

@Override
public String toString ()
{
  return Double.toString(value);
}

} // class

