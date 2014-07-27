/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

public class SimpleErrorReporter implements IErrorReporter
{
private final int m_maxErrors;
private final LinkedList<ErrorInfo> m_errorList = new LinkedList<ErrorInfo>();

public SimpleErrorReporter ( final int maxErrors )
{
  if (maxErrors <= 0)
    throw new IllegalArgumentException();
  m_maxErrors = maxErrors;
}

public void error ( final ISourceCoords coords, final Throwable cause, final String message, final Object... args )
        throws TooManyErrors
{
  m_errorList.add( new ErrorInfo( coords, String.format( message, args ), cause ) );
  if (m_errorList.size() >= m_maxErrors)
    throw new TooManyErrors();
}

public List<ErrorInfo> getErrorList ()
{
  return Collections.unmodifiableList( m_errorList );
}

public int getErrorCount ()
{
  return m_errorList.size();
}
} // class

