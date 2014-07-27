/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

public class EvalError extends PositionedError
{
private static final long serialVersionUID = -7657971301936261632L;

public EvalError ( SourceCoords coords, final String message )
{
  super(coords, message);
}

public EvalError ( final String message )
{
  super(message);
}

public EvalError ( Pair at, String message )
{
  super(at, message);
}

public EvalError ( AST at, String message )
{
  this( at.havePosition() ? new SourceCoords(at.getFileName(), at.getLine(), at.getColumn()) : null,
        message );
}
} // class

