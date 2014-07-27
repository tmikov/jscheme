/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Used to signify the "unspecified" value returned from void procedures and so on.
 */
public final class Unspec implements Serializable
{
private static final long serialVersionUID = 7977103658129707871L;

public static final Unspec UNSPEC = new Unspec();

private Unspec () {};

@Override
public String toString ()
{
  return "#<unspecified>";
}

private Object readResolve() throws ObjectStreamException
{
  return UNSPEC;
}

} // class

