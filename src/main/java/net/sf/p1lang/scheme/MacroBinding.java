/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

public class MacroBinding extends Binding
{
public Closure combination;
public Closure identifier;
public Closure set;

public MacroBinding ( Scope scope, final Symbol sym )
{
  super(scope, sym);
}
} // class

