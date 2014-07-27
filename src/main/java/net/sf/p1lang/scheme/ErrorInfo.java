/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.io.Serializable;

public class ErrorInfo implements Serializable
{
private static final long serialVersionUID = -5783040684517545064L;

public final SourceCoords coords;
public final String message;
public final Throwable cause;

public ErrorInfo ( final ISourceCoords coords, final String message, final Throwable cause )
{
  this.coords = coords != null ? new SourceCoords( coords ) : null;
  this.message = message;
  this.cause = cause;
}

public String formatMessage ()
{
  StringBuilder res = new StringBuilder();
  if (coords != null)
  {
    res.append( coords.fileName != null ? coords.fileName : "<null>");
    res.append( '(' ).append( coords.line ).append(").").append(coords.column );
    res.append( ':' );
  }
  res.append( message );
  if (cause != null)
    res.append( " caused by ").append( cause.getMessage() );
  return res.toString();
}

public String toString ()
{
  return formatMessage();
}

} // class

