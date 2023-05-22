lexer grammar AlgumaLexer;

// =========================== RESERVED KEYWORDS AND IDENTIFIERS =============================

PALAVRA_CHAVE:
    'algoritmo' | 'declare' | 'literal' | 'inteiro' | 'leia' | 'escreva' | 'fim_algoritmo' |
    'real' | 'logico' | 'e' | 'ou' | 'nao' | 'se' | 'entao' | 'senao' | 'fim_se' | 'caso' |
    'seja' | '..' | 'fim_caso' | 'constante' | 'tipo' | 'registro' | 'fim_registro' |
    'procedimento' | 'fim_procedimento' | 'var' | '.' | 'funcao' | 'retorne' | 'fim_funcao' |
    'para' | 'ate' | 'faca' | 'fim_para' | 'enquanto' | 'fim_enquanto' | '^' | '&' |
    'verdadeiro' | 'falso';

IDENT:
    ('a'..'z' | 'A'..'Z') ('a'..'z' | 'A'..'Z' | '0'..'9' | '_')*;

// =========================== CADEIA DE LITERAIS HANDLING =============================

fragment
ESC_SEQ:
    '\\"';

CADEIA:
    '"' ( ESC_SEQ | ~( '"' | '\\' | '\n' | '\r') )* '"';

// this results in an error
UNCLOSED_CADEIA:
    '"' (~( '"' | '\n' | '\r' ))* ('\n' | '\r');

// =========================== COMMENT HANDLING =============================

COMMENT:
    '{' ~( '}' | '\n' | '\r')* '}' { skip(); };

// this results in an error
UNCLOSED_COMMENT:
    '{' ~( '}' | '\n' | '\r' )* ('\n' | '\r');

// =========================== NUMBER HANDLING =============================

NUM_INT:
    ('0'..'9')+;

NUM_REAL:
    ('0'..'9')+ '.' ('0'..'9')+;

// =========================== ARITHMETIC OPERATIONS =============================

fragment
MULT:
    '*';

fragment
SUM:
    '+';

fragment
SUB:
    '-';

fragment
DIV:
    '/';

fragment
MOD:
    '%';

ARIT_OP:
    ( SUM | SUB | MULT | DIV | MOD);

// =========================== RELATIONAL OPERATIONS =============================

fragment
EQU:
    '=';

fragment
NEQ:
    '<>';

fragment
LSS:
    '<';

fragment
GTR:
    '>';

fragment
GEQ:
    '>=';

fragment
LEQ:
    '<=';

RELAC_OP:
    ( EQU | NEQ | LSS | GTR | GEQ | LEQ );

// =========================== ARRAY INDEXING OPERATIONS =============================

INDEX_OP:
    '[' | ']';

// =========================== SYMBOLS THAT SEPARATE STATEMENTS =============================
// we couldn't think of a better name for them

fragment
DELIM:
    ':';

fragment
ABREPAR:
    '(';

fragment
FECHAPAR:
    ')';

fragment
VIRGULA:
    ',';

fragment
ATRIBUTION:
    '<-';

SEPARATION_SYMBOL:
    ( DELIM | ABREPAR | FECHAPAR | VIRGULA | ATRIBUTION );

// =========================== IGNORED SYMBOLS =============================

fragment
WHITE_SPACE:
    ' ';

fragment
ENDLINE:
    '\n';

fragment
TAB:
    '\t';

IGNORABLE_SYMBOL:
    ( WHITE_SPACE | ENDLINE | TAB ) { skip(); };

// =========================== UNIDENTIFIED SYMBOLS =============================
// if all else failed, this lexical rule catches it

UNIDENTIFIED_SYMBOL:
    .;