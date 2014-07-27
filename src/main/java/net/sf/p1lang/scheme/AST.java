/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.io.Serializable;
import java.io.ObjectStreamException;

/**
 * The abstract base of all instructions.
 *
 * <p>Instructions are a compiled internal representation of executable Scheme code. They form an
 * expression tree and what resembles a thread interpreter (to the best approximation possible
 * in Java).
 *
 * <p>Each instruction has two forms: {@link #evalValue(Object[], EvalContext) eval-value} and
 * {@link #evalCont(Object[], EvalContext) eval-continuation}. {@code eval-continuation}
 * is used only in a tail context and returns a continuation. {@code eval-value} is used for
 * regular recursive evaluation.
 *
 * <p>The purpose of maintaining two forms is to avoid managing an explicit value stack. Instead we
 * use Java's native stack. The drawback is that we don't support {@code call/cc}.
 *
 * <p>All instructions are defined here as static inner classes.
 */
abstract class AST implements ISourceCoords, Serializable
{
private static final long serialVersionUID = -3119576591190599097L;

private String m_fileName;
/**
 * Combined line and column. See {@link net.sf.p1lang.scheme.SourceCoords#combinePosition(int, int)}
 */
private int m_position;

protected AST ( ISourceCoords coords )
{
  if (coords != null)
  {
    m_fileName = coords.getFileName();
    m_position = coords.getPosition();
  }
}

public final void setPosition ( int line, int column )
{
  m_position = SourceCoords.combinePosition( line, column );
}

public final boolean havePosition ()
{
  return m_position != 0;
}

public final int getPosition ()
{
  return m_position;
}

public final int getLine ()
{
  return SourceCoords.extractLine( m_position );
}

public final int getColumn ()
{
  return SourceCoords.extractColumn( m_position );
}

public String getFileName ()
{
  return m_fileName;
}

public boolean coordsEqual ( final ISourceCoords x )
{
  return x != null && x.getPosition() == m_position &&
         (m_fileName!=null?m_fileName:"").equals( x.getFileName() );
}

public AST evalCont ( Object[] env, EvalContext ctx ) throws SchemeError
{
  ctx.outEnv = env;
  return this;
}

public abstract Object evalValue ( Object[] env, EvalContext ctx ) throws SchemeError;

public abstract Object dis ();

public void handleException ( Exception e ) throws SchemeError, RuntimeException
{
  if (e instanceof ExecuteContinuation)
    throw (ExecuteContinuation)e;
  else if (e instanceof SchemeError)
  {
    if (!havePosition())
      throw (SchemeError)e;

    if (e instanceof PositionedError)
    {
      PositionedError pe = (PositionedError) e;
      pe.addSchemeStackTrace( this );
      throw pe;
    }
    else
      throw new PositionedError( this, e.getMessage(), e );
  }
  else if (havePosition())
    throw new PositionedError( this, e.getMessage(), e );
  else if (e instanceof RuntimeException)
    throw (RuntimeException)e;
  else
    throw new RuntimeException( e ); // unlikely
}

/**
 *
 */
public static class Lit extends AST
{
  private static final long serialVersionUID = -7246979122032890529L;

  public static final Lit LIT_UNSPEC = new Lit( null, Unspec.UNSPEC )
  {
    private static final long serialVersionUID = 1865862345052235536L;
    private Object readResolve() throws ObjectStreamException
    {
      return LIT_UNSPEC;
    }
  };

  public static final Lit LIT_NULL = new Lit( null, Pair.NULL )
  {
    private static final long serialVersionUID = -8540791133442400471L;
    private Object readResolve() throws ObjectStreamException
    {
      return LIT_NULL;
    }
  };

  public final Serializable datum;

  public Lit ( ISourceCoords coords, Object datum ) { super(coords); this.datum = (Serializable) datum; }

  public final Object evalValue ( final Object[] env, final EvalContext ctx ) throws EvalError
  {
    return datum;
  }

  public String toString ()
  {
    return "(lit "+ datum.toString() +")";
  }

  public Object dis ()
  {
    return toString();
  }
}

public static final class MakeVector extends AST
{
  private static final long serialVersionUID = 5216016765939283504L;
  public final AST[] values;

  protected MakeVector ( ISourceCoords coords, final AST[] values )
  {
    super(coords);
    this.values = values;
  }

  public Object evalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError
  {
    Object[] res = new Object[values.length];
    int len = values.length;
    for ( int i = 0; i < len; ++i )
      res[i] = values[i].evalValue( env, ctx );
    return res;
  }

  public String toString ()
  {
    StringBuilder res = new StringBuilder();
    res.append( "(make-vector " );
    for ( AST v : values )
      res.append( v.toString() ).append( ' ' );
    res.append( ')' );
    return res.toString();
  }

  public Object dis ()
  {
    Pair p = Pair.NULL;
    for ( int i = values.length - 1; i >= 0; --i )
      p = new Pair( values[i].dis(), p );
    return new Pair( "make-vector", p );
  }
}

/**
 *
 */
public static final class Var extends AST
{
  private static final long serialVersionUID = 1072312267094508370L;

  /** How many environments up to go to access the binding's environment. */
  private final int envIndex;
  /** Index of the binding in its environment */
  private final int bindingIndex;
  private final Symbol name;

  public Var ( ISourceCoords coords, Scope curScope, VarBinding binding )
  {
    super(coords);
    this.envIndex = curScope.envLevel - binding.scope.envLevel;
    if (this.envIndex < 0)
      throw new IllegalArgumentException();
    this.bindingIndex = binding.index;
    this.name = binding.sym;
  }

  public final Object evalValue ( final Object[] env, final EvalContext ctx ) throws EvalError
  {
    int i = envIndex;
    Object[] e = env;
    while (i > 0)
    {
      e = (Object[]) e[0];
      --i;
    }
    return e[bindingIndex];
  }

  public String toString ()
  {
    return "(var "+name+":"+envIndex+":"+bindingIndex+")";
  }

  public Object dis ()
  {
    return toString();
  }
}

/**
 *
 */
public static final class SetBang extends AST
{
  private static final long serialVersionUID = -2386977834200816036L;

  /** How many environments up to go to access the binding's environment. */
  private final int envIndex;
  /** Index of the binding in its environment */
  private final int bindingIndex;
  private final AST value;
  private final Symbol name;

  public SetBang ( ISourceCoords coords, Scope curScope, VarBinding binding, AST value )
  {
    super(coords);
    this.envIndex = curScope.envLevel - binding.scope.envLevel;
    if (this.envIndex < 0)
      throw new IllegalArgumentException();
    this.bindingIndex = binding.index;
    this.value = value;
    name = binding.sym;
  }

  public final Object evalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError
  {
    Object[] e = env;
    int i = envIndex;
    while (i > 0)
    {
      e = (Object[]) env[0];
      --i;
    }
    e[bindingIndex] = value.evalValue( env, ctx );
    return Unspec.UNSPEC;
  }

  public String toString ()
  {
    return "(set! "+name+":"+envIndex+":"+bindingIndex+" "+ value +")";
  }

  public Object dis ()
  {
    return new Pair( "set! "+name+":"+envIndex+":"+bindingIndex,
                     new Pair( value.dis() ) );
  }
}

public static final class If extends AST
{
  private static final long serialVersionUID = 2452307072275626425L;

  private final AST m_evalCond, m_evalThen, m_evalElse;

  public If ( ISourceCoords coords,
              final AST evalCond, final AST evalThen, final AST evalElse )
  {
    super(coords);
    assert( evalCond != null && evalThen != null && evalElse != null );
    m_evalCond = evalCond;
    m_evalThen = evalThen;
    m_evalElse = evalElse;
  }

  public final AST evalCont ( final Object[] env, final EvalContext ctx ) throws SchemeError
  {
    AST res = m_evalCond.evalValue( env, ctx ) != Boolean.FALSE ? m_evalThen : m_evalElse;
    ctx.outEnv = env;
    return res;
  }

  public final Object evalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError
  {
    return m_evalCond.evalValue( env, ctx ) != Boolean.FALSE ?
              m_evalThen.evalValue( env, ctx ) : m_evalElse.evalValue( env, ctx );
  }

  public String toString ()
  {
    return "(if "+ m_evalCond.toString() +" "+ m_evalThen.toString() +" "+ m_evalElse.toString()
            +")";
  }

  public Object dis ()
  {
    return new Pair("if",
                    new Pair( m_evalCond.dis(),
                              new Pair( m_evalThen.dis(), new Pair( m_evalElse.dis() ))));
  }
}

public static final class MakeClosure extends AST
{
  private static final long serialVersionUID = 2420718999386736497L;

  private final Lambda m_lambda;

  public MakeClosure ( ISourceCoords coords, final Lambda lambda )
  {
    super(coords);
    m_lambda = lambda;
  }
  public final Object evalValue ( final Object[] env, final EvalContext ctx ) throws EvalError
  {
    return new Closure(env, m_lambda);
  }

  public String toString ()
  {
    return "(make-closure "+ m_lambda.toString() +" "+ m_lambda.body.toString() +")";
  }

  public Object dis ()
  {
    return
      new Pair( "make-closure "+m_lambda.toString(),
                new Pair( m_lambda.body != null ? m_lambda.body.dis() : "java" ));
  }
}

public static abstract class Builtin extends AST
{
  private static final long serialVersionUID = 1983752562273478086L;

  protected final AST[] v;

  public Builtin ( ISourceCoords coords, final AST[] values )
  {
    super(coords);
    assert( values != null );
    this.v = values;
  };

  public String toString ()
  {
    StringBuilder res = new StringBuilder(32+v.length*32);

    String cls = getClass().getName();
    int n;
    if ((n = cls.lastIndexOf( '.' )) >= 0)
      cls = cls.substring( n+1 );

    res.append( '(' ).append( cls );
    for (AST aV : v)
      res.append(' ').append(aV.toString());
    res.append( ')' );
    return res.toString();
  }

  public Object dis ()
  {
    String cls = getClass().getName();
    int n;
    if ((n = cls.lastIndexOf( '.' )) >= 0)
      cls = cls.substring( n+1 );

    Pair res = Pair.NULL;
    for ( int i = this.v.length-1; i >= 0; --i )
      res = new Pair( this.v[i].dis(), res );
    return
      new Pair( "builtin "+ cls, res );
  }

  public final Object evalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError
  {
    try
    {
      return builtinEvalValue( env, ctx );
    }
    catch (Exception e)
    {
      handleException( e );
      return null; // unreachable
    }
  }

  protected abstract Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError;
}

/**
 *
 */
public static final class Apply extends Builtin
{
  private static final long serialVersionUID = -6370863057952752484L;

  public static final int PARAM_COUNT=2;
  public static final boolean HAVE_REST=true;

  public Apply ( ISourceCoords coords, final AST[] values )
  {
    super( coords, values );
  }

  private final Closure evalTarget ( final Object[] env, final EvalContext ctx ) throws SchemeError
  {
    Object target = v[0].evalValue( env, ctx );
    if (!(target instanceof Closure))
      throw new EvalError( this, "Call of non-procedure" );
    return (Closure)target;
  }

  private final Object[] buildEnv ( final Object[] env, final EvalContext ctx, Closure closure )
          throws SchemeError
  {
    Lambda proc = closure.lambda;

    // If the last parameter is non-null, it must evaluate to a list. Conceptually (apply...)
    // builds a list from the first parameters and appends the last parameter (which itself is a list).
    // Then it applies this list consecutively to the procedure. The remainder of the list is passed
    // to the "rest" parameter, if any.
    //
    // The complexity here comes from trying to avoid always going through a list in the case
    // when nor apply nor its target have a "rest" parameter.

    int actualParamCount = v.length-2; // NOTE: apply's last param is a list of more params
    Pair restArgs;
    if (v[v.length-1] == Lit.LIT_NULL) // Short-circuit this frequent case
    {
      // No rest arguments passed.
      restArgs = Pair.NULL;
    }
    else
    {
      // Evaluate apply's "restArg". It must be a (possibly empty) list of more parameters
      Object t = v[v.length-1].evalValue( env, ctx );
      if (t != Pair.NULL && !(t instanceof Pair))
        throw new EvalError( this, "Last parameter of (apply...) is not a list" );
      restArgs = (Pair)t;
      // Count the rest arguments
      try
      {
        for ( Pair p = restArgs; p != Pair.NULL; p = (Pair) p.getCdr())
          ++actualParamCount;
      }
      catch (ClassCastException e)
      {
        throw new EvalError( this, "Last parameter of (apply...) is not a proper list" );
      }
    }

    // Validate the parameter count
    if (actualParamCount < proc.paramCount)
      throw new EvalError( this, String.format("Call to %s expects %s%d parameters",
                                               proc, proc.haveRest?"at least ":"", proc.paramCount) );
    if (actualParamCount > proc.paramCount && !proc.haveRest)
      throw new EvalError( this, String.format("Call to %s expects %s%d parameters",
                                               proc, proc.haveRest?"at least ":"", proc.paramCount) );

    // Create the callee's environment
    Object[] callEnv;
    if (proc.envSize > 0)
    {
      callEnv = new Object[proc.envSize];
      callEnv[Scope.PARENT_SLOT] = closure.parentEnv;

      // Fill the regular parameters.
      for ( int paramIndex = 0; paramIndex < proc.paramCount; ++paramIndex )
      {
        Object t;
        if (paramIndex < v.length-2)
          t = v[paramIndex+1].evalValue( env, ctx );
        else
        {
          t = restArgs.getCar();
          restArgs = (Pair) restArgs.getCdr();
        }
        callEnv[Scope.RESERVED_SLOTS+paramIndex] = t;
      }

      if (proc.haveRest)
      {
        // The rest of the parameters (which could come from m_evalParams, restArgs or both)
        // must be concatenated into a list and supplied as the last actual parameter to
        // the target.
        // We must build the list in reverse (since our pairs are immutable). We just start
        // cons-ing parameters on top of restArgs in reverse order.

        for ( int i = v.length-3; i >= proc.paramCount; --i )
          restArgs = new Pair(v[i+1].evalValue( env, ctx ), restArgs);
        callEnv[Scope.RESERVED_SLOTS+proc.paramCount] = restArgs;
      }

      // Fill the rest of the environment with unspecified values
      {
        int end = proc.envSize;
        int i = Scope.RESERVED_SLOTS+proc.paramCount+(proc.haveRest?1:0);
        for ( ; i < end; ++i )
          callEnv[i] = Unspec.UNSPEC;
      }
    }
    else
      callEnv = null;

    return callEnv;
  }

  public final AST evalCont ( final Object[] env, final EvalContext ctx ) throws SchemeError
  {
    Closure closure = evalTarget( env, ctx );
    ctx.outEnv = buildEnv( env, ctx, closure);
    Lambda proc = closure.lambda;
    if (proc instanceof JavaProcedure)
      return ((JavaProcedure)proc).cont;
    else
      return closure.lambda.body;
  }

  protected final Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError
  {
    Closure closure = evalTarget( env, ctx );
    Object[] callEnv = buildEnv( env, ctx, closure);
    Lambda proc = closure.lambda;

    if (proc instanceof JavaProcedure)
      return ((JavaProcedure)proc).apply( callEnv );
    else
      return closure.lambda.body.evalValue( callEnv, ctx );
  }

  public String toString ()
  {
    StringBuilder res = new StringBuilder(32+v.length*32);

    res.append( "(apply ");
    for (AST aV : v)
      res.append(' ').append(aV.toString());
    res.append( ')' );
    return res.toString();
  }

  public Object dis ()
  {
    Pair res = Pair.NULL;
    for ( int i = this.v.length-1; i >= 0; --i )
      res = new Pair( this.v[i].dis(), res );
    return
      new Pair( "apply", res );
  }
}

/**
 * A special exception type used to transfer the execution to the call/cc site whenever a
 * continuation procedure is invoked.
 */
@SuppressWarnings({"serial"})
private static class ExecuteContinuation extends RuntimeException
{
  final ContinuationProcedure continuation;
  final Object result;
  ExecuteContinuation ( final ContinuationProcedure continuation, final Object result )
  {
    this.continuation = continuation;
    this.result = result;
  }
}

/**
 * A continuation procedure. Its purpose is to transfer the execution to the call/cc site by
 * throwing {@link net.sf.p1lang.scheme.AST.ExecuteContinuation}.
 */
@SuppressWarnings({"serial"})
private static class ContinuationProcedure extends JavaProcedure
{
  private boolean m_used;
  public ContinuationProcedure ()
  {
    super(1, false);
  }
  final void invalidate ()
  {
    m_used = true;
  }
  public Object apply ( final Object[] argv ) throws SchemeError
  {
    if (m_used)
      throw new SchemeError( "Invalid attempt to reuse continuation" );
    throw new ExecuteContinuation( this, argv[Scope.RESERVED_SLOTS+0] );
  }
}

// FIXME: this is a very limited implementation of call/cc:
//          - it isn't tail recursive
//          - continuations work only "upwards" and are one-shot
public static class CallCC extends Builtin
{
  private static final long serialVersionUID = 1L;
  public static final int PARAM_COUNT=1;
  public static final boolean HAVE_REST=false;

  public CallCC ( final ISourceCoords coords, final AST[] values )
  {
    super(coords, values);
  }

  private final Closure evalTarget ( final Object[] env, final EvalContext ctx ) throws SchemeError
  {
    Object target = v[0].evalValue( env, ctx );
    if (!(target instanceof Closure))
      throw new EvalError( this, "call/cc with non-procedure parameter" );
    return (Closure)target;
  }

  private final Object[] buildEnv ( final Object[] env, final EvalContext ctx, Closure closure,
                                    Closure continuation )
          throws SchemeError
  {
    Lambda proc = closure.lambda;

    // Validate the parameter count
    if (proc.paramCount == 0 && !proc.haveRest || proc.paramCount > 1)
      throw new EvalError( this, String.format("call/cc lambda must take one parameter") );

    // Create the callee's environment (copied from Apply)
    Object[] callEnv;
    if (proc.envSize > 0)
    {
      int paramIndex;
      callEnv = new Object[proc.envSize];
      callEnv[Scope.PARENT_SLOT] = closure.parentEnv;

      // Fill the regular parameters
      for ( paramIndex = 0; paramIndex < proc.paramCount; ++paramIndex )
        callEnv[Scope.RESERVED_SLOTS+paramIndex] = continuation;

      // Build a list with the "rest" parameters
      if (proc.haveRest)
      {
        Pair restParams = Pair.NULL;
        for ( int index = 0; index >= proc.paramCount; --index )
          restParams = new Pair( continuation, restParams );
        callEnv[Scope.RESERVED_SLOTS+paramIndex] = restParams;
        ++paramIndex;
      }

      // Fill the rest of the environment with unspecified values
      paramIndex += Scope.RESERVED_SLOTS;
      for ( int end = proc.envSize; paramIndex < end; ++paramIndex )
        callEnv[paramIndex] = Unspec.UNSPEC;
    }
    else
      callEnv = null;

    return callEnv;
  }

  public Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError
  {
    Closure closure = evalTarget( env, ctx );

    ContinuationProcedure continuation = new ContinuationProcedure();
    try
    {
      Object callEnv[] = buildEnv( env, ctx, closure, new Closure(env,continuation) );
      try
      {
        Lambda proc = closure.lambda;
        if (proc instanceof JavaProcedure)
        {
          try
          {
            return ((JavaProcedure)proc).apply( callEnv );
          }
          catch (Exception e)
          {
            handleException( e );
            return null; // unreachable
          }
        }
        else
          return closure.lambda.body.evalValue( callEnv, ctx );
      }
      catch (ExecuteContinuation ec)
      {
        if (ec.continuation != continuation)
          throw ec;
        return ec.result;
      }
    }
    finally
    {
      continuation.invalidate();
    }
  }
}

/**
 *
 */
public static final class Begin extends AST
{
  private static final long serialVersionUID = -2360355287570532933L;

  public final AST[] m_body;

  public Begin ( ISourceCoords coords, final AST[] body )
  {
    super(coords);
    assert( body != null );
    m_body = body;
  }

  public final AST evalCont ( final Object[] env, final EvalContext ctx ) throws SchemeError
  {
    AST res;
    if (m_body.length == 0)
      res = Lit.LIT_UNSPEC;
    else
    {
      int i;
      for ( i = 0; i < m_body.length-1; ++i )
        m_body[i].evalValue( env, ctx );
      res = m_body[i];
    }
    ctx.outEnv = env;
    return res;
  }

  public final Object evalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError
  {
    if (m_body.length == 0)
      return Unspec.UNSPEC;
    int i;
    for ( i = 0; i < m_body.length-1; ++i )
      m_body[i].evalValue( env, ctx );
    return m_body[i].evalValue( env, ctx );
  }

  public String toString ()
  {
    StringBuilder res = new StringBuilder( 32 + m_body.length*32 );
    res.append( "(begin" );
    for ( AST v : m_body )
      res.append( ' ' ).append( v.toString() );
    res.append( ')' );
    return res.toString();
  }

  public Object dis ()
  {
    Pair res = Pair.NULL;
    for ( int i = this.m_body.length-1; i >= 0; --i )
      res = new Pair( this.m_body[i].dis(), res );
    return
      new Pair( "begin", res );
  }
}


public static final class Trampoline extends AST
{
  private static final long serialVersionUID = 513918821698267255L;
  private final AST m_ast;

  public Trampoline ( ISourceCoords coords, final AST ast )
  {
    super(coords);
    m_ast = ast;
  }

  public final AST evalCont ( final Object[] env, final EvalContext ctx ) throws EvalError
  {
    ctx.outEnv = env;
    return m_ast;
  }

  public final Object evalValue ( Object[] env, final EvalContext ctx ) throws SchemeError
  {
    AST cur = m_ast;
    AST next;
    while ((next = cur.evalCont( env, ctx )) != cur)
    {
      env = ctx.outEnv;
      cur = next;
    }
    ctx.outEnv = null; // we don't want to keep extra environments alive
    try
    {
      return cur.evalValue( env, ctx );
    }
    catch (Exception e)
    {
      cur.handleException( e );
      return null; // unreachable
    }
  }

  public String toString ()
  {
    return "(tramp "+ m_ast.toString() +")";
  }

  public Object dis ()
  {
    return new Pair( "tramp", new Pair(m_ast.dis()) );
  }
}

} // class

