/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.io.Serializable;

/**
 * Stores source coordinates. This is not really practical to be used widely, because as an
 * an independent object it takes too much space. It is just a convenient wrapper.
 */
public class SourceCoords implements ISourceCoords, Serializable
{
private static final long serialVersionUID = -1953892843098140935L;

public String fileName;
public int line, column;

public SourceCoords ( final String fileName, final int line, final int column )
{
  this.fileName = fileName;
  this.line = line;
  this.column = column;
}

public SourceCoords ( ISourceCoords o )
{
  if (o != null)
    assign( o );
}

public SourceCoords ()
{}

public void clear ()
{
  fileName = null;
  line = column = 0;
}

public void assign ( ISourceCoords o )
{
  fileName = o.getFileName();
  line = o.getLine();
  column = o.getColumn();
}

public void assign ( SourceCoords o ) // faster
{
  fileName = o.fileName;
  line = o.line;
  column = o.column;
}

/**
 * Combine line and column in one int. The line is in the low 20 bits, the column in the next 11. Any
 * of them could be unspecified, encoded as 0.
 *
 * @param line
 * @param column
 * @return
 */
public static int combinePosition ( int line, int column )
{
  return ((column & 0x7FF)<<20) | (line & 0xFFFFF);
}

public static int extractLine ( int position )
{
  return position & 0xFFFFF;
}

public static int extractColumn ( int position )
{
  return (position >> 20) & 0x7FF;
}

public int getLine ()
{
  return line;
}

public int getColumn ()
{
  return column;
}

public int getPosition ()
{
  return combinePosition( line, column );
}

public String getFileName ()
{
  return fileName;
}

public boolean coordsEqual ( final ISourceCoords x )
{
  return x != null && x.getPosition() == getPosition() &&
         (fileName!=null?fileName:"").equals( x.getFileName() );
}
} // class

