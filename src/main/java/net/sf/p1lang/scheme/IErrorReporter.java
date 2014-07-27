/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */
package net.sf.p1lang.scheme;

import java.util.List;

public interface IErrorReporter
{
public void error (
    ISourceCoords coords, Throwable cause, String message, Object ... args
  ) throws TooManyErrors;

public List<ErrorInfo> getErrorList ();

public int getErrorCount ();
}
