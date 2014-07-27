/*
 * This file is distributed under the terms of the GPL v3.0 license.
 * See file COPYING in the root project folder
 *
 * Copyright Tzvetan Mikov <tmikov@gmail.com>
 */
package net.sf.p1lang.scheme;

public enum SymCode
{
  NONE,
  QUOTE,
/* Handled by a macro
  QUASIQUOTE,
  UNQUOTE,
  UNQUOTE_SPLICING,
*/
  SYNTAX,
  QUASISYNTAX,
  UNSYNTAX,
  UNSYNTAX_SPLICING,

  IF,
  BEGIN,
  LAMBDA,
  DEFINE,
  SETBANG,
  LET,
  LETREC,
  LETREC_STAR,

  BUILTIN,
  DEFINE_MACRO,
  DEFINE_IDENTIFIER_MACRO,
  DEFINE_SET_MACRO,
  MACRO_ENV;
}
