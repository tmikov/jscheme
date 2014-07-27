/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

public class SchemeUncheckedError extends RuntimeException
{
private static final long serialVersionUID = -2761368194883166189L;

public SchemeUncheckedError ()
{
}

public SchemeUncheckedError ( final String message )
{
  super(message);
}

public SchemeUncheckedError ( final String message, final Throwable cause )
{
  super(message, cause);
}

public SchemeUncheckedError ( final Throwable cause )
{
  super(cause);
}
} // class

