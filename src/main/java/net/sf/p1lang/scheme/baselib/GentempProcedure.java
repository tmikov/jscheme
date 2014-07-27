/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme.baselib;

import net.sf.p1lang.scheme.JavaProcedure;
import net.sf.p1lang.scheme.SchemeError;
import net.sf.p1lang.scheme.SchemeInterpreter;

public class GentempProcedure extends JavaProcedure
{
private final SchemeInterpreter m_interp;

public GentempProcedure ( final SchemeInterpreter interp )
{
  super(0,false);
  m_interp = interp;
}

public Object apply ( final Object[] argv ) throws SchemeError
{
  return m_interp.gentemp();
}
} // class

