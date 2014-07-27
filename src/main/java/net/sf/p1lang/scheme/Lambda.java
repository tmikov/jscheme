/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.io.Serializable;

public class Lambda implements Serializable
{
private static final long serialVersionUID = 1541637304714048692L;

/**
 * The name that was associated with the lambda if it was (defined). Naturally this cannot be
 * generally accurate and is just for information.
 */
public Symbol name;

/** Size of the environment (including all parameters) */
public int envSize;
/** Number of "regular" parameters (excluding the "rest" parameter) */
public int paramCount;
/** A last parameter is the "rest" */
public boolean haveRest;
public AST body;

public String toString ()
{
  StringBuilder res = new StringBuilder();
  res.append("#<lambda:");
  if (name != null)
    res.append( name.toString() );
  res.append(':').append(paramCount).append(':').append(haveRest?'t':'f').append('>');
  return res.toString();
}

} // class

