package net.sf.p1lang.scheme;

import java.io.PrintWriter;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * DatumReader Tester.
 *
 * @author <Authors name>
 * @since <pre>04/04/2008</pre>
 * @version 1.0
 */
public class DatumReaderTest extends TestCase
{
private PrintWriter out;

public DatumReaderTest (String name)
{
  super(name);
}

public void setUp() throws Exception
{
  super.setUp();
  out = new PrintWriter(System.out);
}

public void tearDown() throws Exception
{
  out.flush();
  out = null;
  super.tearDown();
}

public void testSimple ()
{
  DatumParser dp = new DatumParser(
     TestUtils.lexer(
"#;(this is ignored)" +
"(begin " +
"     (set! my-var '(+ 10 #;20 #e6.66 #\\a \"a\\x10;t\"))" +
"     #(1 2 3)" +
")"
     ) );
  Object datum = dp.parseDatum();
}

public static Test suite()
{
  return new TestSuite(DatumReaderTest.class);
}
} // DatumTest
