/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.math.BigDecimal;
import java.math.MathContext;

public final class SchemeRational extends SchemeNumber
{
private static final long serialVersionUID = -9049947265636491142L;

public final SchemeInteger numerator; // expresses the sign, so could be negative
public final SchemeInteger denominator; // always non-negative

public static final SchemeRational NAN = new SchemeRational( SchemeFixInt.ZERO, SchemeFixInt.ZERO );
public static final SchemeRational POS_INF = new SchemeRational( SchemeFixInt.ONE, SchemeFixInt.ZERO );
public static final SchemeRational NEG_INF = new SchemeRational( SchemeFixInt.MINUS_ONE, SchemeFixInt.ZERO );

private SchemeRational ( final SchemeInteger numerator, final SchemeInteger denominator )
{
  this.numerator = numerator;
  this.denominator = denominator;
}

public static SchemeNumber make ( SchemeInteger num, SchemeInteger denom ) throws ArithmeticException
{
  if (num.isZero())      // 0/x; 0/0 = NaN, 0/x = 0
    return denom.isZero() ? NAN : SchemeFixInt.ZERO;

  if (denom.isZero())   // x/0 -> +/-inf
    return num.signum() >= 0 ? POS_INF : NEG_INF;

  // We need the denominator to be positive and the numerator to express the sign
  if (denom.signum() < 0)
  {
    num = num.neg();
    denom = denom.neg();
  }

  if (denom.isOne())
    return num;

  SchemeInteger gcd = num.gcd( denom );

  if (!gcd.isOne())
  {
    num = num.quotient( gcd );
    denom = denom.quotient( gcd );
  }

  if (denom.isOne())
    return num;
  else
    return new SchemeRational( num, denom );
}

public final int getRank ()
{
  return RANK_RATIONAL;
}

public boolean isExact ()
{
  return true;
}

public int signum ()
{
  return numerator.signum();
}

public final boolean isZero ()
{
  // This function can't really be invoked becase make() would have just returned a simple integer 0
  return numerator.isZero();
}


public final double toJavaDouble () throws ArithmeticException
{
  // Fast path if both numbers can fit into double's mantissa.
  //
  // I really don't understand this well mathematically, but operations with values
  // outside of the double range are unlikely to produce useful double resules anyway.
  if (numerator instanceof SchemeFixInt && denominator instanceof SchemeFixInt)
  {
    long n = ((SchemeFixInt) numerator).value;
    long d = ((SchemeFixInt) denominator).value;

    if (n >= -SchemeReal.REAL_MANTISSA_BIT_MASK && n <= SchemeReal.REAL_MANTISSA_BIT_MASK &&
        d >= -SchemeReal.REAL_MANTISSA_BIT_MASK && d <= SchemeReal.REAL_MANTISSA_BIT_MASK)
      return (double)n / (double)d;
  }

  // The much slower path
  return
    new BigDecimal(numerator.toJavaBigInteger())
            .divide(new BigDecimal(denominator.toJavaBigInteger()), MathContext.DECIMAL64)
              .doubleValue();
}

public SchemeInteger toInteger () throws ArithmeticException
{
  if (denominator.isOne())
    return numerator; // this shouldn't really happen
  else
    throw new ArithmeticException();
}

public SchemeNumber toInexact ()
{
  return SchemeReal.make( toJavaDouble() );
}

public SchemeNumber toExact ()
{
  return this;
}

public int cmp ( final SchemeNumber o )
{
  if (o.getRank() > RANK_RATIONAL)
    return -o.cmp(this);

  // Note: we know that denominators are always positive, so they can't reverse the comparison

  SchemeInteger a = numerator;
  SchemeInteger b = denominator;

  if (o instanceof SchemeRational)
  {
    SchemeRational x = (SchemeRational)o;
    SchemeInteger c = x.numerator;
    SchemeInteger d = x.denominator;
    return a.mul(d).cmp( b.mul(c) );
  }
  else
    return a.cmp( b.mul( o.toInteger() ) );
}

public final SchemeRational neg () throws ArithmeticException
{
  return new SchemeRational( numerator.neg(), denominator );
}

public SchemeNumber add ( final SchemeNumber o ) throws ArithmeticException
{
  if (o.getRank() > RANK_RATIONAL)
    return o.add( this );
  if (o instanceof SchemeRational)
  {
    SchemeRational x = (SchemeRational)o;
    return
      make( numerator.mul(x.denominator).add( denominator.mul(x.numerator) ).toInteger(),
            denominator.mul(x.denominator).toInteger() );
  }
  else
    return make( numerator.add(denominator.mul(o.toInteger())).toInteger(), denominator );
}

public SchemeNumber sub ( final SchemeNumber o ) throws ArithmeticException
{
  if (o.getRank() > RANK_RATIONAL)
    return o.subr( this );
  if (o instanceof SchemeRational)
  {
    SchemeRational x = (SchemeRational)o;
    return
      make( numerator.mul(x.denominator).sub( denominator.mul(x.numerator) ).toInteger(),
            denominator.mul(x.denominator).toInteger() );
  }
  else
    return make( numerator.sub(denominator.mul(o.toInteger())).toInteger(), denominator );
}

protected SchemeNumber subr ( final SchemeNumber o ) throws ArithmeticException
{
  return make( denominator.mul(o.toInteger()).sub(numerator).toInteger(), denominator );
}

public SchemeNumber mul ( final SchemeNumber o ) throws ArithmeticException
{
  if (o.getRank() > RANK_RATIONAL)
    return o.mul( this );
  if (o instanceof SchemeRational)
  {
    SchemeRational x = (SchemeRational)o;
    return
      make( numerator.mul(x.numerator).toInteger(), denominator.mul(x.denominator).toInteger() );
  }
  else
    return make( numerator.mul(o.toInteger()).toInteger(), denominator );
}

public SchemeNumber div ( final SchemeNumber o ) throws ArithmeticException
{
  if (o.getRank() > RANK_RATIONAL)
    return o.divr( this );
  if (o instanceof SchemeRational)
  {
    SchemeRational x = (SchemeRational)o;
    return
      make( numerator.mul(x.denominator).toInteger(), denominator.mul(x.numerator).toInteger() );
  }
  else
    return make( numerator, denominator.mul(o.toInteger()).toInteger() );
}

protected SchemeNumber divr ( final SchemeNumber o ) throws ArithmeticException
{
  return make( denominator.mul(o.toInteger()).toInteger(), numerator );
}

public SchemeNumber numerator ()
{
  return numerator;
}

public SchemeNumber denominator ()
{
  return denominator;
}

public SchemeNumber imagPart ()
{
  return SchemeFixInt.ZERO;
}

public SchemeNumber expt ( SchemeNumber power )
{
  if (power instanceof SchemeInteger)
  {
    if (power.isZero())
      return SchemeFixInt.ONE;
    if (power.signum() > 0)
      return make( numerator.expt( power ).toInteger(), denominator.expt( power ).toInteger() );
    else
    {
      power = power.neg();
      return make( denominator.expt( power ).toInteger(), numerator.expt( power ).toInteger() );
    }
  }
  else
    return toInexact().expt( power );
}

// FIXME: implement the spec precisely
public String numberToString ( final int radix, final int precision ) throws SchemeError
{
  StringBuilder res = new StringBuilder();
  res.append( numerator.numberToString(radix, precision) );
  res.append( '/' );
  res.append( denominator.numberToString(radix, precision) );
  return res.toString();
}

public boolean equals ( final Object o )
{
  if (this == o)
    return true;
  if (!(o instanceof SchemeRational))
    return false;

  SchemeRational that = (SchemeRational) o;
  return denominator.equals(that.denominator) && numerator.equals(that.numerator);
}

public int hashCode ()
{
  int result;
  result = numerator.hashCode();
  result = 31 * result + denominator.hashCode();
  return result;
}

@Override
public String toString ()
{
  return numerator.toString()+"/"+denominator.toString();
}

} // class

