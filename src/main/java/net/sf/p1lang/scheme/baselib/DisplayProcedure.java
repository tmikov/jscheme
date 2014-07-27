/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme.baselib;

import java.io.PrintWriter;

import net.sf.p1lang.scheme.JavaProcedure;
import net.sf.p1lang.scheme.Unspec;

public class DisplayProcedure extends JavaProcedure
{
private static final long serialVersionUID = 138846740037408326L;

private transient final PrintWriter out = new PrintWriter( System.out );

public DisplayProcedure ( )
{
  super(1, false);
}

public Object apply ( final Object[] argv )
{
  //Object[] rootEnv = (Object[]) argv[0];

  Object arg = argv[ARG0];
  out.print( arg );
//  DatumParser.printDatum( out, arg );
  out.flush();
  return Unspec.UNSPEC;
}

} // class

