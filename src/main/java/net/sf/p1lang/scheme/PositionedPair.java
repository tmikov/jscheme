/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

/**
 * A pair containing optional source position : file, line, column.
 * Any of the position attributes are optional. The file could be null, the line and column 0.
 */
public class PositionedPair extends Pair implements ISourceCoords
{
private static final long serialVersionUID = -98356705216314397L;
public String fileName;
/**
 * Combined line and column. See {@link net.sf.p1lang.scheme.SourceCoords#combinePosition(int, int)}
 */
public int position;

public PositionedPair ( Object car, Object cdr )
{
  super( car, cdr );
}

public PositionedPair ( Object car )
{
  this( car, Pair.NULL );
}

public final PositionedPair setCoords ( ISourceCoords coords )
{
  if (coords != null)
  {
    setFileName( coords.getFileName() );
    this.position = coords.getPosition();
  }
  else
  {
    this.fileName = null;
    this.position = 0;
  }
  return this;
}

public PositionedPair setCoords ( Pair coords )
{
  if (coords instanceof PositionedPair)
  {
    PositionedPair p = (PositionedPair) coords;
    this.fileName = p.fileName;
    this.position  = p.position;
  }
  else
  {
    this.fileName = null;
    this.position = 0;
  }
  return this;
}

public final void setPosition ( int line, int column )
{
  this.position = SourceCoords.combinePosition( line, column );
}

public void setFileName ( final String fileName )
{
  this.fileName = fileName;
}

public final int getLine ()
{
  return SourceCoords.extractLine( this.position );
}

public final int getColumn ()
{
  return SourceCoords.extractColumn( this.position );
}

public final int getPosition ()
{
  return position;
}

public final String getFileName ()
{
  return fileName;
}

public boolean coordsEqual ( final ISourceCoords x )
{
  return x != null && x.getPosition() == position &&
         (fileName!=null?fileName:"").equals( x.getFileName() );
}
} // class

