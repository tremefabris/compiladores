grammar LA;

// ==============================================================================
// ==============================SYNTATIC RULES==================================
// ==============================================================================

programa:
    declaracoes 'algoritmo' corpo 'fim_algoritmo';

declaracoes:
    (decl_local_global)*;

decl_local_global:
    declaracao_local | declaracao_global;

declaracao_local:
    'declare' variavel |
    'constante' IDENT ':' tipo_basico '=' valor_constante |
    'tipo' IDENT ':' tipo;

variavel:
    identificador (',' identificador)* ':' tipo;

identificador:
    IDENT ('.' IDENT)* dimensao;

dimensao:
    ('[' exp_aritmetica ']')*;

tipo:
    registro | tipo_estendido;

tipo_basico:
    'literal' | 'inteiro' | 'real' | 'logico';

tipo_basico_ident:
    tipo_basico | IDENT;

tipo_estendido:
    '^'? tipo_basico_ident;

valor_constante:
    CADEIA | NUM_INT | NUM_REAL | 'verdadeiro' | 'falso';

registro:
    'registro' (variavel)* 'fim_registro';

declaracao_global:
    'procedimento' IDENT '(' parametros? ')' (declaracao_local)* (cmd)* 'fim_procedimento' |
    'funcao' IDENT '(' parametros? ')' ':' tipo_estendido (declaracao_local)* (cmd)* 'fim_funcao';

parametro:
    ('var')? identificador (',' identificador)* ':' tipo_estendido;

parametros:
    parametro (',' parametro)*;

corpo:
    (declaracao_local)* (cmd)*;

cmd:
    cmdLeia |
    cmdEscreva |
    cmdSe |
    cmdCaso |
    cmdPara |    
    cmdEnquanto |
    cmdFaca |
    cmdAtribuicao |
    cmdChamada |
    cmdRetorne ;

cmdLeia:
    'leia' '(' ('^')? identificador (',' ('^')? identificador)* ')' ;

cmdEscreva:
    'escreva' '(' expressao (',' expressao)* ')' ;
    
cmdSe:
    'se' expressao 'entao' (comandose=cmd)* ('senao' (comandosenao=cmd)*)? 'fim_se' ;

cmdCaso:
    'caso' exp_aritmetica 'seja' selecao ('senao' (cmd)*)? 'fim_caso' ;

cmdPara: 
    'para' IDENT '<-' exp_aritmetica 'ate' exp_aritmetica 'faca' (cmd)* 'fim_para' ;

cmdEnquanto:
    'enquanto' expressao 'faca' (cmd)* 'fim_enquanto' ;

cmdFaca:
    'faca' (cmd)* 'ate' expressao ;

cmdAtribuicao:
    ('^')? identificador '<-' expressao ;

cmdChamada:
    IDENT '(' expressao (',' expressao)* ')' ;

cmdRetorne:
    'retorne' expressao ;

selecao:
    (item_selecao)* ;

item_selecao:
    constantes ':' (cmd)* ;

constantes:
    numero_intervalo (',' numero_intervalo)* ;

numero_intervalo:
    (op_unario)? NUM_INT ('..' (op_unario)? NUM_INT)? ;

op_unario:
    '-' ;

exp_aritmetica:
    termo (op1 termo)* ;

termo:
    fator (op2 fator)* ;

fator: 
    parcela (op3 parcela)* ;

op1:
    '+' |
    '-' ;

op2:
    '*' |
    '/' ;    

op3:
    '%' ;

parcela:
    (op_unario)? parcela_unario |
    parcela_nao_unario ;

parcela_unario:
    ('^')? identificador |
    IDENT '(' expressao (',' expressao)* ')' |
    NUM_INT |
    NUM_REAL |
    '(' expressao ')' ;

parcela_nao_unario:
    '&' identificador |
    CADEIA ;

exp_relacional:
    exp_aritmetica (op_relacional exp_aritmetica)? ;

op_relacional:
    '=' |
    '<>' |
    '>=' |
    '<=' |
    '<' |
    '>' ;

expressao:
    termo_logico (op_logico_1 termo_logico)* ;

termo_logico:
    fator_logico (op_logico_2 fator_logico)* ;

fator_logico:
    ('nao')? parcela_logica ;

parcela_logica:
    ('verdadeiro' | 'falso') |
    exp_relacional ;

op_logico_1:
    'ou' ;

op_logico_2:
    'e' ;


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