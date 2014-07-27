/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.util.Arrays;

public class TopLevelScope extends Scope
{
private static final long serialVersionUID = 1300945170358680611L;

final SchemeInterpreter m_interp;
final Object[] m_env;

TopLevelScope m_macroScope;

TopLevelScope ( SchemeInterpreter interp, int envSize )
{
  super(null, envSize, false);
  m_interp = interp;
  m_env = new Object[envSize];
  Arrays.fill( m_env, Unspec.UNSPEC );
}

void define ( Symbol sym, Object value ) throws SchemeUncheckedError
{
  m_env[bindVar( sym ).index] = value;
}

void defineLambda ( Symbol sym, Lambda proc ) throws SchemeUncheckedError
{
  define( sym, new Closure( m_env, proc ) );
}

final TopLevelScope getMacroScope ()
{
  if (m_macroScope == null)
    m_macroScope = new TopLevelScope(m_interp, m_env.length);
  return m_macroScope;
}

final void setMacroScope ( TopLevelScope macroScope )
{
  m_macroScope = macroScope;
}

TopLevelScope copy ()
{
  TopLevelScope res = new TopLevelScope( m_interp, m_env.length );
  res.putAll( this );
  return res;
}

} // class

