/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

public class VarBinding extends Binding
{
public final int index;

public VarBinding ( Scope scope, final Symbol sym, final int index )
{
  super(scope, sym);
  this.index = index;
}

public String toString ()
{
  return "(%VarBinding "+sym+")";
}
} // class

