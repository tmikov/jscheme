/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.util.HashMap;

@SuppressWarnings({"serial"})
public class Scope extends HashMap<Symbol,Binding>
{
private final Scope m_parent;
/** If true, this scope doesn't have a separate environment. It is just a lexical frame */
public final boolean lexical;
/** Physical environment level. Lexical scopes do not increase it */
public final int envLevel;

private final int m_maxBindings;

/**
 * Index of the next variable to be defined
 */
private int m_bindingCount;

public static final int RESERVED_SLOTS = 1;
/** Slot 0 always contains a link to the parent environment */
public static final int PARENT_SLOT = 0;

public Scope ( Scope parent, int maxBindings, boolean lexical )
{
  assert !lexical || parent != null : "Lexical scope must have a parent";

  this.m_parent = parent;
  this.lexical = lexical;
  this.envLevel = parent != null ? parent.envLevel + (lexical ? 0 : 1) : 0;
  this.m_maxBindings = maxBindings;
  m_bindingCount = RESERVED_SLOTS;
}

public Scope ( Scope parent, boolean lexical )
{
  this( parent, Integer.MAX_VALUE, lexical);
}

public Scope ( Scope parent )
{
  this( parent, false );
}

public final boolean isTopLevel ()
{
  return this.m_parent == null;
}

public final Binding lookupAny ( Symbol sym )
{
  Binding b;
  if ( (b = get( sym )) != null)
    return b;
  return m_parent != null ? m_parent.lookupAny( sym ) : null;
}

public final VarBinding lookupVar ( Symbol sym )
{
  Binding b;
  return (b = lookupAny(sym)) != null && b instanceof VarBinding ? (VarBinding)b : null;
}

public final MacroBinding lookupMacro ( Symbol sym )
{
  Binding b;
  return (b = lookupAny(sym)) != null && b instanceof MacroBinding ? (MacroBinding)b : null;
}


/**
 * Look for a symbol on this scope only
 * @param sym the symbol to lookup
 * @return the binding or null if not found
 */
public final VarBinding localLookupVar ( Symbol sym )
{
  Binding t;
  return (t = get(sym)) != null && t instanceof VarBinding ? (VarBinding) t : null;
}

/**
 * Return the binding count of the nearest non-lexical scope
 * @return the binding count of the nearest non-lexical scope
 */
public final int getBindingCount ()
{
  return !this.lexical ? m_bindingCount : m_parent.getBindingCount();
}

/**
 * Create a new variable binging in the nearest non-lexical scope.
 * @return the new binding
 * @throws SchemeUncheckedError if too many bindings are created
 */
private VarBinding newVarBinding ( Symbol sym ) throws SchemeUncheckedError
{
  if (!this.lexical)
  {
    if (m_bindingCount == m_maxBindings)
      throw new SchemeUncheckedError( "Binding count exceeds "+ m_maxBindings );
    return new VarBinding( this, sym, m_bindingCount++);
  }
  else
    return m_parent.newVarBinding( sym );
}

/**
 * Bind a symbol in this scope, assigning it an index in the nearest non-lexical scope's environment.
 * Note that bindings count from {@link #RESERVED_SLOTS}. If the symbol is already bound, return
 * the existing binding.
 *
 * @param sym the symbol to bind
 * @return the binding
 * @throws SchemeUncheckedError if too many bindings are created
 */
public final VarBinding bindVar ( Symbol sym ) throws SchemeUncheckedError
{
  Binding t;
  if ( (t = get( sym )) != null && t instanceof VarBinding)
    return (VarBinding) t;

  VarBinding vb = newVarBinding( sym );
  put( sym, vb );
  return vb;
}

public final MacroBinding bindMacro ( Symbol sym ) throws SchemeUncheckedError
{
  Binding t;
  if ( (t = get( sym )) != null && t instanceof MacroBinding)
    return (MacroBinding)t;

  MacroBinding mb = new MacroBinding( this, sym );
  put( sym, mb );
  return mb;
}

}

