/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.io.PrintWriter;
import java.util.IdentityHashMap;

final class Misc
{
private Misc () {};

/**
 * {@code (cons (car old) tail)}, preserving the source coordinates.
 * @param old
 * @param tail
 * @return the new pair
 */
private static Pair copyCar ( Pair old, Pair tail )
{
  if (old instanceof PositionedPair)
    return new PositionedPair(old.getCar(), tail ).setCoords( old );
  else
    return new Pair(old.getCar(), tail );
}

/**
 * Reverse a list, preserving source coordinates (if present)
 * @param list the list to reverse
 * @return the reversed list
 */
public static Pair reverse ( Pair list )
{
  Pair seed = Pair.NULL;
  for ( ; list != Pair.NULL; list = (Pair) list.getCdr())
    seed = copyCar( list, seed );
  return seed;
}

public static Pair appendReverse ( Pair revHead, Pair tail )
{
  while (revHead != Pair.NULL)
  {
    tail = copyCar( revHead, tail );
    revHead = (Pair) revHead.getCdr();
  }
  return tail;
}

private static void display_ ( PrintWriter out, Object datum, IdentityHashMap<Object,Object> visited )
{
  if (datum == Pair.NULL)
    out.print( "()" );
  else if (datum instanceof Object[])
  {
    Object[] vec = (Object[])datum;
    out.print("#(");
    if (!visited.containsKey( datum ))
    {
      visited.put( datum, datum );
      for ( int i = 0; i < vec.length; ++i )
      {
        if (i > 0)
          out.print(' ');
        display_( out, vec[i], visited );
      }
    }
    else
      out.print( "@rc@");
    out.print(')');
  }
  else if (datum instanceof Pair)
  {
    Pair p = (Pair)datum;
    out.print( '(' );
    if (!visited.containsKey( datum ))
    {
      visited.put( datum, datum );
      for(;;)
      {
        display_( out, p.getCar(), visited );
        if (p.getCdr() == Pair.NULL)
          break;
        else if (p.getCdr() instanceof Pair)
        {
          p = (Pair) p.getCdr();
          out.print(' ');
        }
        else
        {
          out.print(" . ");
          display_( out, p.getCdr(), visited );
          break;
        }
      }
    }
    else
      out.print( "@rc@");
    out.print( ')' );
  }
  else
    out.print( datum );
}

public static void display ( PrintWriter out, Object datum )
{
  display_( out, datum, new IdentityHashMap<Object,Object>() );
}

public static void displayIndented ( PrintWriter out, int indent, Object datum )
{
  if (datum == Pair.NULL)
    out.print( "()" );
  else if (datum instanceof Object[])
  {
    Object[] vec = (Object[])datum;
    out.print("#(");
    for ( int i = 0; i < vec.length; ++i )
    {
      if (i > 0)
        out.print(' ');
      displayIndented( out, indent, vec[i] );
    }
    out.print(')');
  }
  else if (datum instanceof Pair)
  {
    Pair p = (Pair)datum;
    out.print( '(' );
    indent += 4;
    for(;;)
    {
      if (p != datum)
      {
        out.println();
        for ( int i = 0; i < indent; ++i )
          out.print( ' ' );
      }

      displayIndented( out, indent, p.getCar());
      if (p.getCdr() == Pair.NULL)
        break;

      if (p.getCdr() instanceof Pair)
      {
        p = (Pair) p.getCdr();
        out.print(' ');
      }
      else
      {
        out.print(" . ");
        displayIndented( out, indent, p.getCdr());
        break;
      }
    }
    out.print( ')' );
    indent -= 4;
  }
  else if (datum instanceof Symbol)
    out.print( datum );
  else
    out.print( datum );
}



} // class

