/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.io.Serializable;

public class Symbol implements Serializable
{
private static final long serialVersionUID = -7271789381965771999L;

public final String name;
public final SymCode code;

public Symbol ( final String name, SymCode code )
{
  if (name == null)
    throw new IllegalArgumentException( "name is null" );
  if (code == null)
    throw new IllegalArgumentException( "code is null" );
  this.name = name;
  this.code = code;
}

@SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
@Override
public boolean equals ( final Object o )
{
  // There can be only one symbol of any given name
  return o == this;
}

@Override
public int hashCode ()
{
  return name.hashCode();
}

@Override
public String toString ()
{
  return name;
}
} // class

