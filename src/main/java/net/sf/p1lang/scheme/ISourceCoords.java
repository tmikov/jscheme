package net.sf.p1lang.scheme;/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

public interface ISourceCoords
{
public int getLine ();
public int getColumn ();
public int getPosition ();
public String getFileName ();

public boolean coordsEqual ( ISourceCoords x );
}
