/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.io.PrintWriter;

// TODO: better management of instruction sequences. Currently we wrap then in Begin

/*
FIXME: proper error detection in letrec and letrec*
FIXME: a sequence of defines should be converted to letrec*, or at least get the same error
       detection. For example:
(define (func)
  (define a b)
  (define b 10)
  ...)

Which is the same as:
  (letrec* ((a b) (b 10)) ...)

For now we simply initialize a with "#unspecified", but that is not good enough.
 */

// FIXME: generate a trampoline only if there is a tail call


/*
Error recovery
==============

Our error recovery strategy is simple but effective. As soon as we discover an error in a form,
we don't process it further. Thus, nested forms are ignored - this is actually beneficial if the
erroneous form is, for example, "let".

 */

final class Compiler
{
private final SchemeInterpreter m_interp;
private final IErrorReporter m_errors;
private final HashMap<Symbol, BuiltinFactory> m_builtins;
private final TopLevelScope m_topLevelScope;

/**
 * This is set to true if a call occurs in a tail context. Then we know that we should generate
 * a trampoline instruction at the top of the instruction chain.
 */
private boolean m_haveTailCall;

private static enum BodyLevel
{
  LIBRARY, // must always be used in a new compiler environment
  LAMBDA,  // must always be used in a new compiler environment
  INNER;
}

/**
 *
 * @param interp
 * @param builtins
 * @param topLevelScope
 * @throws IllegalArgumentException if the scopes are from another interpreter instance or
 *            if they are the same scope
 */
Compiler ( final SchemeInterpreter interp,
           final IErrorReporter errors,
           final HashMap<Symbol, BuiltinFactory> builtins,
           final TopLevelScope topLevelScope
)
        throws IllegalArgumentException
{
  if (topLevelScope.m_interp != interp)
    throw new IllegalArgumentException( "Top level scope from another interpreter instance" );

  m_interp = interp;
  m_errors = errors;
  m_builtins = builtins;
  m_topLevelScope = topLevelScope;
}

private Compiler ( Compiler parent )
{
  this( parent.m_interp, parent.m_errors, parent.m_builtins, parent.m_topLevelScope );
}

final CompiledCode compileTopLevel ( Pair list, boolean needResult ) throws SchemeUncheckedError
{
  AST body = compileBody( true, BodyLevel.LIBRARY, m_topLevelScope, list, needResult, false );
  return new CompiledCode( m_interp, body , m_topLevelScope.getBindingCount(), m_topLevelScope.m_env );
}

/**
 * This method must always be executed in a new compiler environment
 */
private final AST compileLambda ( Scope scope, Pair pair, boolean needResult, boolean tail )
{
  try
  {
    Pair error = pair;
    pair = cdr(pair); // skip the lambda keyword
    if (pair.getCdr() == Pair.NULL)
      return parseError( pair, "Lambda without a body" );
    return compileLambda(scope, error, pair.getCar(), cdr(pair), needResult, tail, null );
  }
  catch (InvalidList invalidList)
  {
    return AST.Lit.LIT_UNSPEC;
  }
}

/**
 * This method must always be executed in a new compiler environment
 */
private final AST compileLambda ( Scope scope, Pair errorp, Object formalParams,
                                    Pair body, boolean needResult, boolean tail,
                                    Symbol name )
{
  if (body == Pair.NULL)
    return parseError( errorp, "Lambda without a body" );

  Lambda proc = new Lambda();
  proc.name = name;

  Scope lambdaScope = new Scope(scope);
  if (!checkParams(lambdaScope, errorp, proc, formalParams ))
    return AST.Lit.LIT_UNSPEC;

  proc.body = compileBody( true, BodyLevel.LAMBDA, lambdaScope, body, true, true );
  proc.envSize = lambdaScope.getBindingCount();

  // Q: Why didn't we check needResult before compiling the body
  // A: Even if we don't need it, we should still make sure it is valid
  return needResult ? new AST.MakeClosure( c(errorp), proc ) : null;
}

private final AST makeBody ( List<AST> block, boolean needResult )
{
  if (block == null || block.size() == 0)
  {
    return needResult ? AST.Lit.LIT_UNSPEC : null;
  }
  else if (block.size() > 1)
    return new AST.Begin( block.get(0), block.toArray(new AST[block.size()]) );
  else
    return block.get(0);
}

/**
 *
 * @param error coordinates to use for an error
 * @param proc
 * @param lambdaScope
 * @param formalParams
 */
private final boolean checkParams ( Scope lambdaScope, Pair error, Lambda proc, Object formalParams )
{
  try
  {
// Validate the formal parameters. A possibly empty list of symbols ending with a symbol
    while ((formalParams instanceof Pair) && formalParams != Pair.NULL)
    {
      Pair p = (Pair)formalParams;
      Symbol sym = carSymbol(p);

      if (lambdaScope.localLookupVar(sym) != null)
      {
        parseError( p, "Duplicated formal parameter %s", sym );
        return false;
      }
      lambdaScope.bindVar( sym );
      ++proc.paramCount;

      formalParams = p.getCdr();
    }
    // Check for the "rest" parameter
    if (formalParams != Pair.NULL)
    {
      if (!(formalParams instanceof Symbol))
      {
        parseError( error, "<rest> parameter is not a symbol" );
        return false;
      }
      lambdaScope.bindVar( (Symbol)formalParams );
      proc.haveRest = true;
    }

    return true;
  }
  catch (InvalidList invalidList)
  {
    return false;
  }
}

/**
 *
 * @param decl true if still in decl context (declarations are allowed)
 * @param scope
 * @param body
 * @param needResult
 * @param tail
 * @return
 */
private final AST compileBody (
    boolean decl, BodyLevel bodyLevel, Scope scope, Pair body, boolean needResult, boolean tail )
{
  body = expandBody( bodyLevel, scope, body, decl );

  List<AST> block = newBlock();
  try
  {
    if (body != Pair.NULL) // Body not empty ?
    {
      AST t;
      for ( ; body.getCdr() != Pair.NULL; body = cdr(body) )
        spliceInstr( block, compileValue(decl, scope, body, false, false ) );

      if ( (t = compileValue(decl, scope, body, needResult, tail )) != null)
      {
        // Generate a trampoline only at the outer level and if there is a tail call
        if ((bodyLevel == BodyLevel.LAMBDA || bodyLevel == BodyLevel.LIBRARY) &&
            m_haveTailCall)
          t = new AST.Trampoline( t, t );
        spliceInstr( block, t );
      }
    }
  }
  catch (InvalidList ignored)
  {}
  return makeBody( block, needResult );
}


static final boolean DEBUG = false;
static final PrintWriter m_out = new PrintWriter( System.out, true );

/**
 * Expand the top-level macros and bind all declared symbols. Symbols bound in a (define...) form
 * are replaced with their binding (because a macro could later re-define the symbol).
 *
 * @param scope
 * @param body
 * @param decl true if still in decl context (declarations are allowed)
 * @return the expanded body
 */
private final Pair expandBody ( BodyLevel bodyLevel, Scope scope, Pair body, boolean decl )
{
  Pair expBody = Pair.NULL; // accumulate the expanded body in reverse order here

  if (DEBUG)
    {m_out.println("======BEFORE");Misc.displayIndented( m_out, 0, body ); m_out.println();}

  try
  {
    for ( ; body != Pair.NULL; body = cdr(body) )
    {
      try
      {
        Object datum = expandDatum( scope, body, body.getCar()); // macro expansion

        // We are only interested in forms
        if (datum instanceof Pair)
        {
          Pair pair = (Pair)datum;
          switch (checkSpecial( scope, pair.getCar()))
          {
          case BEGIN:
            expBody = Misc.appendReverse( expandBody( bodyLevel, scope, cdr(pair), decl ), expBody );
            continue; // do not append the datum

          case DEFINE:
            if (!decl && bodyLevel != BodyLevel.LIBRARY)
              parseError( pair, "Declaration in expression context" );

            // Must bind the defined symbol and replace the symbol with its own binding
            {
              Symbol definedSymbol;
              VarBinding vb;
              Pair t = ncdr(pair);
              if (t.getCar() instanceof Symbol)
              {
                definedSymbol = (Symbol) t.getCar();
                vb = scope.bindVar( definedSymbol );

                t = new PositionedPair( vb, t.getCdr()).setCoords( t );
              }
              else
              {
                Pair t1 = carPair(t);
                definedSymbol = carSymbol(t1);
                vb = scope.bindVar( definedSymbol );

                t1 = new PositionedPair( vb, t1.getCdr()).setCoords( t1 );
                t = new PositionedPair( t1, t.getCdr()).setCoords( t );
              }

              pair = new PositionedPair(pair.getCar(), t ).setCoords( pair );
              datum = pair; // Store the replaced body
            }
            break;

          case DEFINE_MACRO: case DEFINE_IDENTIFIER_MACRO: case DEFINE_SET_MACRO:
            defineMacro( scope, pair );
            continue; // do not append the datum

          case MACRO_ENV:
            evalInMacroWorld(
              pair,
              new Compiler( m_interp, m_errors, m_builtins, m_topLevelScope.getMacroScope() )
                  .compileBody( true, BodyLevel.LIBRARY, m_topLevelScope.getMacroScope(), ncdr(pair), false, false )
            );
            continue; // do not append the datum

          default:
            decl = false;
            break;
          }
        }
        else
          decl = false;

        expBody = new PositionedPair( datum, expBody ).setCoords(body);
      }
      catch (InvalidList ignored)
      { }
    }
  }
  catch (InvalidList ignored)
  {}

  expBody = Misc.reverse(expBody);
  if (DEBUG)
    {m_out.println("======AFTER=");Misc.displayIndented( m_out, 0, expBody ); m_out.println();}
  return expBody;
}

// Expand the datum if it is a macro. Repeat the process for the result
private Object expandDatum ( Scope scope, Pair body, Object datum )
{
  Object d = datum;
  for(;;)
  {
    // Check for an identifier macro
    MacroBinding mb;

    if (datum instanceof Symbol &&
        (mb = scope.lookupMacro((Symbol) datum)) != null &&
        mb.identifier != null)
    {
      datum = expandIdentifierMacro( body, mb.identifier);
      continue;
    }
    else if (datum instanceof Pair) // check for macro form
    {
      Pair pair = (Pair) datum;
      if (pair.getCar() instanceof Symbol &&
          (mb = scope.lookupMacro((Symbol) pair.getCar())) != null &&
          mb.combination != null)
      {
        datum = expandCombinationMacro( scope, pair, mb.combination );
        continue;
      }
    }
    break;
  }

  if (DEBUG)
    if (d != datum)
    {
      m_out.println("===EXPAND");Misc.displayIndented( m_out, 0, d ); m_out.println();
      m_out.println("===INTO==");Misc.displayIndented( m_out, 0, datum ); m_out.println();
    }
  return datum;
}


private Object expandIdentifierMacro ( Pair next, Closure macroClosure )
{
  // Pass the identifier as the single macro parameter
  return evalMacro_( next, macroClosure, new AST[]{new AST.Lit(c(next), next.getCar())} );
}

private Object expandCombinationMacro ( Scope scope, Pair next, Closure macroClosure )
{
  try
  {
    // We are lazy. Instead of doing we manually, we will pretend that this is a call and we
    // will compile it. Then the call itself will perform parameter validation, etc.

    // We can't count the parameters in advance because any of them cuold be a macro

    // Compile instructions generating each parameter
    ArrayList<AST> paramList = new ArrayList<AST>();
    Pair pair = next;
    while ( (pair = cdr(pair)) != Pair.NULL)
      paramList.add( new AST.Lit( c(pair), pair.getCar()) );

    return evalMacro_( next, macroClosure, paramList.toArray(new AST[paramList.size()]) );
  }
  catch (InvalidList invalidList)
  {
    return Unspec.UNSPEC;
  }
}

private Object expandSetMacro ( Pair next, Closure macroClosure )
{
  try
  {
// Pass the identifier and the value as macro parameters
    Pair p1 = next;
    Pair p2 = ncdr(next);

    return evalMacro_( next, macroClosure,
                       new AST[]{new AST.Lit(c(p1), p1.getCar()), new AST.Lit(c(p2), p2.getCar())} );
  }
  catch (InvalidList invalidList)
  {
    return Unspec.UNSPEC;
  }
}


/**
 * Eval the specified macro.
 *
 * @param src
 * @param macro
 * @param params
 * @return
 */
private Object evalMacro_ ( Pair src, Closure macro, AST[] params )
{
  // We are lazy. Instead of doing we manually, we will pretend that this is a call and we
  // will compile it. Then the call itself will perform parameter validation, etc.

  // Build the parameters for (apply ...).
  // (apply..) expects its first parameter to eval to a closure and the last one to be a list
  AST[] applyParams = new AST[params.length+2];
  applyParams[0] = new AST.Lit(c(src),macro);
  applyParams[applyParams.length-1] = AST.Lit.LIT_NULL;
  System.arraycopy( params, 0, applyParams, 1, params.length );

  AST.Apply call =
     new AST.Apply( c(src), applyParams );

  Object res = null;
  res = evalInMacroWorld( src, call );

  if (isListCyclic( res ))
  {
    parseError( src, "Macro expansion returned a recursive list" );
    return Unspec.UNSPEC;
  }

  // TODO: fill source coordinates
  return res;
}

private final Object evalInMacroWorld ( Pair src, AST ast )
{
  if (ast == null)
    return Unspec.UNSPEC;

  try
  {
    Object[] env = m_topLevelScope.getMacroScope().m_env;
    return ast.evalValue( env, new EvalContext(m_interp, env ) );
  }
  catch (SchemeError schemeError)
  {
    parseError( src, "Macro raised an error:%s", schemeError.getMessage() );
    return Unspec.UNSPEC;
  }
}


private final AST compileValue ( Scope scope, Pair next, boolean needResult, boolean tail )
{
  return compileValue( false, scope, next, needResult, tail );
}

/**
 *
 * @param decl We use compileValue() both in declaration and expression context. This parameter
 *    tells it where it is.
 * @param scope
 * @param next
 * @param needResult
 * @param tail
 * @return
 */
private final AST compileValue ( boolean decl, Scope scope, Pair next, boolean needResult, boolean tail )
{
  try
  {
    Object datum = next.getCar();

    if (!decl) // Macros in declaration context have already been expanded
      datum = expandDatum( scope, next, datum );

    if (datum instanceof Symbol)
    {
      Symbol sym = (Symbol)datum;
      VarBinding vb;
      if ( (vb = scope.lookupVar(sym)) == null)
        return parseError( next, "Unbound variable %s", sym );
      return needResult ? new AST.Var( c(next), scope, vb ) : null;
    }
    else if (datum == Unspec.UNSPEC)
    {
      return needResult ? AST.Lit.LIT_UNSPEC : null;
    }
    else if (datum instanceof Object[])
    {
      return compileVector(scope, next, needResult, tail );
    }
    else if (datum == Pair.NULL)
    {
      return parseError( next, "Invalid syntax" );
    }
    else if (!(datum instanceof Pair))
    {
      return needResult ? new AST.Lit(c(next),datum) : null;
    }
    else
    {
      Pair pair = (Pair)datum;
      switch (checkSpecial(scope, pair.getCar()))
      {
      case BEGIN:
        // NOTE: begin-s which were in declaration context have already been spliced in by
        // expandBody()
        assert(!decl);
        return compileBody( false, BodyLevel.INNER, scope, cdr(pair), needResult, tail );
      case IF:
        return compileIf(scope, pair, needResult, tail );
      case LAMBDA:
        // Note: a lambda must be compiled in a separate compiler environment
        return new Compiler(this).compileLambda(scope, pair, needResult, tail );

      case BUILTIN:
        return compileBuiltin( scope, pair, needResult, tail );
      case SETBANG:
        return compileSetbang( scope, pair, needResult, tail );
      case LET:
        return compileLet( scope, pair, needResult, tail );
      case LETREC:
      case LETREC_STAR:
        return compileLetrecStar( scope, pair, needResult, tail );
      case QUOTE:
        return compileQuote( scope, pair, needResult, tail );

      case DEFINE:
        if (!decl)
          return parseError( pair, "(define ...) not allowed in expression context" );
        return compileDefine(scope, pair, needResult, tail );

      case MACRO_ENV:
      case DEFINE_MACRO: case DEFINE_IDENTIFIER_MACRO: case DEFINE_SET_MACRO:
        assert(!decl);
        return parseError( pair, "Macro definitions not allowed in expression context" );

/* Handled by a macro
      case QUASIQUOTE:
      case UNQUOTE:
      case UNQUOTE_SPLICING:
*/

      case QUASISYNTAX:
      case SYNTAX:
      case UNSYNTAX:
      case UNSYNTAX_SPLICING:
        return parseError( pair, "Not implemented" );

      case NONE: // A function call
        return compileCall(scope, pair, needResult, tail);
      }

      assert false;
      return null;
    }
  }
  catch (InvalidList invalidList)
  {
    return AST.Lit.LIT_UNSPEC;
  }
}

private void defineMacro ( Scope scope, Pair next ) throws InvalidList
{
  Symbol form = (Symbol) next.getCar(); // remember whether it is define-macro/define-identifier-macro/etc
  Pair pair = next;
  AST value = null;
  Symbol sym;

  pair = cdr(pair);
  if (pair.getCar() instanceof Symbol)
  {
    sym = (Symbol) pair.getCar();

    if (pair.getCdr() != Pair.NULL)
    {
      pair = cdr(pair);
      value = new Compiler( m_interp, m_errors, m_builtins, m_topLevelScope.getMacroScope() )
                  .compileValue( m_topLevelScope.getMacroScope(), pair, true, false );
      needNullCdr( pair );
    }
  }
  else if (pair.getCar() instanceof Pair) // Defining a function ?
  {
    Pair inner = (Pair) pair.getCar();
    sym = carSymbol(inner);

    Pair body = cdr(pair);
    if (body == Pair.NULL)
    {
      parseError( pair, "Bad (%s ()) form syntax: missing body",form );
      return;
    }

    value = new Compiler( m_interp, m_errors, m_builtins, m_topLevelScope.getMacroScope() )
                .compileLambda( m_topLevelScope.getMacroScope(), pair, inner.getCdr(), body, true, false, sym );
  }
  else
  {
    parseError( pair, "Bad (%s) form syntax",form );
    return;
  }

  Object tmp = evalInMacroWorld( next, value );
  if (!(tmp instanceof Closure))
  {
    parseError( next, "(%s) body did not evaluate to a closure",form );
    return;
  }
  Closure closure = (Closure)tmp;

  MacroBinding mb = scope.bindMacro( sym );
  switch (form.code)
  {
  case DEFINE_MACRO: mb.combination = closure; break;
  case DEFINE_IDENTIFIER_MACRO: mb.identifier = closure; break;
  case DEFINE_SET_MACRO: mb.set = closure; break;
  default: assert( false );
  }
}

private AST compileVector ( Scope scope, Pair pair, boolean needResult, boolean tail )
{
  assert( pair.getCar() instanceof Object[] );
  Object[] vec = (Object[]) pair.getCar();

  AST ast[] = new AST[vec.length];
  boolean literal = true;
  for ( int i = 0; i < ast.length; ++i )
  {
    ast[i] = compileValue( scope, new PositionedPair( vec[i] ).setCoords(pair), needResult, false );
    if (!(ast[i] instanceof AST.Lit))
      literal = false;
  }

/* This isn't safe.
  if (literal)
  {
    // If all values were literals, we can return the entire vector as a literal
    if (!needResult)
      return AST.Lit.LIT_UNSPEC;

    Object[] lit = new Object[ast.length];
    for ( int i = 0; i < lit.length; ++i )
      lit[i] = ((AST.Lit)ast[i]).datum;

    return new AST.Lit( c(pair), lit );
  }
  else
*/
  {
    if (!needResult)
    {
      // Evaluate all expressions for their side effects only
      ArrayList<AST> block = new ArrayList<AST>(ast.length);
      for ( AST a : ast )
        if (a != null && !(a instanceof AST.Lit))
          block.add( a );

      return makeBody( block, false );
    }
    else
      return new AST.MakeVector( c(pair), ast );
  }
}

private AST compileIf ( Scope scope, Pair pair, boolean needResult, boolean tail )
        throws InvalidList
{
  Pair cond = ncdr(pair);
  Pair then = ncdr(cond);
  Pair pelse = cdr(then);
  if (pelse != Pair.NULL)
    needNullCdr( pelse );

  AST condI, thenI, elseI;

  condI = compileValue(scope, cond, true, false );
  thenI = compileValue(scope, then, true, tail );
  if (pelse != Pair.NULL)
    elseI = compileValue(scope, pelse, true, tail );
  else
    elseI = AST.Lit.LIT_UNSPEC;

  return new AST.If( c(pair), condI, thenI, elseI );
}

private AST compileCall ( Scope scope, Pair pair, boolean needResult, boolean tail )
        throws InvalidList
{
  ArrayList<AST> paramList = new ArrayList<AST>();

  // Evaluate the target of the call
  paramList.add( compileValue(scope, pair, true, false ) );

  // Evaluate all the parameters
  Pair cur = pair;
  while ( (cur = cdr(cur)) != Pair.NULL)
    paramList.add( compileValue(scope, cur, true, false ) );

  // (apply ...) expects its last parameter to be a list (empty in this case)
  paramList.add( AST.Lit.LIT_NULL );

  if (tail)
    m_haveTailCall = true;

  return new AST.Apply( c(pair), paramList.toArray(new AST[paramList.size()]) );
}

private AST compileBuiltin ( Scope scope, Pair pair, boolean needResult, boolean tail )
        throws InvalidList
{
  Pair symp = ncdr( pair );
  Symbol sym = carSymbol( symp );

  BuiltinFactory factory;
  if ( (factory = m_builtins.get( sym )) == null)
    return parseError( symp, "%s is not a builtin",sym );

  // Compile the parameters and count them
  ArrayList<AST> params = new ArrayList<AST>();
  Pair paramp = cdr(symp);
  while (paramp != Pair.NULL)
  {
    params.add( compileValue(scope, paramp, true, false ) );
    paramp = cdr(paramp);
  }

  // Verify the number of parameters
  if (params.size() < factory.paramCount)
    return parseError( symp, "Builtin %s expects %s%d parameters", sym,
                                               factory.haveRest?"at least ":"",factory.paramCount);
  if (params.size() > factory.paramCount && !factory.haveRest)
    return parseError( symp, "Builtin %s expects %s%d parameters", sym,
                                               factory.haveRest?"at least ":"",factory.paramCount);


  AST res = factory.create( c(pair), params.toArray( new AST[params.size()] ) );
  if (res instanceof AST.Apply)
    m_haveTailCall = true;
  return res;
}

private AST compileSetbang ( Scope scope, Pair next, boolean needResult, boolean tail )
        throws InvalidList
{
  Pair symp = ncdr(next);  // identifier pair
  Symbol sym = carSymbol(symp);
  Pair valp = ncdr(symp);  // value pair
  needNullCdr( valp );

  MacroBinding mb;
  if ( (mb = scope.lookupMacro(sym))!=null && mb.set != null)
  {
    Object datum = expandSetMacro( next, mb.set );
    // Create a "fake" pair holding the macro result
    Pair p = new PositionedPair( datum ).setCoords( next );
    return compileValue( scope, p, needResult, tail );
  }

  VarBinding vb;
  if ( (vb = scope.lookupVar( sym )) == null)
    return parseError( symp, "Unbound variable %s in set!", sym );

  return new AST.SetBang( c(next), scope, vb, compileValue(scope, valp, true, false ) );
}

private AST compileLet ( Scope scope, Pair next, boolean needResult, boolean tail )
        throws InvalidList
{
  Pair t = ncdr(next);
  Pair bindings;
  Symbol name;

  // A let always needs a scope of its own
  scope = new Scope( scope, true );

  if (t.getCar() instanceof Symbol) // named let ?
  {
    name = (Symbol) t.getCar();
    bindings = ncdr(t);
  }
  else
  {
    bindings = t;
    name = null;
  }
  Pair body = cdr( bindings );

  // Compile the binding values in the current scope
  ArrayList<Pair> compiled = new ArrayList<Pair>(); // #( (symbol . instr) (symbol . instr) ... )
  for ( Pair curBinding = carPair(bindings); curBinding != Pair.NULL; curBinding = cdr(curBinding) )
  {
    Pair symp = carPair(curBinding);
    Symbol sym = carSymbol( symp );
    Pair valuep = ncdr(symp);
    needNullCdr( valuep );
    AST cv = compileValue( scope, valuep, true, false );
    compiled.add( new PositionedPair( sym, cv ).setCoords( symp ) );
  }

  ArrayList<AST> res = new ArrayList<AST>();

  if (name != null) // named let ?
  {
    // Build the parameters for the call. Note that it must be done before we have bound the
    // lambda. The first parameter must be the lambda closure, and the last parameter must be an
    // a list (empty in this case), per apply's specification
    AST cp[] = new AST[compiled.size()+2];
    for ( int i = 0; i < compiled.size(); ++i )
      cp[i+1] = (AST) compiled.get(i).getCdr();
    cp[cp.length-1] = AST.Lit.LIT_NULL;

    // Bind the lambda
    VarBinding lambdaBinding = scope.bindVar( name );

    // Build a "fake" list of formal parameters for the lambda
    Pair formalParams = Pair.NULL;
    for ( int i = compiled.size()-1; i >= 0; --i )
    {
      Pair s_cv = compiled.get(i);
      formalParams = new PositionedPair(s_cv.getCar(), formalParams ).setCoords( s_cv );
    }

    AST mkLambda = new Compiler(this).compileLambda( scope, next, formalParams, body, true, false, name );
    res.add( new AST.SetBang( c(next), scope, lambdaBinding, mkLambda ) );

    cp[0] = new AST.Var( c(next), scope, lambdaBinding );

    if (tail)
      m_haveTailCall = true;
    res.add( new AST.Apply( c(next), cp ) );

    return makeBody( res, needResult );
  }
  else // regular let
  {
    // Declare the symbols and initialize the bindings
    for ( Pair s_cv : compiled )
    {
      Symbol sym  = (Symbol) s_cv.getCar();
      AST cv = (AST) s_cv.getCdr();

      if (scope.localLookupVar( sym ) != null)
        return parseError( s_cv, "Duplicate binding for variable "+ sym );

      VarBinding binding = scope.bindVar( sym );
      res.add( new AST.SetBang( cv, scope, binding, cv ) );
    }

    return appendInstr( res, compileBody( true, BodyLevel.INNER, scope, body, needResult, tail ) );
  }
}

// FIXME: check for init errors at compile time and runtime. For now we simply init the
// variables with UNSPEC
private AST compileLetrecStar ( Scope scope, Pair next, boolean needResult, boolean tail )
        throws InvalidList
{
  Pair bindings = ncdr(next);
  Pair body = cdr( bindings );

  ArrayList<AST> res = new ArrayList<AST>();

  // Bind all variables and initialize them to UNSPEC in a new scope
  scope = new Scope( scope, true );
  for ( Pair curBinding = carPair(bindings); curBinding != Pair.NULL; curBinding = cdr(curBinding) )
  {
    Pair symp = carPair(curBinding);
    Symbol sym = carSymbol( symp );
    Pair valuep = ncdr(symp);
    needNullCdr( valuep );

    if (scope.localLookupVar( sym ) != null)
      return parseError( symp, "Duplicate binding for variable "+ sym );
    VarBinding binding = scope.bindVar( sym );
    // NOTE: we don't need to explicitly initialize variables with UNSPEC
    // res.add( new AST.SetBang( sym, 0, binding.index, AST.Lit.LIT_UNSPEC ) );
  }

  // Now compile the init values and set them in order
  for ( Pair curBinding = carPair(bindings); curBinding != Pair.NULL; curBinding = cdr(curBinding) )
  {
    Pair symp = carPair(curBinding);
    Symbol sym = carSymbol( symp );
    Pair value = ncdr(symp);

    VarBinding binding = scope.localLookupVar( sym );
    AST cv = compileValue( scope, value, true, false );
    res.add( new AST.SetBang( cv, scope, binding, cv ) );
  }

  return appendInstr( res, compileBody( true, BodyLevel.INNER, scope, body, needResult, tail ) );
}

private AST compileQuote ( Scope scope, Pair pair, boolean needResult, boolean tail )
        throws InvalidList
{
  pair = ncdr(pair); // Get the parameter
  AST value = new AST.Lit( c(pair), pair.getCar());
  needNullCdr( pair );
  return value;
}

private AST compileDefine ( Scope scope, Pair next, boolean needResult, boolean tail )
        throws InvalidList
{
  Pair pair = next;
  AST value = null;
  VarBinding sbinding;

  pair = ncdr(pair);
  if (pair.getCar() instanceof VarBinding)
  {
    sbinding = (VarBinding) pair.getCar();

    if (pair.getCdr() != Pair.NULL)
    {
      pair = cdr(pair);
      value = compileValue( scope, pair, true, false );
      needNullCdr( pair );
    }
  }
  else if (pair.getCar() instanceof Pair && ((Pair) pair.getCar()).getCar() instanceof VarBinding) // Defining a function ?
  {
    Pair inner = (Pair) pair.getCar();
    sbinding = (VarBinding) inner.getCar();

    Pair body = cdr(pair);
    if (body == Pair.NULL)
      return parseError( pair, "Bad (define ()) form syntax: missing body" );

    value = new Compiler(this).compileLambda(scope, pair, inner.getCdr(), body, true, false, sbinding.sym );
  }
  else
    return parseError( pair, "Bad (define) form syntax" );

  if (value != null)
    return new AST.SetBang( c(next), scope, sbinding, value );
  else if (needResult)
    return AST.Lit.LIT_UNSPEC;
  else
    return null;
}

/**
 * If the parameter is a symbol and has not been redefined, returns its
 * {@link net.sf.p1lang.scheme.SymCode}
 *
 * @param scope the current scope
 * @param o the object to check
 * @return the symcode or {@link net.sf.p1lang.scheme.SymCode#NONE}
 */
private final SymCode checkSpecial ( Scope scope, Object o )
{
  if (o instanceof Symbol)
  {
    Symbol sym = (Symbol) o;
    if (scope.lookupAny(sym) == null)
      return sym.code;
  }
  return SymCode.NONE;
}

/**
 * We used this checked exception internally, to simplify handling of some common errors.
 * It is thrown by some internal functions when their encounter an error, after they have already
 * reported the error. The purpose is to excut quickly from the outer function.
 */
private static final class InvalidList extends Exception
{
  private static final long serialVersionUID = -3365761559673041149L;
}

private Pair carPair ( Pair p ) throws InvalidList
{
  if (p.getCar() != Pair.NULL && !(p.getCar() instanceof Pair))
  {
    parseError( p, "Invalid syntax: list required" );
    throw new InvalidList();
  }
  return (Pair) p.getCar();
}

private Symbol carSymbol ( Pair p ) throws InvalidList
{
  if (!(p.getCar() instanceof Symbol))
  {
    parseError( p, "Invalid syntax: symbol required" );
    throw new InvalidList();
  }
  return (Symbol) p.getCar();
}

private void needNullCdr ( Pair p ) throws InvalidList
{
  if (p.getCdr() != Pair.NULL)
  {
    parseError( p, "Invalid syntax: list is too long" );
    throw new InvalidList();
  }
}

private Pair cdr ( Pair p ) throws InvalidList
{
  if (p == Pair.NULL)
  {
    parseError( null, "(cdr '())" );
    throw new InvalidList();
  }
  if (!(p.getCdr() instanceof Pair))
  {
    parseError( p, "Syntax must be a proper list" );
    throw new InvalidList();
  }
  return (Pair) p.getCdr();
}

private Pair ncdr ( Pair p ) throws InvalidList
{
  if (p.getCdr() == Pair.NULL)
  {
    parseError( p, "Invalid syntax - list is too short" );
    throw new InvalidList();
  }
  return cdr(p);
}

private static final List<AST> newBlock()
{
  return new LinkedList<AST>();
}

private static boolean isListCyclic ( Object datum )
{
/*
  return datum != Pair.NULL && datum instanceof Pair &&
         isListRecursive_((Pair) datum, new IdentityHashMap<Object, Object>());
*/
  return false; // FIXME: how to check ???
}

/*
private static boolean isListRecursive_ ( Pair list, IdentityHashMap<Object,Object> visited )
{
  assert( list != Pair.NULL );
  do
  {
    if (visited.put( list, list ) != null)
      return true;

    if (list.car != Pair.NULL && list.car instanceof Pair)
    {
      if (isListRecursive_((Pair) list.car, visited ))
        return true;
    }
  }
  while (list.cdr instanceof Pair && (list = (Pair)list.cdr) != Pair.NULL);
  return false;
}
*/

private final void spliceInstr ( List<AST> block, AST t )
{
  if (t != null)
    if (t instanceof AST.Begin)
      block.addAll( Arrays.asList(((AST.Begin)t).m_body) );
    else
      block.add( t );
}

private final AST appendInstr ( List<AST> res, AST b )
{
  if (res == null)
    return b;

  spliceInstr( res, b );

  if (res.size() == 0)
    return AST.Lit.LIT_UNSPEC;
  else if (res.size() == 1)
    return res.get(0);
  else
    return new AST.Begin( res.get(0), res.toArray(new AST[res.size()]) );
}

private final AST appendInstr ( AST a, AST b )
{
  if (a == null)
    return b;
  if (b == null)
    return a;

  ArrayList<AST> res = new ArrayList<AST>();
  spliceInstr( res, a );
  return appendInstr( res, b );
}

private static final ISourceCoords c ( Pair p )
{
  return p instanceof PositionedPair ? (PositionedPair)p : null;
}

private final AST parseError ( Pair loc, String message, Object ... args )
{
  m_errors.error( (loc instanceof PositionedPair) ? (PositionedPair)loc : null, null, message, args );
  return AST.Lit.LIT_UNSPEC;
}

} // class

