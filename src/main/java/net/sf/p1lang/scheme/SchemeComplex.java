/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

// TODO: I am not exactly sure how exactness should be preserved in the different operations,
//      especially mul() and div()

public final class SchemeComplex extends SchemeNumber
{
private static final long serialVersionUID = -1671203506798471099L;

public final SchemeNumber a;
public final SchemeNumber b;

public static final SchemeComplex EXACT_I = new SchemeComplex( SchemeFixInt.ZERO, SchemeFixInt.ONE );
public static final SchemeComplex EXACT_MINUS_I = new SchemeComplex( SchemeFixInt.ZERO, SchemeFixInt.MINUS_ONE );

public static final SchemeComplex INEXACT_I = new SchemeComplex( SchemeReal.ZERO, SchemeReal.ONE );
public static final SchemeComplex INEXACT_MINUS_I = new SchemeComplex( SchemeReal.ZERO, SchemeReal.MINUS_ONE );

private SchemeComplex ( final SchemeNumber a, final SchemeNumber b )
{
  this.a = a;
  this.b = b;
}

public static SchemeNumber make ( SchemeNumber a, SchemeNumber b )
{
  if (b.isZero())
    return a;
  return new SchemeComplex( a, b );
}

public int getRank ()
{
  return RANK_COMPLEX;
}

public boolean isExact ()
{
  return a.isExact() && b.isExact();
}

public int signum ()
{
  if (b.isZero())
    return a.signum();
  throw new ArithmeticException( "Complex number doesn't have a sign" );
}

public final boolean isZero ()
{
  return a.isZero() && b.isZero();
}

public double toJavaDouble () throws ArithmeticException
{
  if (b.isZero())
    return a.toJavaDouble();
  else
    throw new ArithmeticException();
}

public SchemeInteger toInteger () throws ArithmeticException
{
  if (b.isZero())
    return a.toInteger();
  else
    throw new ArithmeticException();
}

public SchemeNumber toInexact ()
{
  if (!a.isExact() && !b.isExact())
    return this;
  return make( a.toInexact(), b.toInexact() );
}

public SchemeNumber toExact ()
{
  if (a.isExact() && b.isExact())
    return this;
  return make( a.toExact(), b.toExact() );
}

public int cmp ( final SchemeNumber o )
{
  // FIXME: implement this properly
  throw new ArithmeticException("Complex cmp");
}

public final SchemeComplex neg () throws ArithmeticException
{
  return new SchemeComplex( a.neg(), b.neg() );
}

public final SchemeNumber add ( final SchemeNumber o ) throws ArithmeticException
{
  if (o.isZero())
    return this;
  if (o instanceof SchemeComplex)
  {
    SchemeComplex x = (SchemeComplex)o;
    return make( a.add( x.a ), b.add( x.b ) );
  }
  else
    return make( a.add( o ), b );
}

public final SchemeNumber sub ( final SchemeNumber o ) throws ArithmeticException
{
  if (o.isZero())
    return this;
  if (o instanceof SchemeComplex)
  {
    SchemeComplex x = (SchemeComplex)o;
    return make( a.sub( x.a ), b.sub( x.b ) );
  }
  else
    return make( a.sub( o ), b );
}

protected SchemeNumber subr ( final SchemeNumber o ) throws ArithmeticException
{
  return make( o.sub( a ), b.neg() );
}

public final SchemeNumber mul ( final SchemeNumber o ) throws ArithmeticException
{
  if (o instanceof SchemeComplex)
  {
    SchemeComplex x = (SchemeComplex)o;
    SchemeNumber c = x.a;
    SchemeNumber d = x.b;
    return make( a.mul(c).sub( b.mul(d) ), b.mul(c).add( a.mul(d) ) );
  }
  else
    return make( a.mul(o), b.mul(o) );
}

public final SchemeNumber div ( final SchemeNumber o ) throws ArithmeticException
{
  if (o instanceof SchemeComplex)
  {
    SchemeComplex x = (SchemeComplex)o;
    SchemeNumber c = x.a;
    SchemeNumber d = x.b;
    SchemeNumber tmp = c.mul(c).add( d.mul(d) );
    return make( a.mul(c).add( b.mul(d) ).div( tmp ), b.mul(c).sub( a.mul(d) ).div( tmp ) );
  }
  else
  {
    SchemeNumber tmp = o.mul(o);
    return make( a.mul(o).div( tmp ), b.mul(o).div( tmp ) );
  }
}

protected SchemeNumber divr ( final SchemeNumber o ) throws ArithmeticException
{
  SchemeNumber tmp = a.mul(a).add( b.mul(b) );
  return make( o.mul(a).div( tmp ), o.mul(b).neg().div( tmp ) );
}

public SchemeNumber numerator ()
{
  throw new ArithmeticException();
}

public SchemeNumber denominator ()
{
  throw new ArithmeticException();
}

public SchemeNumber realPart ()
{
  return a;
}

public SchemeNumber imagPart ()
{
  return b;
}

public SchemeNumber expt ( final SchemeNumber power )
{
  if (b.isZero())
    return a.expt( power );
  throw new ArithmeticException( "Complex power not implemented" ); // FIXME: implement
}

// FIXME: implement the spec precisely
public String numberToString ( final int radix, final int precision ) throws SchemeError
{
  StringBuilder res = new StringBuilder();
  res.append( a.numberToString(radix, precision) );
  res.append( '+' );
  res.append( b.numberToString(radix, precision) );
  res.append( 'i' );
  return res.toString();
}

public boolean equals ( final Object o )
{
  if (this == o)
    return true;
  if (!(o instanceof SchemeComplex))
    return false;

  SchemeComplex that = (SchemeComplex) o;
  return a.equals(that.a) && b.equals(that.b);
}

public int hashCode ()
{
  int result;
  result = a.hashCode();
  result = 31 * result + b.hashCode();
  return result;
}

@Override
public String toString ()
{
  StringBuilder buf = new StringBuilder(a.toString());
  if (b.signum() >= 0)
    buf.append('+').append(b.toString());
  else
    buf.append( b.toString() );
  return buf.append('i').toString();
}
} // class

