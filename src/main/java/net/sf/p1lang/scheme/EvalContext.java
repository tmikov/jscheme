/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

/**
 * Context is used mainly as an output parameter holder, but we also use it to pass around
 * other data that may be useful - since we already are dragging an extra pointer around, we
 * might as well make the most of it.
 *
 * <p>Q:Why do we pass it around instead of allocating it when we need an output parameter holder ?
 * <p>A:I dread the idea of using heap allocations for frequent trivial operations
 */
final class EvalContext
{
public final SchemeInterpreter interp;

/** Direct reference to the root environment. Not really used yet */
public final Object[] rootEnv;

/** Output paremeter used to store the continuation's environment */
public Object[] outEnv;

EvalContext ( final SchemeInterpreter interp, final Object[] rootEnv )
{
  this.interp = interp;
  this.rootEnv = rootEnv;
}

//public Object[] stack;
///** Next index to use in {@link #stack} */
//private int m_sp;
//private int[] m_frames;
///** Last used index in {@link #m_frames} */
//private int m_curFrame;
//
//public EvalContext ( final Object[] rootEnv )
//{
//  this.rootEnv = rootEnv;
//  this.stack = new Object[128];
//  m_frames = new int[128];
//  m_curFrame = -1;
//  m_sp = 0;
//}
//
///**
// * Allocate a new stack frame holding {@code params} number of values. Return the index
// * of the first parameter, which can then be accessed directly using {@code stack[index+ofs]}.
// *
// * <p>The frame must be deallocated using {@link #popFrame()}
// *
// * @param params number of parameters to allocate
// * @return the index of the first parameter
// */
//public int pushFrame ( int params )
//{
//  if (m_sp + params < this.stack.length)
//  {
//    Object[] tmp = new Object[Math.max( this.stack.length*2, m_sp + params)];
//    System.arraycopy( this.stack, 0, tmp, 0, m_sp );
//    this.stack = tmp;
//  }
//
//  if (m_curFrame == m_frames.length-1)
//  {
//    int[] tmp = new int[m_frames.length*2];
//    System.arraycopy(m_frames, 0, tmp, 0, m_curFrame+1);
//    m_frames = tmp;
//  }
//
//  ++m_curFrame;
//  m_frames[m_curFrame] = params;
//  m_sp += params;
//
//  return m_sp - params;
//}
//
//public void popFrame ()
//{
//  assert( m_curFrame >= 0 );
//  int oldSp = m_sp - m_frames[m_curFrame];
//
//  // Clean the frame
//  for ( int i = oldSp; i < m_sp; ++i )
//    this.stack[i] = null;
//
//  m_sp = oldSp;
//  --m_curFrame;
//}

}

