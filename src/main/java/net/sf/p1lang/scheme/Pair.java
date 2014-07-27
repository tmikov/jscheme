/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.io.Serializable;
import java.io.ObjectStreamException;

/**
 * A Scheme pair.
 */
public class Pair implements Serializable
{
private static final long serialVersionUID = -2718489010559544180L;

public static final Pair NULL = new Pair(){
  private static final long serialVersionUID = 4999460607958811756L;
  private Object readResolve() throws ObjectStreamException
  {
    return NULL;
  }
};

private Object m_car;
private Object m_cdr;

private Pair ()
{
  m_car = m_cdr = null;
}

public Pair ( final Object car, final Object cdr )
{
  assert( car != null && cdr != null );
  this.m_car = car;
  this.m_cdr = cdr;
}
public Pair ( final Object car )
{
  this( car, NULL );
}

public String toString ()
{
  StringBuilder res = new StringBuilder();
  res.append('(');
  Pair p = this;
  while (p != NULL)
  {
    res.append( p.getCar().toString() );
    if (p.getCdr() instanceof Pair)
    {
      p = (Pair) p.getCdr();
      res.append( ' ' );
    }
    else
    {
      res.append( " . " );
      res.append(p.getCdr());
      break;
    }
  }
  res.append(')');
  return res.toString();
}

public final void setCarBang ( Object car )
{
  if (car == null)
    throw new IllegalArgumentException("null");
  if (this == NULL)
    throw new IllegalArgumentException("(set-car! null ...)");
  this.m_car = car;
}

public final void setCdrBang ( Object cdr )
{
  if (cdr == null)
    throw new IllegalArgumentException();
  if (this == NULL)
    throw new IllegalArgumentException("(set-cdr! null ...)");
  this.m_cdr = cdr;
}

public final Object getCar ()
{
  // use an assertion because this is an extremely frequent operation and we don't want to
  // slow it down in release
  assert( this != NULL );
  return m_car;
}

public final Object getCdr ()
{
  // use an assertion because this is an extremely frequent operation and we don't want to
  // slow it down in release
  assert( this != NULL );
  return m_cdr;
}
} // class

