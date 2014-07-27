/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

public class SchemeError extends Exception
{
private static final long serialVersionUID = -6440277421504812043L;

public SchemeError ()
{
}

public SchemeError ( final String message )
{
  super(message);
}

public SchemeError ( final String message, final Throwable cause )
{
  super(message, cause);
}

public SchemeError ( final Throwable cause )
{
  super(cause);
}
} // class

