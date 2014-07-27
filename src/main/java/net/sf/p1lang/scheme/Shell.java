/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.FileInputStream;
import java.io.Reader;
import java.util.StringTokenizer;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class Shell
{
private final SchemeInterpreter m_interp;

private BufferedReader m_in = new BufferedReader( new InputStreamReader(System.in) );
private PrintWriter m_out = new PrintWriter( System.out, true );

public Shell () throws SchemeError, IOException
{
  m_interp = new SchemeInterpreter();
}

public SchemeInterpreter getInterp ()
{
  return m_interp;
}

private boolean checkCommand ( String line ) throws SchemeError, IOException
{
  StringTokenizer tok = new StringTokenizer( line );
  if (tok.hasMoreTokens())
  {
    String cmd = tok.nextToken();

    if (cmd.charAt(0) != ',')
      return false;

    if (",exit".equals(cmd)) // ,exit
    {
      if (tok.hasMoreTokens())
      {
        m_out.println( "*** Invalid ,exit syntax" );
        return true;
      }
      System.exit(0);
    }
    else if (",dis".equals(cmd)) // ,dis symbol
    {
      if (!tok.hasMoreTokens())
        m_out.println( "*** Invalid ,dis syntax" );
      else
      {
        String name = tok.nextToken();
        if (tok.hasMoreTokens())
          m_out.println( "*** Invalid ,dis syntax" );
        else
          dis( name );
      }
      return true;
    }
    else if (",load".equals(cmd)) //,load file
    {
      if (!tok.hasMoreTokens())
        m_out.println( "*** Invalid ,load syntax" );
      {
        String fileName = tok.nextToken(); // FIXME: this is wrong of course
        if (tok.hasMoreTokens())
          m_out.println( "*** Invalid ,load syntax" );
        else
          load( fileName );
      }
      return true;
    }
    else if (",extend".equals(cmd)) //,loadClass class
    {
      if (!tok.hasMoreTokens())
        m_out.println( "*** Invalid ,extend syntax" );
      {
        String fileName = tok.nextToken();
        if (tok.hasMoreTokens())
          m_out.println( "*** Invalid ,extend syntax" );
        else
          extend( fileName );
      }
      return true;
    }
    else
    {
      m_out.println(
          ",help                 - this help\n"+
          ",exit                 - exit\n" +
          ",dis procedure        - disassemble procedure\n" +
          ",load filename        - parse, compile and eval a Scheme file\n" +
          ",extend className     - load an extension class\n" +
          "     The extension class must be in the class path. For example:\n" +
          "          java -cp mylib.jar:scheme.jar "+getClass().getName()+ "\n"
      );
      m_out.flush();
      return true;
    }
  }

  return false;
}

private String readExpr () throws IOException, SchemeError
{
  m_out.print( ">> " );m_out.flush();

  StringBuilder expr = new StringBuilder();
  String line;
  while ((line = m_in.readLine()) != null)
  {
    // Check for a command
    if (expr.length() == 0 && checkCommand(line))
    {
      m_out.print( ">> " );m_out.flush();
      continue; // The command has been executed - restart the read loop
    }

    expr.append( line ).append( '\n' );
    if (m_interp.isCompleteExpr( expr.toString() ))
      break;

    m_out.print( ".. " );m_out.flush();
  }

  if (line == null && expr.length() == 0) // EOF ?
    return null;
  return expr.toString();
}

public void run ()
{
  m_out.println( "Java Scheme" );

  for(;;)
  {
    String expr;
    try
    {
      if ((expr = readExpr()) == null) // EOF
        break;
      //Object res = m_interp.eval( expr );
      IErrorReporter errors = new SimpleErrorReporter(5);
      Object res = m_interp.compile( m_interp.parse( new StringReader(expr), "<tty>", errors ),
                                     errors, true )
                           .eval();
      display( res );
      m_out.println(); m_out.flush();
    }
    catch (IOException e)
    {
      e.printStackTrace( m_out );
      m_out.flush();
      break;
    }
    catch (ParseErrors parseErrors)
    {
      for ( ErrorInfo ei : parseErrors.errors.getErrorList() )
        m_out.println( "***Error:"+ ei.formatMessage() );
      m_out.flush();
    }
    catch (PositionedError positionedError)
    {
      if (positionedError.getPosition() != 0)
        m_out.println( positionedError.getMessage() );
      else
        positionedError.printStackTrace( m_out );
    }
    catch (SchemeError schemeError)
    {
      schemeError.printStackTrace( m_out );
      m_out.flush();
    }
    catch (RuntimeException e)
    {
      e.printStackTrace( m_out );
      m_out.flush();
    }
  }
}

private void load ( String fileName ) throws SchemeError
{
  try
  {
    Reader in = new BufferedReader(new InputStreamReader( new FileInputStream(fileName)));
    try
  {
    SimpleErrorReporter errors = new SimpleErrorReporter(20);
      ParsedList pl = m_interp.parse( in, fileName, errors);
      in.close();
      m_interp.compile( pl, errors ).eval();
    }
    finally
    {
      try  {
        in.close();
      }
      catch (IOException ignored) {}
    }
  }
  catch (IOException e)
  {
    m_out.println( "***Error:"+ e.getMessage() );
    m_out.flush();
  }
}

private void extend ( String className ) throws SchemeError
{
  Class clz;
  Method method;
  try
  {
    clz = Class.forName(className);
    method = clz.getMethod( "extend", SchemeInterpreter.class, IErrorReporter.class );
  }
  catch (Exception e)
  {
    e.printStackTrace( m_out );
    m_out.flush();
    return;
  }

  try
  {
    method.invoke( null, m_interp, new SimpleErrorReporter(20) );
  }
  catch (IllegalAccessException e)
  {
    e.printStackTrace( m_out );
    m_out.flush();
  }
  catch (InvocationTargetException e)
  {
    if (e.getCause() instanceof SchemeError)
      throw (SchemeError)e.getCause();
    e.printStackTrace( m_out );
    m_out.flush();
  }
}

private void dis ( String name ) throws SchemeError
{
  Object res = m_interp.eval( name );
  if (res instanceof Closure)
  {
    Closure l = (Closure) res;
    m_out.println( l );
    if (l.lambda.body != null)
    {
      displayIndented( 0, l.lambda.body.dis() );
    }
    else
      m_out.print( "null" );
    m_out.println();
  }
  else
    m_out.println( "** Error: not a lambda" );
}

private void display ( Object datum )
{
  Misc.display(m_out, datum);
}

private void displayIndented ( int indent, Object datum )
{
  Misc.displayIndented(m_out, indent, datum);
}

public static void main ( String[] args ) throws SchemeError, IOException
{
  new Shell().run();
}

} // class

