/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.io.StringReader;

public final class TestUtils
{
private TestUtils () {};

public static Lexer lexer ( SymbolMap map, String str )
{
  return new Lexer( new StringReader(str), "<string>", map, new SimpleErrorReporter(1) );
}

public static Lexer lexer ( String str )
{
  return lexer( new SymbolMap(), str );
}

} // class

