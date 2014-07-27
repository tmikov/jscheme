/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme.baselib;

import net.sf.p1lang.scheme.JavaProcedure;
import net.sf.p1lang.scheme.SchemeError;
import net.sf.p1lang.scheme.SchemeNumber;
import net.sf.p1lang.scheme.Pair;

public class NumberToStringProcedure extends JavaProcedure
{
private static final long serialVersionUID = 5363819592708980463L;

public NumberToStringProcedure ()
{
  super(1, true);
}

// FIXME: implement the spec precisely
public Object apply ( final Object[] argv ) throws SchemeError
{
  SchemeNumber n = (SchemeNumber)argv[ARG0];
  Pair p = (Pair)argv[ARG0+1];
  int radix = 10;
  int precision = -1;

  if (p != Pair.NULL)
  {
    int t = ((SchemeNumber)p.getCar()).toInteger().toJavaInt();
    switch (t)
    {
    case 2: case 8: case 10: case 16:
      radix = t;
      break;
    default:
      throw new SchemeError( "number->string: invalid radix "+ t );
    }
    p = (Pair)p.getCdr();
  }

  if (p != Pair.NULL)
  {
    int t = ((SchemeNumber)p.getCar()).toInteger().toJavaInt();
    if (t < 0)
      throw new SchemeError( "number->string: invalid precision "+ t );
    p = (Pair)p.getCdr();
  }

  if (p != Pair.NULL)
    throw new SchemeError( "number->string: more than 3 parameters" );

  if (radix != 10 && !n.isExact())
    throw new SchemeError( "number->string: invalid radix for inexact number" );

  return n.numberToString( radix, precision );
}
} // class

