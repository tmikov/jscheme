/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme.baselib;

import net.sf.p1lang.scheme.JavaProcedure;
import net.sf.p1lang.scheme.SchemeError;
import net.sf.p1lang.scheme.Pair;

public class StringAppendProcedure extends JavaProcedure
{
private static final long serialVersionUID = -5864187428601616844L;

public StringAppendProcedure ()
{
  super(1, true);
}

public Object apply ( final Object[] argv ) throws SchemeError
{
  StringBuilder res = new StringBuilder( (String)argv[ARG0] );
  for ( Pair p = (Pair)argv[ARG0+1]; p != Pair.NULL; p = (Pair)p.getCdr() )
    res.append( (String)(p.getCar()) );
  return res.toString();
}

} // class

