/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

public class TooManyErrors extends RuntimeException
{
private static final long serialVersionUID = -8644987442059312848L;

public TooManyErrors ()
{
  super( "Too many errors" );
}
} // class

