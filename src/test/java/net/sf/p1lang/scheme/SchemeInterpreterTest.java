package net.sf.p1lang.scheme;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * SchemeInterpreter Tester.
 *
 * @author <Authors name>
 * @since <pre>04/04/2008</pre>
 * @version 1.0
 */
public class SchemeInterpreterTest extends TestCase
{
private PrintWriter out;
private SchemeInterpreter sc;

public SchemeInterpreterTest(String name)
{
  super(name);
}

public void setUp() throws Exception
{
  super.setUp();
  out = new PrintWriter(System.out);
  sc = new SchemeInterpreter();
}

public void tearDown() throws Exception
{
  out.flush();
  out = null;
  sc = null;
  super.tearDown();
}

private void needError ( Callable c )
{
  try
  {
    c.call();
    fail( "Exception not thrown" );
  }
  catch (Exception ignored){};
}

private Object eval ( String str ) throws SchemeError
{
  return sc.eval( str );
}

public void testSimple () throws Exception
{
  Object res;
  res = eval( "1" );
  res = eval( "(display 1)" );
  res = eval( "(display (+ (+ 1 3/2) -10i))" );
}

public void testMore () throws Exception
{
  eval(
"(define var 10)" +
"(set! var (+ var 1))" +
"(display (+ var 2))"
  );
}

public void testFunc () throws Exception
{
  eval(
"(define (add a b) (+ a b))" +
"(display (add 10 20))"
  );
}

public void testIf () throws Exception
{
  eval(
"(display (if #t 1 -1))"+
"(display (if #f 1 -1))"
  );
}

public void testLambda () throws Exception
{
  eval(
"(define (make-counter init step)" +
"  (define (next)" +
"    (define res init)" +
"    (set! init (+ init step))" +
"    res)" +
"  next)" +
"" +
"(define c1 (make-counter 0 2))" +
"(define c2 (make-counter 10 10))" +
"" +
"(define (disp a) " +
"  (display a)" +
"  (display #\\,))" +
"" +
"(disp (c1))" +
"(disp (c2))" +
"(disp (c1))" +
"(disp (c2))" +
"(disp (c1))" +
"(disp (c2))" +
"(disp (c1))" +
"(disp (c2))" +
""
  );
}

public void testQuote () throws Exception
{
  Object res;
  res = eval( "'1" );
  res = eval( "'(a b)" );
  needError( new Callable() { public Object call () throws Exception {
    eval( "(quote 1 2)" );
    return null;
  }});
}

public void testMacro () throws Exception
{
  eval(
"(define-macro (ADD a b) (+ a b))" +
"(define-identifier-macro (ADD name) 300)" +
"(define-set-macro (ADD name value) '(display \"set\") )" +
"(display (ADD 100 200))"+
"(display ADD)"+
"(set! ADD 20)"
);
}

public void testBuiltins () throws Exception
{
  eval("(__@builtin add 1 2)" );
  eval("(display (__@builtin add 1 2))" );
  needError( new Callable() { public Object call () throws Exception {
    eval( "(__@builtin)" );
    return null;
  }});
  needError( new Callable() { public Object call () throws Exception {
    eval( "(__@builtin bla)" );
    return null;
  }});
  needError( new Callable() { public Object call () throws Exception {
    eval( "(__@builtin add)" );
    return null;
  }});
  needError( new Callable() { public Object call () throws Exception {
    eval( "(__@builtin add 1)" );
    return null;
  }});
  needError( new Callable() { public Object call () throws Exception {
    eval( "(__@builtin add 1 2 3)" );
    return null;
  }});
}

public static Test suite()
{
  return new TestSuite(SchemeInterpreterTest.class);
}
} // SchemeInterpreterTest
