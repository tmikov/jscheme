/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.math.BigInteger;

public abstract class SchemeInteger extends SchemeNumber
{
private static final long serialVersionUID = -5371790448486131117L;

public abstract BigInteger toJavaBigInteger ();
public abstract int toJavaInt () throws ArithmeticException;
public abstract long toJavaLong () throws ArithmeticException;

public abstract boolean isOne ();

/**
 * Truncating division (the normal Java/C99 one):
 * <ul>
 *   <li> 42 / 8 = 5
 *   <li> -42 / -8 = 5
 *   <li> -42 / 8 = -5
 *   <li> 42 / -8 = -5
 * </ul>
 * @param o
 * @return this div o
 * @throws ArithmeticException
 */
public abstract SchemeInteger quotient ( SchemeInteger o ) throws ArithmeticException;
public abstract SchemeInteger quotientr ( SchemeInteger o ) throws ArithmeticException;

/**
 * Truncating remainder (the normal Java/C99 one). Result has the sign of the dividend:
 * <ul>
 *   <li> 42 % 8 = 2
 *   <li> -42 % -8 = -2
 *   <li> -42 % 8 = -2
 *   <li> 42 % -8 = 2
 * </ul>
 * @param o
 * @return this % o
 * @throws ArithmeticException
 */
public abstract SchemeInteger remainder ( SchemeInteger o ) throws ArithmeticException;
public abstract SchemeInteger remainderr ( SchemeInteger o ) throws ArithmeticException;

public abstract SchemeInteger gcd ( SchemeInteger o );

public abstract SchemeInteger neg ();

public abstract int countDecimalDigits ();

/**
 * Convert a digit in bases 2,8,10,16 to int. Note that we don't have to specify the base.
 * @param ch
 * @return
 */
private static final int baseDigitToInt ( char ch )
{
  ch |= 32;
  return ch <= '9' ? ch - '0'  : ch - ('a' - 10);
}

/**
 * Create an appropriate integer to hold the specified number.
 * @param radix
 * @param digits
 * @return
 */
public static SchemeInteger makeInteger ( int radix, char[] digits, int len )
{
  if (radix < 1 || radix > 16 || len <= 0)
    throw new IllegalArgumentException();

  long limit = Long.MAX_VALUE / radix; // maximum long we can multiply by base without overflow
  long val = 0;
  for ( int i = 0; i < len; ++i )
  {
    int digit = baseDigitToInt(digits[i]);
    if (val > limit)
      return SchemeBigInt.makeBigInt( new BigInteger(new String(digits, 0, len)) );
    val *= radix;
    if (val + digit < val)
      return SchemeBigInt.makeBigInt( new BigInteger(new String(digits, 0, len)) );
    val += digit;
  }

  return SchemeFixInt.make( val );
}


/**
 *
 * @param exponent
 * @return
 * @throws ArithmeticException
 */
public static SchemeInteger pow10 ( int exponent ) throws ArithmeticException
{
  if (exponent < 0)
    throw new ArithmeticException();

  if (exponent <= SchemeFixInt.MAX_FIXINT_EXP)
    return SchemeFixInt.s_fixIntPow10[exponent];

  return SchemeBigInt.makeBigInt( BigInteger.TEN.pow( exponent ) );
}

public SchemeNumber numerator ()
{
  return this;
}

public SchemeNumber denominator ()
{
  return SchemeFixInt.ONE;
}

public SchemeNumber toExact ()
{
  return this;
}

public SchemeNumber imagPart ()
{
  return SchemeFixInt.ZERO;
}

public SchemeInteger toInteger () throws ArithmeticException
{
  return this;
}

public SchemeNumber expt ( final SchemeNumber power )
{
  if (power instanceof SchemeInteger)
  {
    SchemeInteger ipower = power.toInteger();

    if (ipower.isZero())
      return SchemeFixInt.ONE;
    if (ipower.signum() > 0)
      return make( toJavaBigInteger().pow( ipower.toJavaInt() ) );
    else
    {
      // Negative power==> 1/x**(-power)
      return SchemeRational.make(
              SchemeFixInt.ONE,
              make(toJavaBigInteger().pow( -ipower.toJavaInt()))
      );
    }
  }
  else
    return toInexact().expt( power );
}

/**
 * Create a fixint or bigint depending on the parameter
 * @param value
 * @return
 */
public static SchemeInteger make ( BigInteger value )
{
  if (value.compareTo(SchemeFixInt.s_biMIN_VALUE) >= 0 &&
      value.compareTo(SchemeFixInt.s_biMAX_VALUE) <= 0)
  {
    return SchemeFixInt.make( value.longValue() );
  }
  else
    return SchemeBigInt.makeBigInt( value );
}
} // class

