lexer grammar AlgumaLexer;

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

UNCLOSED_CADEIA:
    '"' (~( '"' | '\n' | '\r' ))* ('\n' | '\r');

// =========================== COMMENT HANDLING =============================

COMMENT:
    '{' ~('\n' | '\r')* '}' { skip(); };

UNCLOSED_COMMENT:
    '{' ~( '}' | '\n' | '\r' )* ('\n' | '\r');

NUM_INT:
    ('0'..'9')+;

NUM_REAL:
    ('0'..'9')+ '.' ('0'..'9')+;

ARIT_OP:
    ( SUM | SUB | MULT | DIV | MOD);

RELAC_OP:
    ( EQU | NEQ | LSS | GTR | GEQ | LEQ );

INDEX_OP:
    '[' | ']';

SEPARATION_SYMBOL:
    ( DELIM | ABREPAR | FECHAPAR | VIRGULA | ATRIBUTION );

IGNORABLE_SYMBOL:
    ( WHITE_SPACE | ENDLINE | TAB ) { skip(); };

// =========================== FRAGMENTS FOR ABOVE DEFINITIONS =============================
// and below everything the "catch all else as error" rule

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

fragment
WHITE_SPACE:
    ' ';

fragment
ENDLINE:
    '\n';

fragment
TAB:
    '\t';

UNIDENTIFIED_SYMBOL:
    . {
        // segundo o Lucredio, nao deve ser tratado aqui
        // throw new Exception("UNIDENTIFIED_SYMBOL");
        // System.out.println("Linha " + getLine() + ": " + getText() + " - simbolo nao identificado" );
        // skip();
        // System.exit(0);
    };