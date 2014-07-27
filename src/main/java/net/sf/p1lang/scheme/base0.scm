;; Bootstrap the language using the builtins. It must be done in stages because
;; macros can only use previously compiled routines

( ; stage 0

(define (list . l) l)
(define __%VOID (__%builtin LitUnspec))

(define (__%cons a b) (__%builtin Cons a b))
) ; stage 0

( ; stage 1

(define (__%error who msg . irritants) (error who msg irritants)) ; FIXME: this behaves differently
(define-macro (error who msg . irritants)
    (__%cons '__%builtin (__%cons 'Error (__%cons who (__%cons msg irritants)))))
(define-identifier-macro (error _) '__%error)

(define (__%syntax-error where a) (syntax-error where a))
(define-macro (syntax-error where a) (list '__%builtin 'SyntaxError where a))
(define-identifier-macro (syntax-error _) '__%syntax-error)

(define (__%not x) (not x))
(define-macro (not x) (list 'if x #f #t))
(define-identifier-macro (not _) '__%not)

(define (__%cons a b) (cons a b))
(define-macro (cons a b) (list '__%builtin 'Cons a b))
(define-identifier-macro (cons _) '__%cons)

(define (__%car a) (car a))
(define-macro (car a) (list '__%builtin 'Car a))
(define-identifier-macro (car _) '__%car)

(define (__%cdr a) (cdr a))
(define-macro (cdr a) (list '__%builtin 'Cdr a))
(define-identifier-macro (cdr _) '__%cdr)

(define (__%set-car! a b) (set-car! a b))
(define-macro (set-car! a b) (list '__%builtin 'SetCar a b))
(define-identifier-macro (set-car! _) '__%set-car!)

(define (__%set-cdr! a b) (set-cdr! a b))
(define-macro (set-cdr! a b) (list '__%builtin 'SetCdr a b))
(define-identifier-macro (set-cdr! _) '__%set-cdr!)



(define (__%null? a) (null? a))
(define-macro (null? a) (list '__%builtin 'NullPred a))
(define-identifier-macro (null? _) '__%null?)

(define (__%pair? a) (pair? a))
(define-macro (pair? a) (list '__%builtin 'PairPred a))
(define-identifier-macro (pair? _) '__%pair?)

(define (__%eq? a b) (eq? a b))
(define-macro (eq? a b) (list '__%builtin 'Eq a b))
(define-identifier-macro (eq? _) '__%eq?)

(define (__%eqv? a b) (eqv? a b))
(define-macro (eqv? a b) (list '__%builtin 'Eqv a b))
(define-identifier-macro (eqv? _) '__%eqv?)

(define (boolean? a) (__%builtin BooleanPred a))
(define (symbol? a) (__%builtin SymbolPred a))
(define (char? a) (__%builtin CharPred a))
(define (string? a) (__%builtin StringPred a))
(define (procedure? a) (__%builtin ProcedurePred a))
(define (vector? a) (__%builtin VectorPred a))
(define (number? a) (__%builtin NumberPred a))
(define (complex? a) (__%builtin ComplexPred a))
(define (real? a) (__%builtin RealPred a))
(define (rational? a) (__%builtin RationalPred a))
(define (integer? a) (__%builtin IntegerPred a))
(define (exact? a)   (__%builtin ExactPred a))
(define (inexact? a)   (if (__%builtin ExactPred a) #f #t))

(define (caar   x) (car (car x)))
(define (cadr   x) (car (cdr x)))
(define (cdar   x) (cdr (car x)))
(define (cddr   x) (cdr (cdr x)))

(define (caaar  x) (caar (car x)))
(define (caadr  x) (caar (cdr x)))
(define (cadar  x) (cadr (car x)))
(define (caddr  x) (cadr (cdr x)))
(define (cdaar  x) (cdar (car x)))
(define (cdadr  x) (cdar (cdr x)))
(define (cddar  x) (cddr (car x)))
(define (cdddr  x) (cddr (cdr x)))

(define (caaaar x) (caaar (car x)))
(define (caaadr x) (caaar (cdr x)))
(define (caadar x) (caadr (car x)))
(define (caaddr x) (caadr (cdr x)))
(define (cadaar x) (cadar (car x)))
(define (cadadr x) (cadar (cdr x)))
(define (caddar x) (caddr (car x)))
(define (cadddr x) (caddr (cdr x)))
(define (cdaaar x) (cdaar (car x)))
(define (cdaadr x) (cdaar (cdr x)))
(define (cdadar x) (cdadr (car x)))
(define (cdaddr x) (cdadr (cdr x)))
(define (cddaar x) (cddar (car x)))
(define (cddadr x) (cddar (cdr x)))
(define (cdddar x) (cdddr (car x)))
(define (cddddr x) (cdddr (cdr x)))

(define (call-with-current-continuation p) (__%builtin CallCC p))
(define call/cc call-with-current-continuation)

;;
;; length
;;
(define (length lst)
    (let lp ([lst lst] [len 0])
        (if (pair? lst)
            (lp (cdr lst) (__%builtin Add 1 len))
            len)))

(define (__%apnd a b) ; simple append
    (if (null? b)
        a
        (if (null? a)
            b
            (cons (car a) (__%apnd (cdr a) b)))))

)  ; stage 1

(  ; stage 2
;;
;; (and ... )
;;
(define-macro (and . params)
    (if (null? params)
        #t
        (if (null? (cdr params))
            (car params)
            (list 'if (car params) (cons 'and (cdr params)) #f))))

;;
;; (or ... )
;;
(define-macro (or . params)
    (if (null? params)
        #f
        (if (null? (cdr params))
            (car params)
            (let ((x (gentemp)))
                (list 'let (list (list x (car params)))
                    (list 'if x x (cons 'or (cdr params))))))))

;;
;; (let* ((a b)(c d) body) is translated into:
;;   (let ((a b)) (let ((c d)) body))
;;
(define-macro (let* bindings . body)
    (define (ncdr p)
        (let ([res (cdr p)])
            (if (null? res) (syntax-error p "Invalid syntax - list is too short") res)))
    (if (null? bindings)
        (cons 'let (cons '() body))
        (list
            'let
            (list (car bindings))
            (cons 'let* (cons (cdr bindings) body)))))

;;
;; (cond ...)
;;
(define-macro (cond first . rest)
    (if (null? first)
        (syntax-error first "Invalid (cond) syntax: no clauses"))
    (let ([rest-value (if (null? rest) '() (list (cons 'cond rest)))]
          [cnd (car first)]
          [body (cdr first)])
        (if (eq? cnd 'else)
            ;; (else ...) ==> (begin ...)
            (if (not (null? rest))
                (syntax-error first "Invalid (cond) syntax: else must be the last clause")
                (cons 'begin body))
            ;; not else
            (if (null? body)
                ;; empty body
                (if (null? rest)
                    cnd                      ; (cond (x)) => x
                    (let ([tmp (gentemp)])   ; (cond (x)(y...)) => (let ((t=x))(if t t (cond (y...))))
                        (list 'let (list (list tmp cnd))
                            (__%apnd (list 'if tmp tmp) rest-value))))
                ;; non-empty body
                (if (eq? (car body) '=>)
                    ;; (cond (a => b)...) => (let ((t=a)) (if t (b t) ...)
                    (let ([tmp (gentemp)])
                        (if (null? (cdr body))
                            (syntax-error body "Invalid (cond) syntax: empty body after =>"))
                        (if (not (null? (cddr body)))
                            (syntax-error (cddr body) "Invalid (cond) syntax: more than one expr after =>"))
                        (list 'let (list (list tmp cnd))
                            (__%apnd (list 'if tmp (__%apnd (cdr body) (list tmp))) rest-value)))
                    ;; (cond (a b c) ... => (if a (begin b c) ...)
                    (__%apnd (list 'if cnd (cons 'begin body)) rest-value))))))

;;
;; (when test expr...)
;;
(define-macro (when test . body)
    (if (null? body)
        (syntax-error test "Invalid (when) syntax: empty body"))
    (list 'if test (cons 'begin body)))

(define-macro (unless test . body)
    (if (null? body)
        (syntax-error test "Invalid (unless) syntax: empty body"))
    ;; Some uglyness to avoid a function call
    (list 'if (list 'if test #f #t) (cons 'begin body)))

;;
;; (do (?[var init ?step]...)(test ?tbody...) ?body...)
;;   ==>
;;      (let %tmp ([var init]...)
;;          (if (test)
;;              (begin tbody...)
;;              (begin
;;                  body
;;                  (%tmp ?step...))))
;;+TODO: test & report errors instead of relying on VM exceptions
(define-macro (do bindings test-clause . body)
    (define (<= a b)  (__%builtin NumberLE a b))
    (define (>= a b)  (__%builtin NumberLE b a))
    ;; Validate the bindings format
    (let lp ([bindings bindings])
        (if (not (null? bindings))
            (begin
                (if (not (let ([varp (car bindings)])
                            (if (pair? varp) ;+FIXME: should really use (list?) but it isn't available here
                                (let ([ll (length varp)])  ; can't use and/or
                                    (if (>= ll 2) (<= ll 3) #f))
                                #f)))
                    (syntax-error bindings "Invalid (do) binding syntax"))
                (lp (cdr bindings)))))
    ;; Do the real work
    (let ([tmp (gentemp)])
        (list
            'let
            tmp
            ;; Build the init list ([var init]...)
            (let lp ([bindings bindings])
                (if (null? bindings)
                    '()
                    (let ([varp (car bindings)])
                        (cons
                            (list (car varp) (cadr varp))
                            (lp (cdr bindings))))))
            ;; the body of the let
            (list
                'if
                (car test-clause)
                (cons 'begin (cdr test-clause))
                (__%apnd
                    (cons 'begin body)
                    ;; Build the loop invocation (%tmp ?step ...)
                    (list
                        (cons
                            tmp
                            (let lp ([bindings bindings])
                                (if (null? bindings)
                                    '()
                                    (let ([varp (car bindings)])
                                        (cons
                                            (if (null? (cddr varp)) (car varp) (caddr varp))
                                            (lp (cdr bindings)))))))))))))

;;
;; let-optionals*
;;
;; (let-optionals* rest ((a aa)(b bb...)) . body)
;;   ==>
;;      (let* ([%r rest][a (if (null? %r) 0 (car %r))])
;;          (let-optionals* (if (null? %r) '() (cdr %r)) ((b bb...)) . body ))
;;
(define-macro (let-optionals* arg params . body)
    (if (null? params)
        ;; check if there are extra arguments
        (list
            'if
            (list 'null? arg)
            (cons 'let (cons '() body))
            (list 'error "let-optionals*" "Extra arguments"))
        ;; If there is a single last parameter, it takes the rest of the arguments
        (if (not (pair? params))
            (syntax-error arg "Invalid (let-optionals*...) syntax: parameter 2 is not a list")
            (if (not (pair? (car params)))
                (cons 'let (cons (list (list (car params) arg)) body))
                (let ([tmp (gentemp)])
                    (list
                        'let*
                        (list
                            (list tmp arg)
                            (list
                                (caar params)
                                (list 'if (list 'null? tmp) (cadar params) (list 'car tmp))))
                        (cons
                            'let-optionals*
                            (cons
                                (list 'if (list 'null? tmp) tmp (list 'cdr tmp))
                                (cons (cdr params) body)))))))))

;;
;; list?
;;
(define (list? l)
    (let recur ((l l) (lag l))            ;Cycle detection
        (or (null? l)
            (and (pair? l)
                 (or (null? (cdr l))
                     (and (pair? (cdr l))
                          (not (eq? (cdr l) lag))
                          (recur (cddr l) (cdr lag))))))))

;;
;; apply
;;
;; Very inefficient out-of-line version of apply
;; It must verify that the last parameter is a list
;;
(define (__%apply target . rest )
    (if (null? rest)
        (error "(apply)" "need at least 2 parameters")
        (__%builtin Apply
            target
            ;; Must concatenate the first parameters with the last one
            (let lp ([car-rest (car rest)] [cdr-rest (cdr rest)])
                (if (null? cdr-rest)
                    (if (pair? car-rest)
                        car-rest
                        (error "(apply)" "Last parameter must be a list"))
                    (cons car-rest (lp (car cdr-rest) (cdr cdr-rest))))))))

(define-macro (apply target . rest)
    (if (null? rest)
        (syntax-error target "(apply ...) needs at least 2 parameters"))
    (cons '__%builtin (cons 'Apply (cons target rest))))

(define-identifier-macro (apply _) '__%apply)

;;
;; +
;;
(define-macro (+ . params)
    (if (null? params)
        0
        (if (null? (cdr params))
            (list '__%builtin 'Add 0 (car params))
            (let lp ([acc (car params)] [params (cdr params)])
                (if (null? params)
                    acc
                    (lp (list '__%builtin 'Add acc (car params)) (cdr params)))))))

(define-identifier-macro (+ _) '__%+)

(define (__%+ . params)
    (let helper ([acc 0] [params params])
        (if (null? params)
            acc
            (helper (+ acc (car params)) (cdr params)))))

;;;
;;;

;; FIXME
(define (<= a b)  (__%builtin NumberLE a b))
(define (< a b)  (__%builtin NumberLT a b))
(define (= a b)  (__%builtin NumberEQ a b))
(define (>= a b)  (__%builtin NumberLE b a))
(define (> a b)  (__%builtin NumberLT b a))

(define (* . params)
    (define (helper acc params)
        (if (null? params)
            acc
            (helper (__%builtin Mul acc (car params)) (cdr params))))
    (helper 1 params))

(define (- a . params)
    (define (helper acc params)
        (if (null? params)
            acc
            (helper (__%builtin Sub acc (car params)) (cdr params))))
    (if (null? params)
        (__%builtin Sub 0 a)
        (helper (__%builtin Sub a (car params)) (cdr params))))

(define (/ a . params)
    (define (helper acc params)
        (if (null? params)
            acc
            (helper (__%builtin Div acc (car params)) (cdr params))))
    (if (null? params)
        (__%builtin Div 1 a)
        (helper (__%builtin Div a (car params)) (cdr params))))


;;;;;;;;;; Strings ;;;;;;;;;;;;;;;;;;;

;; FIXME: multiple params
(define (string=? a b) (__%builtin NumberEQ (__%builtin Compare2Strings a b) 0))

(define (string-upcase str) (__%builtin StringUpcase str))
(define (string-downcase str) (__%builtin StringDowncase str))
(define string-foldcase string-downcase) ;; FIXME!

;; FIXME: multiple params
(define (string-ci=? a b)
    (string=? (string-foldcase a) (string-foldcase b)))

(define (string-length str) (__%builtin StringLength str))
(define (string-ref str index) (__%builtin StringRef str index))

(define string-append __%string-append)

(define number->string __%number->string)
(define (symbol->string sym) (__%builtin SymbolToString sym))

;;;;;;;;;; Chars  ;;;;;;;;;;;;;;;;;;;;

(define (char->integer ch) (__%builtin CharToInteger ch))
(define (char-downcase ch) (__%builtin CharDowncase ch))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define-macro (exact x) (list '__%builtin 'Exact x))
(define-macro (inexact->exact x) (list '__%builtin 'Exact x))
(define-identifier-macro (exact _) '__%exact)
(define-identifier-macro (inexact->exact _) '__%exact)
(define (__%exact x) (exact x))

(define-macro (inexact x) (list '__%builtin 'Inexact x))
(define-macro (exact->inexact x) (list '__%builtin 'Inexact x))
(define-identifier-macro (inexact _) '__%inexact)
(define-identifier-macro (exact->inexact _) '__%inexact)
(define (__%inexact x) (inexact x))

(define-macro (numerator x) (list '__%builtin 'Numerator x))
(define-identifier-macro (numerator _) '__%numerator)
(define (__%numerator x) (numerator x))

(define-macro (denominator x) (list '__%builtin 'Denominator x))
(define-identifier-macro (denominator _) '__%denominator)
(define (__%denominator x) (denominator x))


;; Truncated integer division (quotient -42 5) ==> -8
;; a = q*b + r
;; 0 <= abs(r) < abs(b)
;;
(define-macro (quotient a b) (list '__%builtin 'Quotient a b))
(define-identifier-macro (quotient _) '__%quotient)
(define (__%quotient a b) (quotient a b))

;; Truncated integer remainder
;; Always the same sign as the dividend
;;   (remainder -42  5) ==> -2
;;   (remainder  42  5) ==>  2
;;   (remainder  42 -5) ==>  2
;;   (remainder -42 -5) ==> -2
;;
(define-macro (remainder a b) (list '__%builtin 'Remainder a b))
(define-identifier-macro (remainder _) '__%remainder)
(define (__%remainder a b) (remainder a b))

;; Truncated integer modulo:
;; Always the same sign as the divisor
;;    (modulo -42  5) ==>  3
;;    (modulo  42  5) ==>  2
;;    (modulo  42 -5) ==> -3
;;    (modulo -42 -5) ==> -2
;;
;;+TODO: optimize ?
(define (modulo a b)
    (let* ([a (exact a)]
           [b (exact b)]
           [r (remainder a b)])
        (if (or (and (> r 0) (< b 0))
                (and (> b 0) (< r 0)))
            (+ r b)
            r)))


;; Euclidian division  (div -42 5) ==> -9
;;   a = q*b + m
;;   0 <= m <= abs(b)
;;    (div -42  5) ==> -9
;;    (mod  42  5) ==> 8
;;    (mod  42 -5) ==> -8
;;    (mod -42 -5) ==> 9
;;
;;+TODO: optimize ?
(define (div a b)
    (let* ([a (exact a)]
           [b (exact b)]
           [q (quotient a b)])
        (if (< a 0)
            (if (> b 0)
                (- q 1)
                (+ q 1))
            q)))

;; Euclidian integer remainder (remainder -42 5) ==> 3
;; Always positive.
;;    (mod -42  5) ==> 3
;;    (mod  42  5) ==> 2
;;    (mod  42 -5) ==> 2
;;    (mod -42 -5) ==> 3
;;
;;+TODO: optimize ?
(define (mod a b)
    (let* ([a (exact a)]
           [b (exact b)]
           [r (remainder a b)])
        (if (< r 0) ; could use 'a instead of 'r (they have the same sign), but its not live
            (if (> b 0)
                (+ r b)
                (- r b))
            r)))

; From Scheme48
; Messy because of inexact contagion.

(define (max first . rest)
  (max-or-min first rest #t))

(define (min first . rest)
  (max-or-min first rest #f))

(define (max-or-min first rest max?)
  (let loop ((result first) (rest rest) (lose? (inexact? first)))
    (if (null? rest)
        (if (and lose? (exact? result))
            (exact->inexact result)
            result)
        (let ((next (car rest)))
          (loop (if (if max?
                        (< result next)
                        (> result next))
                    next
                    result)
                (cdr rest)
                (or lose? (inexact? next)))))))


(define (real-part x) (__%builtin RealPart x))
(define (imag-part x) (__%builtin ImagPart x))

(define (expt x y) (__%builtin Expt x y))

(define (abs n) (if (< n 0) (- 0 n) n))

(define (zero? x) (= x 0))
(define (positive? x) (< 0 x))
(define (negative? x) (< x 0))

(define (even? n) (= 0 (remainder n 2)))
(define (odd? n) (not (even? n)))

;;; vectors
(define-macro (vector-set! vec ind val) (list '__%builtin 'VectorSet vec ind val))
(define-identifier-macro (vector-set! _) '__%vector-set!)
(define (__%vector-set! vec ind val) (vector-set! vec ind val))

(define-macro (vector-ref vec ind) (list '__%builtin 'VectorRef vec ind))
(define-identifier-macro (vector-ref _) '__%vector-ref)
(define (__%vector-ref vec ind) (vector-ref vec ind))

(define-macro (vector-length vec) (list '__%builtin 'VectorLen vec))
(define-identifier-macro (vector-length _) '__%vector-length)
(define (__%vector-length vec ind) (vector-length vec))

(define (make-vector k . fill)
    (__%builtin MakeVector
        k
        (if (not (null? fill))
            (if (null? (cdr fill))
                (car fill)
                (error "(make-vector)" "Need 1 or 2 arguments")))))

;++ TODO: map.put( sm.newSymbol("Vector"), new BuiltinFactory<Vector>(Vector.class) );
(define (vector . lst) (list->vector lst))

(define (list->vector lst)
    (define vec (__%builtin MakeVector (length lst) __%VOID))
    (let lp ([i 0] [lst lst])
        (if (null? lst)
            vec
            (begin
                (vector-set! vec i (car lst))
                (lp (+ i 1) (cdr lst))))))

(define (vector->list vec)
    (let lp ([i (- (vector-length vec) 1)] [lst '()])
        (if (< i 0)
            lst
            (lp (- i 1) (cons (vector-ref vec i) lst)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; Multiple values
;;;

(define __%values-marker '__%values-marker)

(define (__%values? v) (and (pair? v) (eq? (car v) __%values-marker)))

(define (values . vals)
    (cond
        ((null? vals) vals)
        ((null? (cdr vals)) (car vals))
        (else (cons __%values-marker vals))))

(define (call-with-values producer consumer)
    (let ([vals (producer)])
        (if (__%values? vals)
            (apply consumer (cdr vals))
            (apply consumer vals '()))))

(define-macro (receive formals expression . body)
    (list 'call-with-values
        (list 'lambda '() expression)
        (cons 'lambda (cons formals body))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;
;; equal?
;;
(define (equal? obj1 obj2)
  (cond ((eqv? obj1 obj2) #t)
        ((pair? obj1)
         (and (pair? obj2)
              (equal? (car obj1) (car obj2))
              (equal? (cdr obj1) (cdr obj2))))
        ((string? obj1)
         (and (string? obj2)
              (string=? obj1 obj2)))
        ((vector? obj1)
         (and (vector? obj2)
              (let ((z (vector-length obj1)))
                (and (= z (vector-length obj2))
                     (let loop ((i 0))
                       (cond ((= i z) #t)
                             ((equal? (vector-ref obj1 i) (vector-ref obj2 i))
                              (loop (+ i 1)))
                             (else #f)))))))
        (else #f)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define (assp proc alist)
    (cond ((null? alist) #f)
          ((proc (caar alist)) (car alist))
          (else (assp proc (cdr alist)))))

;Note: Extended version is defined in srfi-1
(define (assoc obj alist)
    (cond ((null? alist) #f)
          ((equal? obj (caar alist)) (car alist))
          (else (assoc obj (cdr alist)))))

(define (assv obj alist)
    (cond ((null? alist) #f)
          ((eqv? obj (caar alist)) (car alist))
          (else (assv obj (cdr alist)))))

(define (assq obj alist)
    (cond ((null? alist) #f)
          ((eq? obj (caar alist)) (car alist))
          (else (assq obj (cdr alist)))))


) ; stage 2

( ; stage 3

;;
;; (case <key> <case clause> <case clause> ...)
;;
;;   ==>
;;      (let ([%tmp key])
;;          (if (or (eqv? key a)(eqv? key b)....)
;;              clause1-body
;;              (if (or (eqv? key c)(eqv? key d)....)
;;                  clause2-body
;;
;;+TODO: build a table, do a binary search, index lookup, etc
(define-macro (case key . clauses)
    (if (null? clauses)
        key ; evalue key for its side effects only
        (let ([tmp (gentemp)])
            (list
                'let (list (list tmp key)) ; the outer let
                (let recurse-clause ([clauses clauses])
                    (let* ([cur-clause (car clauses)]
                           [datum-list (car cur-clause)]
                           [clause-body (cdr cur-clause)])
                        (if (eq? datum-list 'else)
                            ;; else clause
                            (if (null? (cdr clauses))   ; Is 'else the last clause ?
                                (cons 'begin clause-body)
                                (syntax-error cur-clause "(case...): else must be the last clause"))
                            ;; Regular clause
                            (begin
                                (if (not (list? datum-list))
                                    (syntax-error cur-clause "(case...): datum list expected"))
                                (__%apnd
                                    ;; (if (..match..) body
                                    (list
                                        'if
                                        ;; (or (eqv? ..)(eqv? ..))
                                        (cons
                                            'or
                                            (let loop-datum ([datum-list datum-list])
                                                (if (null? datum-list)
                                                    '()
                                                    (cons
                                                        (list 'eqv?
                                                              tmp
                                                              (list 'quote (car datum-list)))
                                                        (loop-datum (cdr datum-list))))))
                                        (cons 'begin clause-body))
                                    ;; Append the 'else' part of the 'if':
                                    ;; either recurse into the next clause, or nothing
                                    (if (null? (cdr clauses))
                                        '()
                                        (list (recurse-clause (cdr clauses)))))))))))))


;; Based on Scheme48
(define-macro (quasiquote exp)
    (define (expand-quasiquote x level)
      (descend-quasiquote x level finalize-quasiquote))

    (define (finalize-quasiquote mode arg)
      (cond ((eq? mode 'quote) (list 'quote arg))
            ((eq? mode 'unquote) arg)
            ((eq? mode 'unquote-splicing)
             (syntax-error ",@ in invalid context" arg))
            (else (cons mode arg))))

    (define (descend-quasiquote x level return)
      (cond ((vector? x)
             (descend-quasiquote-vector x level return))
            ((not (pair? x))
             (return 'quote x))
            ((interesting-to-quasiquote? x 'quasiquote)
             (descend-quasiquote-pair x (+ level 1) return))
            ((interesting-to-quasiquote? x 'unquote)
             (cond ((= level 0)
                    (return 'unquote (cadr x)))
                   (else
                    (descend-quasiquote-pair x (- level 1) return))))
            ((interesting-to-quasiquote? x 'unquote-splicing)
             (cond ((= level 0)
                    (return 'unquote-splicing (cadr x)))
                   (else
                    (descend-quasiquote-pair x (- level 1) return))))
            (else
             (descend-quasiquote-pair x level return))))

    (define (descend-quasiquote-pair x level return)
      (descend-quasiquote (car x) level
        (lambda (car-mode car-arg)
          (descend-quasiquote (cdr x) level
            (lambda (cdr-mode cdr-arg)
              (cond ((and (eq? car-mode 'quote)
                          (eq? cdr-mode 'quote))
                     (return 'quote x))
                    ((eq? car-mode 'unquote-splicing)
                     ;; (,@mumble ...)
                     (cond ((and (eq? cdr-mode 'quote) (null? cdr-arg))
                            (return 'unquote
                                    car-arg))
                           (else
                            (return 'append
                                    (list car-arg (finalize-quasiquote
                                                     cdr-mode cdr-arg))))))
                    (else
                     (return 'cons
                             (list (finalize-quasiquote car-mode car-arg)
                                   (finalize-quasiquote cdr-mode cdr-arg))))))))))

    (define (descend-quasiquote-vector x level return)
      (descend-quasiquote (vector->list x) level
        (lambda (mode arg)
          (cond ((eq? mode 'quote)
                 (return 'quote x))
                (else
                 (return 'list->vector
                         (list (finalize-quasiquote mode arg))))))))

    (define (interesting-to-quasiquote? x marker)
      (and (pair? x)
           (eq? (car x) marker)))

    (expand-quasiquote exp 0))


)

