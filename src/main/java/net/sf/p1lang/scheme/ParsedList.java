/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

public class ParsedList
{
final SchemeInterpreter m_interp;
final Pair m_list;

ParsedList ( final SchemeInterpreter interp, final Pair list )
{
  m_interp = interp;
  m_list = list;
}

public String toString ()
{
  return m_list.toString();
}

} // class

