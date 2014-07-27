/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.io.Serializable;

public abstract class SchemeNumber implements Serializable
{
private static final long serialVersionUID = 6125278439690237345L;

public static final int RANK_COMPLEX = 4;
public static final int RANK_REAL = 3;
public static final int RANK_RATIONAL = 2;
public static final int RANK_BIGINT = 1;
public static final int RANK_FIXINT = 0;

public abstract int getRank ();

public abstract boolean isExact ();
public abstract int signum ();
public abstract boolean isZero ();
public abstract double toJavaDouble () throws ArithmeticException;

public abstract SchemeInteger toInteger () throws ArithmeticException;

public abstract SchemeNumber toInexact ();
public abstract SchemeNumber toExact ();

public abstract int cmp ( SchemeNumber o );
public abstract SchemeNumber neg () throws ArithmeticException;
public abstract SchemeNumber add ( SchemeNumber o ) throws ArithmeticException;
public abstract SchemeNumber sub ( SchemeNumber o ) throws ArithmeticException;
protected abstract SchemeNumber subr ( SchemeNumber o ) throws ArithmeticException;
public abstract SchemeNumber mul ( SchemeNumber o ) throws ArithmeticException;
public abstract SchemeNumber div ( SchemeNumber o ) throws ArithmeticException;
protected abstract SchemeNumber divr ( SchemeNumber o ) throws ArithmeticException;

public abstract SchemeNumber numerator ();
public abstract SchemeNumber denominator ();

public SchemeNumber realPart () { return this; }
public abstract SchemeNumber imagPart ();

public abstract SchemeNumber expt ( SchemeNumber power );

public abstract String numberToString ( int radix, int precision ) throws SchemeError;
} // class

