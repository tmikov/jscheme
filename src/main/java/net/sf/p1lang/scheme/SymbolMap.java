/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */

package net.sf.p1lang.scheme;

import java.util.HashMap;

public final class SymbolMap
{
private final HashMap<String,Symbol> m_map = new HashMap<String, Symbol>();

public final Symbol sym_quote             = special( "quote", SymCode.QUOTE );
public final Symbol sym_quasiquore        = special( "quasiquote", SymCode.NONE /*SymCode.QUASIQUOTE*/ );
public final Symbol sym_unquote           = special( "unquote", SymCode.NONE /*SymCode.UNQUOTE*/ );
public final Symbol sym_unquote_splicing  = special( "unquote-splicing", SymCode.NONE /*SymCode.UNQUOTE_SPLICING*/ );
public final Symbol sym_syntax            = special( "syntax", SymCode.SYNTAX );
public final Symbol sym_quasisyntax       = special( "quasisyntax", SymCode.QUASISYNTAX );
public final Symbol sym_unsyntax          = special( "unsyntax", SymCode.UNSYNTAX );
public final Symbol sym_unsyntax_splicing = special( "unsyntax-splicing", SymCode.UNSYNTAX_SPLICING );

public final Symbol sym_if                = special( "if", SymCode.IF );
public final Symbol sym_begin             = special( "begin", SymCode.BEGIN );
public final Symbol sym_lambda            = special( "lambda", SymCode.LAMBDA );
public final Symbol sym_define            = special( "define", SymCode.DEFINE );
public final Symbol sym_setbang           = special( "set!", SymCode.SETBANG );
public final Symbol sym_let               = special( "let", SymCode.LET );
public final Symbol sym_letrec            = special( "letrec", SymCode.LETREC );
public final Symbol sym_letrec_star       = special( "letrec*", SymCode.LETREC_STAR );

public final Symbol sym_builtin           = special( "__%builtin", SymCode.BUILTIN );
public final Symbol sym_define_macro      = special( "define-macro", SymCode.DEFINE_MACRO );
public final Symbol sym_define_identifier_macro = special( "define-identifier-macro", SymCode.DEFINE_IDENTIFIER_MACRO );
public final Symbol sym_define_det_macro  = special( "define-set-macro", SymCode.DEFINE_SET_MACRO );
public final Symbol sym_macro_env         = special( "macro-env", SymCode.MACRO_ENV );


private final Symbol special ( String name, SymCode code )
{
  Symbol res = new Symbol( name, code );
  if (m_map.put( name, res ) != null)
    assert false : "Duplicate definition of special symbol";
  return res;
}

public final /*synchronized*/ Symbol newSymbol ( String name )
{
  Symbol res;
  if ( (res = m_map.get( name )) == null)
    m_map.put( name, res = new Symbol( name, SymCode.NONE ) );
  return res;
}

} // class

