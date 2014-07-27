/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

public abstract class JavaProcedure extends Lambda
{
private static final long serialVersionUID = -7632683063835068688L;

// We need this for a continuation into this procedure
public final AST cont = new AST(null)
{
  private static final long serialVersionUID = 2720094542654325145L;
  public Object evalValue ( final Object[] env, EvalContext ctx ) throws SchemeError
  {
    return apply( env );
  }

  public Object dis ()
  {
    return toString();
  }
};

public JavaProcedure ( int paramCount, boolean haveRest )
{
  this.paramCount = paramCount;
  this.haveRest = haveRest;
  this.envSize = Scope.RESERVED_SLOTS + paramCount + (haveRest?1:0);
}

public abstract Object apply ( Object[] argv ) throws SchemeError;

public static final int ARG0 = Scope.RESERVED_SLOTS;
} // class

