(define pred-list '() )

(define (disp pat)
  (if (pair? pat)
    (begin (disp (car pat)) (disp (cdr pat)))
    (if (not (null? pat)) (display pat))))


; Generate a a builtin
(define (gen-builtin-full name param-count have-rest body)
  (set! pred-list (cons name pred-list))
  (disp `(
  "static final class " ,name " extends Builtin {
    private static final long serialVersionUID = 1L;
    public static final int PARAM_COUNT=" ,param-count ";
    public static final boolean HAVE_REST=" ,have-rest ";
    public " ,name " ( ISourceCoords coords, AST[] values ) { super(coords,values); }
    protected Object builtinEvalValue ( final Object[] env, final EvalContext ctx ) throws SchemeError {
      " ,body "
    }
  }\n\n")))

; Generate a a builtin
(define (gen-builtin name param-count body)
  (gen-builtin-full name param-count "false" body))


;; Generate a builtin which extracts its parameters into local variables
;; named a, b, c, etc
(define (gen-xop name param-count body)
  (gen-builtin
    name param-count
    ; build the local variables initializaion
    (let loop ((bd '()) (i 0) (chr #\a))
      (if (< i param-count)
        (loop `(,bd "Object ",chr"=v[",i"].evalValue(env,ctx);\n")
              (+ i 1)
              (integer->char (+ 1 (char->integer chr))))
        (list bd body) ))))

(define (gen-pred name body)
  (gen-xop (string-append name "Pred") 1 body ))

(define (gen-inst-pred name type)
  (gen-pred name `("return a instanceof " ,type ";" )))


(display "\
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

")

(gen-pred      "Null" "return a == Pair.NULL;")
(gen-pred      "Pair" "return a != Pair.NULL && a instanceof Pair;")
(gen-inst-pred "Boolean" "Boolean")
(gen-inst-pred "Symbol" "Symbol")
(gen-inst-pred "Char" "Character")
(gen-inst-pred "String" "String")
(gen-inst-pred "Procedure" "Closure")
(gen-inst-pred "Vector" "Object[]")
(gen-inst-pred "Number" "SchemeNumber")
(gen-inst-pred "Complex" "SchemeNumber")
(gen-pred      "Real"
     "return a instanceof SchemeInteger ||
             a instanceof SchemeRational ||
             a instanceof SchemeReal;")
(gen-pred      "Rational"
     "return a instanceof SchemeInteger ||
             a instanceof SchemeRational;")
(gen-inst-pred "Integer" "SchemeInteger")

(gen-pred      "Exact" "return a instanceof SchemeNumber && ((SchemeNumber)a).isExact();")

(gen-xop "Eq" 2 "return a == b;")
(gen-xop "Eqv" 2
"    if (a == b)
      return true;
    if (a instanceof SchemeNumber)
      return (b instanceof SchemeNumber) && ((SchemeNumber)a).cmp( (SchemeNumber)b ) == 0;
    if (a instanceof Character)
      return (b instanceof Character) && ((Character)a).charValue() == ((Character)b).charValue();
    return false;
")

(gen-xop "NumberLE" 2 "return ((SchemeNumber)a).cmp( (SchemeNumber)b ) <= 0;")
(gen-xop "NumberLT" 2 "return ((SchemeNumber)a).cmp( (SchemeNumber)b ) < 0;")
(gen-xop "NumberEQ" 2 "return ((SchemeNumber)a).cmp( (SchemeNumber)b ) == 0;")

(gen-xop "Add" 2 "return ((SchemeNumber)a).add( (SchemeNumber)b );")
(gen-xop "Sub" 2 "return ((SchemeNumber)a).sub( (SchemeNumber)b );")
(gen-xop "Mul" 2 "return ((SchemeNumber)a).mul( (SchemeNumber)b );")
(gen-xop "Div" 2 "return ((SchemeNumber)a).div( (SchemeNumber)b );")
(gen-xop "Quotient" 2 "return ((SchemeInteger)a).quotient( (SchemeInteger)b );")
(gen-xop "Remainder" 2 "return ((SchemeInteger)a).remainder( (SchemeInteger)b );")

(gen-xop "Cons" 2 "return new Pair( a, b );")
(gen-xop "SetCar" 2 "((Pair)a).setCarBang( b ); return Unspec.UNSPEC;")
(gen-xop "SetCdr" 2 "((Pair)a).setCdrBang( b ); return Unspec.UNSPEC;")
(gen-xop "Car" 1
  "if (a == Pair.NULL) throw new PositionedError( this, \"car of null\" );
  return ((Pair)a).getCar();")
(gen-xop "Cdr" 1
  "if (a == Pair.NULL) throw new PositionedError( this, \"cdr of null\" );
  return ((Pair)a).getCdr();")

(gen-xop "SymbolToString" 1 "return ((Symbol)a).name;")
(gen-xop "Compare2Strings" 2 "return SchemeFixInt.make( ((String)a).compareTo( (String)b ) );")
(gen-xop "StringLength" 1 "return SchemeFixInt.make( ((String)a).length() );")
(gen-xop "StringRef" 2
  "return Character.valueOf( ((String)a).charAt( ((SchemeNumber)b).toInteger().toJavaInt() ) );")

(gen-xop "StringUpcase" 1 "return ((String)a).toUpperCase();")
(gen-xop "StringDowncase" 1 "return ((String)a).toLowerCase();")

(gen-xop "CharToInteger" 1 "return SchemeFixInt.make( (int)((Character)a).charValue() );")
(gen-xop "CharDowncase" 1 "return Character.valueOf( Character.toLowerCase((Character)a) ); ")

(gen-xop "Exact" 1 "return ((SchemeNumber)a).toExact();")
(gen-xop "Inexact" 1 "return ((SchemeNumber)a).toInexact();")

(gen-xop "Numerator" 1 "return ((SchemeNumber)a).numerator();")
(gen-xop "Denominator" 1 "return ((SchemeNumber)a).denominator();")

(gen-xop "RealPart" 1 "return ((SchemeNumber)a).realPart();")
(gen-xop "ImagPart" 1 "return ((SchemeNumber)a).imagPart();")

(gen-xop "Expt" 2 "return ((SchemeNumber)a).expt((SchemeNumber)b);")

(gen-xop "MakeVector" 2
  "Object[] res = new Object[((SchemeNumber)a).toInteger().toJavaInt()];
   // Help the compiler. Hopefully it knows than len is nonnegative
   int len = res.length;
   for ( int i = 0; i < len; ++i )
     res[i] = b;
   return res;")
(gen-xop "VectorLen" 1 "return SchemeFixInt.make( ((Object[])a).length );")
(gen-xop "VectorRef" 2 "return ((Object[])a)[((SchemeNumber)b).toInteger().toJavaInt()];")
(gen-xop "VectorSet" 3
  "((Object[])a)[((SchemeNumber)b).toInteger().toJavaInt()]=c;
  return Unspec.UNSPEC;")
(gen-builtin-full "Vector" 0 "true"
"  int len = v.length;
   Object[] res = new Object[len];
   for ( int i = 0; i < len; ++i )
     res[i] = v[i].evalValue(env,ctx);
   return res;")
(gen-builtin-full "LitUnspec" 0 "false" "return Unspec.UNSPEC;")

(gen-builtin-full "Error" 2 "true"
    "
       Object who = v[0].evalValue(env,ctx);
       Object message = v[1].evalValue(env,ctx);
       StringBuilder res = new StringBuilder();
       res.append( \"Application error:\");
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
    ")
(gen-xop "SyntaxError" 2
    "throw new PositionedError( a instanceof ISourceCoords?(ISourceCoords)a:null, b.toString() );" );"

(display "static void define ( SymbolMap sm, HashMap<Symbol,BuiltinFactory> map )
{
")
(for-each
  (lambda (pred)
    (disp `("  map.put( sm.newSymbol(\"" ,pred "\"), "
                 "new BuiltinFactory<" ,pred ">(" ,pred ".class) );\n" )))
  pred-list)
(display "}

} // class
")

