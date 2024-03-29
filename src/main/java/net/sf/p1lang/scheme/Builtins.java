/*
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * DO NOT EDIT
 * This file automatically generated by Builtins-gen.scm.
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 */

package net.sf.p1lang.scheme;

import java.util.HashMap;

import net.sf.p1lang.scheme.AST.Builtin;

final class Builtins
{
private Builtins () {};

static final class NullPred extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public NullPred ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return a == Pair.NULL;
    }
  }

static final class PairPred extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public PairPred ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return a != Pair.NULL && a instanceof Pair;
    }
  }

static final class BooleanPred extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public BooleanPred ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return a instanceof Boolean;
    }
  }

static final class SymbolPred extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public SymbolPred ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return a instanceof Symbol;
    }
  }

static final class CharPred extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public CharPred ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return a instanceof Character;
    }
  }

static final class StringPred extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public StringPred ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return a instanceof String;
    }
  }

static final class ProcedurePred extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public ProcedurePred ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return a instanceof Closure;
    }
  }

static final class VectorPred extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public VectorPred ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return a instanceof Object[];
    }
  }

static final class NumberPred extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public NumberPred ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return a instanceof SchemeNumber;
    }
  }

static final class ComplexPred extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public ComplexPred ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return a instanceof SchemeNumber;
    }
  }

static final class RealPred extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public RealPred ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return a instanceof SchemeInteger ||
             a instanceof SchemeRational ||
             a instanceof SchemeReal;
    }
  }

static final class RationalPred extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public RationalPred ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return a instanceof SchemeInteger ||
             a instanceof SchemeRational;
    }
  }

static final class IntegerPred extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public IntegerPred ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return a instanceof SchemeInteger;
    }
  }

static final class ExactPred extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public ExactPred ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return a instanceof SchemeNumber && ((SchemeNumber)a).isExact();
    }
  }

static final class Eq extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public Eq ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
return a == b;
    }
  }

static final class Eqv extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public Eqv ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
    if (a == b)
      return true;
    if (a instanceof SchemeNumber)
      return (b instanceof SchemeNumber) && ((SchemeNumber)a).cmp( (SchemeNumber)b ) == 0;
    if (a instanceof Character)
      return (b instanceof Character) && ((Character)a).charValue() == ((Character)b).charValue();
    return false;

    }
  }

static final class NumberLE extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public NumberLE ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
return ((SchemeNumber)a).cmp( (SchemeNumber)b ) <= 0;
    }
  }

static final class NumberLT extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public NumberLT ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
return ((SchemeNumber)a).cmp( (SchemeNumber)b ) < 0;
    }
  }

static final class NumberEQ extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public NumberEQ ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
return ((SchemeNumber)a).cmp( (SchemeNumber)b ) == 0;
    }
  }

static final class Add extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public Add ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
return ((SchemeNumber)a).add( (SchemeNumber)b );
    }
  }

static final class Sub extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public Sub ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
return ((SchemeNumber)a).sub( (SchemeNumber)b );
    }
  }

static final class Mul extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public Mul ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
return ((SchemeNumber)a).mul( (SchemeNumber)b );
    }
  }

static final class Div extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public Div ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
return ((SchemeNumber)a).div( (SchemeNumber)b );
    }
  }

static final class Quotient extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public Quotient ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
return ((SchemeInteger)a).quotient( (SchemeInteger)b );
    }
  }

static final class Remainder extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public Remainder ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
return ((SchemeInteger)a).remainder( (SchemeInteger)b );
    }
  }

static final class Cons extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public Cons ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
return new Pair( a, b );
    }
  }

static final class SetCar extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public SetCar ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
((Pair)a).setCarBang( b ); return Unspec.UNSPEC;
    }
  }

static final class SetCdr extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public SetCdr ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
((Pair)a).setCdrBang( b ); return Unspec.UNSPEC;
    }
  }

static final class Car extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public Car ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
if (a == Pair.NULL) throw new PositionedError( this, "car of null" );
  return ((Pair)a).getCar();
    }
  }

static final class Cdr extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public Cdr ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
if (a == Pair.NULL) throw new PositionedError( this, "cdr of null" );
  return ((Pair)a).getCdr();
    }
  }

static final class SymbolToString extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public SymbolToString ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return ((Symbol)a).name;
    }
  }

static final class Compare2Strings extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public Compare2Strings ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
return SchemeFixInt.make( ((String)a).compareTo( (String)b ) );
    }
  }

static final class StringLength extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public StringLength ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return SchemeFixInt.make( ((String)a).length() );
    }
  }

static final class StringRef extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public StringRef ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
return Character.valueOf( ((String)a).charAt( ((SchemeNumber)b).toInteger().toJavaInt() ) );
    }
  }

static final class StringUpcase extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public StringUpcase ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return ((String)a).toUpperCase();
    }
  }

static final class StringDowncase extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public StringDowncase ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return ((String)a).toLowerCase();
    }
  }

static final class CharToInteger extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public CharToInteger ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return SchemeFixInt.make( (int)((Character)a).charValue() );
    }
  }

static final class CharDowncase extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public CharDowncase ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return Character.valueOf( Character.toLowerCase((Character)a) );
    }
  }

static final class Exact extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public Exact ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return ((SchemeNumber)a).toExact();
    }
  }

static final class Inexact extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public Inexact ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return ((SchemeNumber)a).toInexact();
    }
  }

static final class Numerator extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public Numerator ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return ((SchemeNumber)a).numerator();
    }
  }

static final class Denominator extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public Denominator ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return ((SchemeNumber)a).denominator();
    }
  }

static final class RealPart extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public RealPart ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return ((SchemeNumber)a).realPart();
    }
  }

static final class ImagPart extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public ImagPart ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return ((SchemeNumber)a).imagPart();
    }
  }

static final class Expt extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public Expt ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
return ((SchemeNumber)a).expt((SchemeNumber)b);
    }
  }

static final class MakeVector extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public MakeVector ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
Object[] res = new Object[((SchemeNumber)a).toInteger().toJavaInt()];
   // Help the compiler. Hopefully it knows than len is nonnegative
   int len = res.length;
   for ( int i = 0; i < len; ++i )
     res[i] = b;
   return res;
    }
  }

static final class VectorLen extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=1;
    public static final boolean HAVE_REST=false;
    public VectorLen ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
return SchemeFixInt.make( ((Object[])a).length );
    }
  }

static final class VectorRef extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public VectorRef ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
return ((Object[])a)[((SchemeNumber)b).toInteger().toJavaInt()];
    }
  }

static final class VectorSet extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=3;
    public static final boolean HAVE_REST=false;
    public VectorSet ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
Object c=v[2].evalValue(env,ctx);
((Object[])a)[((SchemeNumber)b).toInteger().toJavaInt()]=c;
  return Unspec.UNSPEC;
    }
  }

static final class Vector extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=0;
    public static final boolean HAVE_REST=true;
    public Vector ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
        int len = v.length;
   Object[] res = new Object[len];
   for ( int i = 0; i < len; ++i )
     res[i] = v[i].evalValue(env,ctx);
   return res;
    }
  }

static final class LitUnspec extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=0;
    public static final boolean HAVE_REST=false;
    public LitUnspec ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      return Unspec.UNSPEC;
    }
  }

static final class Error extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=true;
    public Error ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {

       Object who = v[0].evalValue(env,ctx);
       Object message = v[1].evalValue(env,ctx);
       StringBuilder res = new StringBuilder();
       res.append( "Application error:");
       if (who != Boolean.FALSE)
         res.append( who.toString() ).append(':');
       res.append( message.toString() );
       if (v.length > 2)
         res.append( '(' );
       for ( int i = 2; i < v.length; ++i )
       {
         if (i > 2)
           res.append( ' ' );
         res.append( v[i].evalValue(env,ctx).toString() );
       }
       if (v.length > 2)
         res.append( ')' );
       throw new PositionedError( this, res.toString() );

    }
  }

static final class SyntaxError extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=2;
    public static final boolean HAVE_REST=false;
    public SyntaxError ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      Object a=v[0].evalValue(env,ctx);
Object b=v[1].evalValue(env,ctx);
throw new PositionedError( a instanceof ISourceCoords?(ISourceCoords)a:null, b.toString() );
    }
  }

static void define ( SymbolMap sm, HashMap<Symbol,BuiltinFactory> map )
{
  map.put( sm.newSymbol("SyntaxError"), new BuiltinFactory<SyntaxError>(SyntaxError.class) );
  map.put( sm.newSymbol("Error"), new BuiltinFactory<Error>(Error.class) );
  map.put( sm.newSymbol("LitUnspec"), new BuiltinFactory<LitUnspec>(LitUnspec.class) );
  map.put( sm.newSymbol("Vector"), new BuiltinFactory<Vector>(Vector.class) );
  map.put( sm.newSymbol("VectorSet"), new BuiltinFactory<VectorSet>(VectorSet.class) );
  map.put( sm.newSymbol("VectorRef"), new BuiltinFactory<VectorRef>(VectorRef.class) );
  map.put( sm.newSymbol("VectorLen"), new BuiltinFactory<VectorLen>(VectorLen.class) );
  map.put( sm.newSymbol("MakeVector"), new BuiltinFactory<MakeVector>(MakeVector.class) );
  map.put( sm.newSymbol("Expt"), new BuiltinFactory<Expt>(Expt.class) );
  map.put( sm.newSymbol("ImagPart"), new BuiltinFactory<ImagPart>(ImagPart.class) );
  map.put( sm.newSymbol("RealPart"), new BuiltinFactory<RealPart>(RealPart.class) );
  map.put( sm.newSymbol("Denominator"), new BuiltinFactory<Denominator>(Denominator.class) );
  map.put( sm.newSymbol("Numerator"), new BuiltinFactory<Numerator>(Numerator.class) );
  map.put( sm.newSymbol("Inexact"), new BuiltinFactory<Inexact>(Inexact.class) );
  map.put( sm.newSymbol("Exact"), new BuiltinFactory<Exact>(Exact.class) );
  map.put( sm.newSymbol("CharDowncase"), new BuiltinFactory<CharDowncase>(CharDowncase.class) );
  map.put( sm.newSymbol("CharToInteger"), new BuiltinFactory<CharToInteger>(CharToInteger.class) );
  map.put( sm.newSymbol("StringDowncase"), new BuiltinFactory<StringDowncase>(StringDowncase.class) );
  map.put( sm.newSymbol("StringUpcase"), new BuiltinFactory<StringUpcase>(StringUpcase.class) );
  map.put( sm.newSymbol("StringRef"), new BuiltinFactory<StringRef>(StringRef.class) );
  map.put( sm.newSymbol("StringLength"), new BuiltinFactory<StringLength>(StringLength.class) );
  map.put( sm.newSymbol("Compare2Strings"), new BuiltinFactory<Compare2Strings>(Compare2Strings.class) );
  map.put( sm.newSymbol("SymbolToString"), new BuiltinFactory<SymbolToString>(SymbolToString.class) );
  map.put( sm.newSymbol("Cdr"), new BuiltinFactory<Cdr>(Cdr.class) );
  map.put( sm.newSymbol("Car"), new BuiltinFactory<Car>(Car.class) );
  map.put( sm.newSymbol("SetCdr"), new BuiltinFactory<SetCdr>(SetCdr.class) );
  map.put( sm.newSymbol("SetCar"), new BuiltinFactory<SetCar>(SetCar.class) );
  map.put( sm.newSymbol("Cons"), new BuiltinFactory<Cons>(Cons.class) );
  map.put( sm.newSymbol("Remainder"), new BuiltinFactory<Remainder>(Remainder.class) );
  map.put( sm.newSymbol("Quotient"), new BuiltinFactory<Quotient>(Quotient.class) );
  map.put( sm.newSymbol("Div"), new BuiltinFactory<Div>(Div.class) );
  map.put( sm.newSymbol("Mul"), new BuiltinFactory<Mul>(Mul.class) );
  map.put( sm.newSymbol("Sub"), new BuiltinFactory<Sub>(Sub.class) );
  map.put( sm.newSymbol("Add"), new BuiltinFactory<Add>(Add.class) );
  map.put( sm.newSymbol("NumberEQ"), new BuiltinFactory<NumberEQ>(NumberEQ.class) );
  map.put( sm.newSymbol("NumberLT"), new BuiltinFactory<NumberLT>(NumberLT.class) );
  map.put( sm.newSymbol("NumberLE"), new BuiltinFactory<NumberLE>(NumberLE.class) );
  map.put( sm.newSymbol("Eqv"), new BuiltinFactory<Eqv>(Eqv.class) );
  map.put( sm.newSymbol("Eq"), new BuiltinFactory<Eq>(Eq.class) );
  map.put( sm.newSymbol("ExactPred"), new BuiltinFactory<ExactPred>(ExactPred.class) );
  map.put( sm.newSymbol("IntegerPred"), new BuiltinFactory<IntegerPred>(IntegerPred.class) );
  map.put( sm.newSymbol("RationalPred"), new BuiltinFactory<RationalPred>(RationalPred.class) );
  map.put( sm.newSymbol("RealPred"), new BuiltinFactory<RealPred>(RealPred.class) );
  map.put( sm.newSymbol("ComplexPred"), new BuiltinFactory<ComplexPred>(ComplexPred.class) );
  map.put( sm.newSymbol("NumberPred"), new BuiltinFactory<NumberPred>(NumberPred.class) );
  map.put( sm.newSymbol("VectorPred"), new BuiltinFactory<VectorPred>(VectorPred.class) );
  map.put( sm.newSymbol("ProcedurePred"), new BuiltinFactory<ProcedurePred>(ProcedurePred.class) );
  map.put( sm.newSymbol("StringPred"), new BuiltinFactory<StringPred>(StringPred.class) );
  map.put( sm.newSymbol("CharPred"), new BuiltinFactory<CharPred>(CharPred.class) );
  map.put( sm.newSymbol("SymbolPred"), new BuiltinFactory<SymbolPred>(SymbolPred.class) );
  map.put( sm.newSymbol("BooleanPred"), new BuiltinFactory<BooleanPred>(BooleanPred.class) );
  map.put( sm.newSymbol("PairPred"), new BuiltinFactory<PairPred>(PairPred.class) );
  map.put( sm.newSymbol("NullPred"), new BuiltinFactory<NullPred>(NullPred.class) );
}

} // class
