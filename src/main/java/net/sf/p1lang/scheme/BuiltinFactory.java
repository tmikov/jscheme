/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

final class BuiltinFactory<T extends AST.Builtin>
{
public final int paramCount;
public final boolean haveRest;
private final Constructor<T> m_constr;

public BuiltinFactory ( final Class<T> clazz )
{
  if (!AST.Builtin.class.isAssignableFrom(clazz))
    throw new IllegalArgumentException();

  try
  {
    paramCount = clazz.getField( "PARAM_COUNT" ).getInt(null);
    haveRest = clazz.getField( "HAVE_REST" ).getBoolean(null);
    m_constr = clazz.getConstructor(ISourceCoords.class, AST[].class);
  }
  catch (NullPointerException e)
  {
    throw new IllegalArgumentException("Not a static field", e);
  }
  catch (NoSuchMethodException e)
  {
    throw new IllegalArgumentException(e);
  }
  catch (NoSuchFieldException e)
  {
    throw new IllegalArgumentException(e);
  }
  catch (IllegalAccessException e)
  {
    throw new IllegalArgumentException(e);
  }
}

public final AST.Builtin create ( ISourceCoords coords, AST[] values )
{
  try
  {
    return m_constr.newInstance(coords, values);
  }
  catch (InstantiationException e)
  {
    throw new RuntimeException( e );
  }
  catch (IllegalAccessException e)
  {
    throw new RuntimeException( e );
  }
  catch (InvocationTargetException e)
  {
    throw new RuntimeException( e );
  }
}

} // class

