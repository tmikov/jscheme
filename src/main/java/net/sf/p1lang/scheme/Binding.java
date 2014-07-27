/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

public abstract class Binding
{
public final Scope scope;
public final Symbol sym;

public Binding ( final Scope scope, final Symbol sym )
{
  this.scope = scope;
  this.sym = sym;
}

} // class

