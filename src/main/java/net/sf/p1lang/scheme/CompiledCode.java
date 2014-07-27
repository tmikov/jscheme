/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

public class CompiledCode
{
final SchemeInterpreter m_interp;
final AST m_code;
final int m_envSize;
private final Object[] m_env;

CompiledCode ( SchemeInterpreter interp, final AST code, final int envSize, final Object[] env )
{
  m_interp = interp;
  m_code = code;
  m_envSize = envSize;
  m_env = env;
}

public Object eval () throws SchemeError
{
  return eval( m_env );
}

Object eval ( Object[] env ) throws SchemeError
{
  return m_code != null ? m_code.evalValue( env, new EvalContext(m_interp, env) ) : Unspec.UNSPEC;
}

} // class

