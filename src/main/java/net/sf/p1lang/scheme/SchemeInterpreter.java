/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.io.StringReader;
import java.io.Reader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;

import net.sf.p1lang.scheme.baselib.DisplayProcedure;
import net.sf.p1lang.scheme.baselib.GentempProcedure;
import net.sf.p1lang.scheme.baselib.StringAppendProcedure;
import net.sf.p1lang.scheme.baselib.NumberToStringProcedure;

public class SchemeInterpreter
{
private final SymbolMap m_map = new SymbolMap();
private final HashMap<Symbol, BuiltinFactory> m_builtins = new HashMap<Symbol,BuiltinFactory>();

private final TopLevelScope m_topLevelScope;

private int m_nextTempSymbol = 0;

public SchemeInterpreter () throws SchemeError, IOException
{
  Builtins.define( m_map, m_builtins );
  defineBuiltin( "CallCC", AST.CallCC.class );
  defineBuiltin( "Apply", AST.Apply.class );

  m_topLevelScope = new TopLevelScope( this, 1024 );
  bootstrap();
}

public void defineJavaProcedure ( String name, JavaProcedure proc )
{
  defineJavaProcedure( m_topLevelScope, name, proc );
}

public <T extends AST.Builtin> void defineBuiltin ( String name, Class<T> clz )
{
  m_builtins.put( m_map.newSymbol(name), new BuiltinFactory<T>(clz) );
}

public Object getTopLevelVar ( String name ) throws SchemeError
{
  VarBinding b;
  if ( (b = m_topLevelScope.lookupVar( m_map.newSymbol(name) )) == null)
    throw new SchemeError( String.format("Variable '%s' is not bound", name ) );
  return m_topLevelScope.m_env[b.index];
}

public Closure getTopLevelClosure ( String name ) throws SchemeError
{
  Object o = getTopLevelVar( name );
  if (o instanceof Closure)
    return (Closure) o;
  else
    throw new SchemeError( String.format("Variable '%s' is not bound to a closure", name ) );
}

public Object evalClosure ( Closure closure, Object ... args ) throws SchemeError
{
  return
    new AST.Apply( null, new AST[]{ new AST.Lit( null, closure ) } )
            .evalValue( m_topLevelScope.m_env, new EvalContext(this, m_topLevelScope.m_env));
}

private void defineStdLib ( TopLevelScope scope )
{
  if (scope.m_interp != this)
    throw new IllegalArgumentException( "Scope not associated with this interpreter" );

  defineJavaProcedure( scope, "display", new DisplayProcedure() );
  defineJavaProcedure( scope, "gentemp", new GentempProcedure(this) );
  defineJavaProcedure( scope, "__%string-append", new StringAppendProcedure());
  defineJavaProcedure( scope, "__%number->string", new NumberToStringProcedure());
}

private void defineJavaProcedure ( TopLevelScope scope, String name, JavaProcedure proc )
{
  if (scope.m_interp != this)
    throw new IllegalArgumentException( "Scope not associated with this interpreter" );

  Symbol sym = m_map.newSymbol(name);
  if (proc.name == null)
    proc.name = sym;

  scope.defineLambda( sym, proc );
}

private void bootstrap ()
{
  SimpleErrorReporter errors = new SimpleErrorReporter(10);
  try
  {
    // Our bootstrap procedure is subtle. The problem is that macros can only use functions and
    // macros which have previously been compiled and their definitions executed (bound in the
    // top environment)
    // So we compile every stage in the top level environment, then copy the top level into the
    // macro environment and execute the code of all stages up to here.

    TopLevelScope macroScope;
    ParsedList plist = parse( SchemeInterpreter.class.getResource("base0.scm" ), errors );

    ArrayList<Pair> stages = new ArrayList<Pair>();
    ArrayList<CompiledCode> compiled = new ArrayList<CompiledCode>();

    for ( Pair list = plist.m_list; list != Pair.NULL; list = (Pair) list.getCdr())
      stages.add( (Pair) list.getCar());

    stages.add( parse(SchemeInterpreter.class.getResource("srfi-1.scm" ),errors).m_list );

    defineStdLib(m_topLevelScope);
    for (Pair stage : stages)
    {
      compiled.add(compile(m_topLevelScope, stage, errors, false));

      macroScope = m_topLevelScope.copy();
      defineStdLib(macroScope);
      for (CompiledCode cc : compiled)
        cc.eval(macroScope.m_env);

      m_topLevelScope.setMacroScope(macroScope);
    }

    // Libs are compiled after the stages and don't require the recursive initialization

    compiled.add( compile( parse(SchemeInterpreter.class.getResource("srfi-9.scm" ),errors), errors, false ) );
    compiled.add( compile( parse(SchemeInterpreter.class.getResource("srfi-69.scm" ),errors), errors, false ) );

    // Prepare the final version of the macro environment by evaluating all compiled code in it
    macroScope = m_topLevelScope.copy();
    defineStdLib(macroScope);
    for (CompiledCode cc : compiled)
      cc.eval(macroScope.m_env);

    // Finally evaluate all compiled code in the top level environment
    for ( CompiledCode cc : compiled )
      cc.eval( m_topLevelScope.m_env );
  }
  catch (IOException e)
  {
    throw new RuntimeException(e);
  }
  catch (ParseErrors e)
  {
    for ( ErrorInfo ei : errors.getErrorList() )
      System.err.println( ei );
    throw new RuntimeException(e);
  }
  catch (SchemeError e)
  {
    throw new RuntimeException(e);
  }
}

private final Lexer makeLexer ( Reader in, String name, IErrorReporter errors )
{
  return new Lexer( in, name, m_map, errors );
}

private ParsedList parse ( Lexer lexer ) throws ParseErrors
{
  DatumParser dr = new DatumParser( lexer );

  Pair list = Pair.NULL;
  try
  {
    Object datum;
    while ((datum  = dr.parseDatum()) != DatumParser.EOF)
      list = new Pair( datum, list );
  }
  catch (TooManyErrors ignored)
  {}

  if (lexer.getErrorReporter().getErrorCount() > 0)
    throw new ParseErrors( lexer.getErrorReporter() );

  return new ParsedList( this, Misc.reverse(list) );
}

public ParsedList parse ( Reader in, String name, IErrorReporter errors ) throws ParseErrors
{
  return parse( makeLexer( in, name, errors ) );
}

public ParsedList parse ( URL url, IErrorReporter errors ) throws ParseErrors, IOException
{
  if (url == null)
    throw new IllegalArgumentException();
  return parse( new BufferedReader( new InputStreamReader(url.openStream()) ), url.toString(), errors );
}

public Object eval ( URL url, IErrorReporter errors ) throws IOException, SchemeError
{
  return compile( parse( url, errors ), errors ).eval();
}

/**
 * Throws TooManyErrors at teh first error without recording it. It is usable when we want to know
 * whether there was an error at all, but don't care what it was.
 */
private static final IErrorReporter s_dummyReporter = new IErrorReporter()
{
  public void error ( final ISourceCoords coords, final Throwable cause, final String message, final Object... args )
    throws TooManyErrors
  {
    throw new TooManyErrors();
  }
  public List<ErrorInfo> getErrorList ()
  {
    return null;
  }
  public int getErrorCount ()
  {
    return 0;
  }
};

/**
 * Check if a string is a complete s-expression. This is intended for use from within an
 * interactive environment where we want to allow more input after the user presses enter.
 * Once we know we have a complete expression, we will re-parse the whole thing.
 *
 * <p>Lexical errors will be thrown early, because we don't want to allow the user to enter
 * lots of lines, only to find out that he had a lexical error on the first one.
 *
 * <p>Unfortunately this is not a 100% solution. It is possible that the line ended in the middle
 * of a string. We won't be able to handle that.
 *
 * @param str
 * @return true if this is a complete expression or there was a parse error
 */
public boolean isCompleteExpr ( String str )
{
  try
  {
    Lexer lex = makeLexer( new StringReader(str), "<string>", s_dummyReporter );
    Lexer.Token tok;
    int level = 0;
    while ( (tok = lex.nextToken()) != Lexer.Token.EOF)
    {
      if (tok == Lexer.Token.LPAR || tok == Lexer.Token.LSQUARE)
        ++level;
      else if (tok == Lexer.Token.RPAR || tok == Lexer.Token.RSQUARE)
        --level;
      //System.out.println( String.format( "%10s %d", tok, level ) );
    }

    return level <= 0;
  }
  catch (TooManyErrors w)
  {
    // If there is any error, we tell the caller that the expression is complete, so he will
    // immediately re-parse it and discover the error
    return true;
  }
}

public CompiledCode compile ( ParsedList parsedList, IErrorReporter errors )
  throws IllegalArgumentException, ParseErrors, SchemeUncheckedError
{
  return compile( parsedList, errors, true );
}

public CompiledCode compile ( ParsedList parsedList, IErrorReporter errors, boolean needResult )
  throws IllegalArgumentException, ParseErrors, SchemeUncheckedError
{
  if (parsedList.m_interp != this)
    throw new IllegalArgumentException( "ParsedList from another interpreter instance" );
  return compile( m_topLevelScope, parsedList.m_list, errors, needResult );
}

private CompiledCode compile ( TopLevelScope scope, Pair list, IErrorReporter errors, boolean needResult )
        throws IllegalArgumentException, SchemeUncheckedError, ParseErrors
{
  CompiledCode res;
  try
  {
    res = new Compiler(this, errors, m_builtins, scope).compileTopLevel(list, needResult);
  }
  catch (TooManyErrors e)
  {
    throw new ParseErrors( errors );
  }
  if (errors.getErrorCount() > 0)
    throw new ParseErrors( errors );
  return res;
}

public Object eval ( String str )
  throws ParseErrors, SchemeError, SchemeUncheckedError
{
  SimpleErrorReporter errors = new SimpleErrorReporter(1);
  return compile( parse( new StringReader(str), "<null>", errors ), errors, true ).eval();
}

public Symbol gentemp ()
{
  return m_map.newSymbol( "__%"+(++m_nextTempSymbol) );
}

public static int asInt ( Object o ) throws SchemeError
{
  return ((SchemeNumber)o).toInteger().toJavaInt();
}

public static long asLong ( Object o ) throws SchemeError
{
  return ((SchemeNumber)o).toInteger().toJavaLong();
}

} // class

