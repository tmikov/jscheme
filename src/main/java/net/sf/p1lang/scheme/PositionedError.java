/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.util.ArrayList;

public class PositionedError extends SchemeError implements ISourceCoords
{
private static final long serialVersionUID = -338380394007676854L;

private final String m_fileName;
private final int m_position;

private ArrayList<SourceCoords> m_stackTrace;

public PositionedError ( ISourceCoords coords, final String message, Throwable cause )
{
  super(message, cause);
  if (coords != null)
  {
    m_fileName = coords.getFileName();
    m_position = coords.getPosition();
  }
  else
  {
    m_fileName = null;
    m_position = 0;
  }
}

public PositionedError ( ISourceCoords coords, final String message )
{
  this( coords, message, null );
}

public PositionedError ( final String message )
{
  this( (SourceCoords)null, message);
}

public PositionedError ( Pair at, String message )
{
  this( at instanceof PositionedPair ? (ISourceCoords)at : null, message );
}

public final boolean havePosition ()
{
  return m_position != 0;
}

public void addSchemeStackTrace ( ISourceCoords coords )
{
  if (coords == null || coords.getPosition() == 0)
    return;

  if (m_stackTrace == null)
    m_stackTrace = new ArrayList<SourceCoords>();

  m_stackTrace.add( new SourceCoords(coords) );
}

@Override
public String getMessage ()
{
  if (m_position != 0 || m_stackTrace != null && m_stackTrace.size() > 0)
  {
    StringBuilder res = new StringBuilder(128);

    if (m_position != 0)
      res.append( String.format( "%s(%d).%d:", getFileName(), getLine(), getColumn() ) );
    if (getCause() != this && getCause() != null)
      res.append( getCause().getClass().getName() ).append(':');
    res.append( super.getMessage() );

    if (m_stackTrace != null && m_stackTrace.size() > 0)
    {
      res.append( '\n' );
      for (SourceCoords c : m_stackTrace)
        res.append( String.format( "%s(%d).%d\n", c.getFileName(), c.getLine(), c.getColumn() ) );
    }

    return res.toString();
  }
  else
    return super.getMessage();
}

public final int getLine ()
{
  return SourceCoords.extractLine( m_position );
}

public final int getColumn ()
{
  return SourceCoords.extractColumn( m_position );
}

public final int getPosition ()
{
  return m_position;
}

public final String getFileName ()
{
  return m_fileName;
}

public boolean coordsEqual ( final ISourceCoords x )
{
  return x != null && x.getPosition() == m_position &&
         (m_fileName!=null?m_fileName:"").equals( x.getFileName() );
}
} // class

