/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.io.Serializable;

public class Closure implements Serializable
{
private static final long serialVersionUID = 604959204288939224L;

public final Object[] parentEnv;
public final Lambda lambda;

public Closure ( final Object[] parentEnv, final Lambda proc )
{
  this.parentEnv = parentEnv;
  this.lambda = proc;
}

public String toString ()
{
  return "#<closure:("+ lambda +")>";
}
} // class

