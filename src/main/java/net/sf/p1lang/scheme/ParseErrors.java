/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

public class ParseErrors extends SchemeError
{
private static final long serialVersionUID = -5706872727021567892L;

public transient final IErrorReporter errors;

public ParseErrors ( final IErrorReporter errors )
{
  this.errors = errors;
}

} // class

