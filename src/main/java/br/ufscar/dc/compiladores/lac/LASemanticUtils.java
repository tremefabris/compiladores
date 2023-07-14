package br.ufscar.dc.compiladores.lac;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.Token;

import br.ufscar.dc.compiladores.lac.SymbolTable.LAType;


public class LASemanticUtils {
    public static List<String> semanticErrors = new ArrayList<>();

    public static void addSemanticError(Token token, String msg) {
        int line = token.getLine();

        semanticErrors.add(String.format(
            "Linha %d: %s", line, msg
        ));
    }

    /*
     * TYPE VERIFICATION
     * 
     * The semantic error handling is done at higher-level.
     * 
     * The business logic for these functions is: if the expression
     * follows a declared type (including LAType.INVALID), then that
     * type is returned. Else, LAType.INVALID is returned.
     * Therefore, null serves to indicate that the expression is faulty.
     */

    /*
     * EXPRESSAO VERIFICATION
     */
    public static LAType
    verifyType(SymbolTable table, LAParser.ExpressaoContext ctx) {
        LAType ret = null;

        for (LAParser.Termo_logicoContext tlc: ctx.termo_logico()) {
            LAType aux = verifyType(table, tlc);

            if (ret == null) {
                ret = aux;
            } else if (ret != aux && aux != LAType.INVALID) {
                return LAType.INVALID;
            }
        }
        
        return ret;
    }

    /*
     * TERMO LOGICO VERIFICATION
     */
    public static LAType
    verifyType(SymbolTable table, LAParser.Termo_logicoContext ctx) {
        LAType ret = null;

        for (LAParser.Fator_logicoContext flc: ctx.fator_logico()) {
            LAType aux = verifyType(table, flc);

            if (ret == null) {
                ret = aux;
            } else if (ret != aux && aux != LAType.INVALID) {
                return LAType.INVALID;
            }
        }

        return ret;
    }

    /*
     * FATOR LOGICO VERIFICATION
     */
    public static LAType
    verifyType(SymbolTable table, LAParser.Fator_logicoContext ctx) {
        LAType ret = verifyType(table, ctx.parcela_logica());
        return ret;
    }

    /*
     * PARCELA LOGICA VERIFICATION
     */
    public static LAType
    verifyType(SymbolTable table, LAParser.Parcela_logicaContext ctx) {
        LAType ret = null;

        if (ctx.exp_relacional() == null) {    // means it's ('verdadeiro' | 'falso')
            ret = LAType.LOGICAL;
        } else {                               // means it's exp_relacional
            ret = verifyType(
                table, ctx.exp_relacional()
            );
        }

        return ret;
    }

    /*
     * EXPRESSAO RELACIONAL VERIFICATION
     */
    public static LAType
    verifyType(SymbolTable table, LAParser.Exp_relacionalContext ctx) {
        LAType ret = null;

        for (LAParser.Exp_aritmeticaContext eac: ctx.exp_aritmetica()) {
            LAType aux = verifyType(table, eac);

            if (ret == null) {
                ret = aux;
            } else if (ret != aux && aux != LAType.INVALID) {
                return LAType.INVALID;
            }
        }

        /*
         * If there is ANY relational operation (<, >, =, etc),
         * then its type is LOGICAL.
         */
        if (ctx.op_relacional() != null) {
            return LAType.LOGICAL;
        }

        return ret;
    }


    /*
     * EXPRESSAO ARITMETICA VERIFICATION
     */
    public static LAType
    verifyType(SymbolTable table, LAParser.Exp_aritmeticaContext ctx) {
        LAType ret = null;

        for (LAParser.TermoContext tc: ctx.termo()) {
            LAType aux = verifyType(table, tc);

            if (ret == null) {
                ret = aux;
            } else if (ret != aux && aux != LAType.INVALID) {
                return LAType.INVALID;
            }
        }

        return ret;
    }

    /*
     * TERMO VERIFICATION
     */
    public static LAType
    verifyType(SymbolTable table, LAParser.TermoContext ctx) {
        LAType ret = null;

        for (LAParser.FatorContext fc: ctx.fator()) {
            LAType aux = verifyType(table, fc);

            if (ret == null) {
                ret = aux;
            } else if (ret != aux && aux != LAType.INVALID) {

                /*
                 * INTEGERs and REALs should be interchangeable.
                 * Notice we're typecasting to REAL.
                 */
                if (
                        (ret == LAType.REAL && aux == LAType.INTEGER) ||
                        (ret == LAType.INTEGER && aux == LAType.REAL)
                ) {
                    return LAType.REAL;
                } else {
                    return LAType.INVALID;
                }
            }
        }

        return ret;
    }

    /*
     * FATOR VERIFICATION
     */
    public static LAType
    verifyType(SymbolTable table, LAParser.FatorContext ctx) {
        LAType ret = null;

        for (LAParser.ParcelaContext pc: ctx.parcela()) {
            LAType aux = verifyType(table, pc);

            if (ret == null) {
                ret = aux;
            } else if (ret != aux && aux != LAType.INVALID) {
                return LAType.INVALID;
            }
        }

        return ret;
    }


    /*
     * PARCELA VERIFICATION
     */
    public static LAType
    verifyType(SymbolTable table, LAParser.ParcelaContext ctx) {
        LAType ret = null;

        if (ctx.parcela_unario() != null) {

            ret = verifyType(table, ctx.parcela_unario());

        } else {  // parcela_nao_unario

            ret = verifyType(table, ctx.parcela_nao_unario());

        }

        return ret;
    }

    /*
     * PARCELA NAO UNARIA VERIFICATION
     */
    public static LAType
    verifyType(SymbolTable table, LAParser.Parcela_nao_unarioContext ctx) {
        
        if (ctx.identificador() != null) {
            return LAType.MEM_ADDR;
        } else {  // CADEIA
            return LAType.LITERAL;
        }
    
    }

    /*
     * PARCELA UNARIA VERIFICATION
     */
    public static LAType
    verifyType(SymbolTable table, LAParser.Parcela_unarioContext ctx) {

        /*
         * If it's a variable...
         */
        if (ctx.identificador() != null) {

            String ident_name = ctx.identificador().getText();
            LAType ident_type = null;

            if (table.exists(ident_name)) {
                ident_type = table.verify(ident_name);
            } else {
                return null;
            }

            if (ctx.getText().contains("^")) {    // dereference operator

                switch(ident_type) {
                    case PTR_INTEGER:
                        ident_type = LAType.INTEGER;
                        break;
                    default:
                        ident_type = null;
                        break;
                }

            }

            return ident_type;

        /*
         * Not sure what this even means...
         */
        } else if (ctx.IDENT() != null) {
            
            LAType ret = null;
            for (LAParser.ExpressaoContext ec: ctx.expressao()) {
                LAType aux = verifyType(table, ec);

                if (ret == null) {
                    ret = aux;
                } else if (ret != aux && aux != LAType.INVALID) {
                    return LAType.INVALID;
                }
            }

            return ret;
        
        /*
         * If it's an integer...
         */
        } else if (ctx.NUM_INT() != null) {

            return LAType.INTEGER;

        /*
         * If it's a real number...
         */
        } else if (ctx.NUM_REAL() != null) {

            return LAType.REAL;

        /*
         * If it's a nested expression
         */
        } else {  // '(' expressao ')'

            return verifyType(table, ctx.expressao(0));

        }
    }

}
