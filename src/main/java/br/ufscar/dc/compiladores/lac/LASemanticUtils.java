package br.ufscar.dc.compiladores.lac;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
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
    verifyType(Scopes scopes, LAParser.ExpressaoContext ctx) {
        LAType ret = null;

        for (LAParser.Termo_logicoContext tlc: ctx.termo_logico()) {
            LAType aux = verifyType(scopes, tlc);

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
    verifyType(Scopes scopes, LAParser.Termo_logicoContext ctx) {
        LAType ret = null;

        for (LAParser.Fator_logicoContext flc: ctx.fator_logico()) {
            LAType aux = verifyType(scopes, flc);

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
    verifyType(Scopes scopes, LAParser.Fator_logicoContext ctx) {
        LAType ret = verifyType(scopes, ctx.parcela_logica());
        return ret;
    }

    /*
     * PARCELA LOGICA VERIFICATION
     */
    public static LAType
    verifyType(Scopes scopes, LAParser.Parcela_logicaContext ctx) {
        LAType ret = null;

        if (ctx.exp_relacional() == null) {    // means it's ('verdadeiro' | 'falso')
            ret = LAType.LOGICAL;
        } else {                               // means it's exp_relacional
            ret = verifyType(
                scopes, ctx.exp_relacional()
            );
        }

        return ret;
    }

    /*
     * EXPRESSAO RELACIONAL VERIFICATION
     */
    public static LAType
    verifyType(Scopes scopes, LAParser.Exp_relacionalContext ctx) {
        LAType ret = null;

        for (LAParser.Exp_aritmeticaContext eac: ctx.exp_aritmetica()) {
            LAType aux = verifyType(scopes, eac);

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
    verifyType(Scopes scopes, LAParser.Exp_aritmeticaContext ctx) {
        LAType ret = null;

        for (LAParser.TermoContext tc: ctx.termo()) {
            LAType aux = verifyType(scopes, tc);

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
    verifyType(Scopes scopes, LAParser.TermoContext ctx) {
        LAType ret = null;

        for (LAParser.FatorContext fc: ctx.fator()) {
            LAType aux = verifyType(scopes, fc);

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
    verifyType(Scopes scopes, LAParser.FatorContext ctx) {
        LAType ret = null;

        for (LAParser.ParcelaContext pc: ctx.parcela()) {
            LAType aux = verifyType(scopes, pc);

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
    verifyType(Scopes scopes, LAParser.ParcelaContext ctx) {
        LAType ret = null;

        if (ctx.parcela_unario() != null) {

            ret = verifyType(scopes, ctx.parcela_unario());

        } else {  // parcela_nao_unario

            ret = verifyType(scopes, ctx.parcela_nao_unario());

        }

        return ret;
    }

    /*
     * PARCELA NAO UNARIA VERIFICATION
     */
    public static LAType
    verifyType(Scopes scopes, LAParser.Parcela_nao_unarioContext ctx) {
        
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
    verifyType(Scopes scopes, LAParser.Parcela_unarioContext ctx) {

        /*
         * If it's a variable...
         */
        if (ctx.identificador() != null) {

            String ident_name;

            /*
             * If the variable is an array...
             */
            if (LASemanticUtils.isArray(ctx.identificador())) {
                ident_name = formatArrayIdent(ctx.identificador(), false);
            }
            /*
             * If it isn't an array...
             */
            else {
                ident_name = ctx.identificador().getText();
            }

            LAType ident_type = null;

            /*
             * Checking variable existence...
             */
            if (scopes.exists(ident_name)) {
                ident_type = scopes.verifyType(ident_name);
            } else {
                return null;
            }

            /*
             * Dereferencing if needed...
             */
            if (ctx.getText().contains("^")) {

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
         * If it's a function...
         */
        } else if (ctx.IDENT() != null) {

            return scopes.verifyType(ctx.IDENT().getText());
        
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

            return verifyType(scopes, ctx.expressao(0));

        }
    }

    /*
     * TYPE INCOMPATIBILY VERIFICATION
     */
    public static boolean areTypesIncompatible(LAType var, LAType exp) {
        boolean ret = (
            var != exp &&
            !(
                (var == LAType.INTEGER && exp == LAType.REAL) ||
                (var == LAType.REAL && exp == LAType.INTEGER)
            ) &&
            !(
                (var == LAType.PTR_INTEGER && exp == LAType.MEM_ADDR)
            )
        );

        return ret;
    }

    /*
     * CHECKS IF GIVEN IDENTIFIER IS PART OF A 
     * REGISTER'S DECLARATION
     */
    public static boolean isRegisterAttribute(LAParser.IdentificadorContext ctx) {

        boolean ret = (ctx.getParent().getParent() instanceof LAParser.RegistroContext);
        return ret;

    }

    /*
     * RETRIEVES A LIST OF VARIABLE NAMES TO
     * PROPERLY CONSTRUCT REGISTERS
     */
    public static List<String> getRegisterVariableNames(LAParser.IdentificadorContext ctx) {

        List<String> reg_var_names = new ArrayList<>();

        ParserRuleContext ctx_gggfather = ctx.getParent()
                                             .getParent()
                                             .getParent()
                                             .getParent();

        /*
         * If we are deep in a variavel rule...
         */
        if (ctx_gggfather instanceof LAParser.VariavelContext) {

            LAParser.VariavelContext
            reg_decl_ctx = (LAParser.VariavelContext) ctx_gggfather;

            /*
             * For each variable being created...
             */
            for (LAParser.IdentificadorContext ic: reg_decl_ctx.identificador()) {
                reg_var_names.add(ic.getText());
            }

        /*
         * If we are deep in a custom type creation rule...
         */
        } else if (ctx_gggfather instanceof LAParser.Declaracao_localContext) {

            LAParser.Declaracao_localContext
            reg_decl_ctx = (LAParser.Declaracao_localContext) ctx_gggfather;

            /*
             * Only one "variable" (custom type) being created
             * at a time, so no need for for-loop.
             */
            reg_var_names.add(reg_decl_ctx.IDENT().getText());

        }


        return reg_var_names;
    }

    /*
     * RETRIEVES A LIST OF VARIABLE NAMES TO
     * PROPERLY CONSTRUCT CUSTOM TYPES
     */
    public static List<String> getCustomTypeVariableNames(LAParser.Tipo_basico_identContext ctx) {

        /*
         * Here, we retrieve the identifier of variables
         * that need to be properly initialized for a
         * custom type.
         */

        List<String> ct_var_names = new ArrayList<>();

        ParserRuleContext ctx_gfather = ctx.getParent()
                                           .getParent();

        /*
         * If we are in a variable declaration (from
         * 'variavel' grammar rule, for example)...
         */
        if (ctx_gfather.getParent() instanceof LAParser.VariavelContext) {

            LAParser.VariavelContext
            ct_decl_ctx = (LAParser.VariavelContext) ctx_gfather.getParent();

            for (LAParser.IdentificadorContext ic: ct_decl_ctx.identificador()) {

                ct_var_names.add(ic.getText());

            }
        }
        /*
         * If we are in a function declaration...
         */
        else if (ctx_gfather instanceof LAParser.ParametroContext) {
            
            LAParser.ParametroContext
            ct_decl_ctx = (LAParser.ParametroContext) ctx_gfather;

            for (LAParser.IdentificadorContext ic: ct_decl_ctx.identificador()) {

                ct_var_names.add(ic.getText());

            }
        }

        return ct_var_names;
    }

    /*
     * ARRAY VERIFICATION
     */
    public static boolean isArray(LAParser.IdentificadorContext ctx) {
        String ident_text = ctx.getText();

        /*
         * Here, we consider every identifier that has
         * both "[" and "]" as an array.
         */
        return (
            ident_text.contains("[") &&
            ident_text.contains("]")
        );
    }

    /*
     * ARRAY SIZE (AND INDEX) RETRIEVAL
     */
    public static String getArraySize(LAParser.IdentificadorContext ctx) {
        String ident_text = ctx.getText();

        /*
         * Basically just returning anything that's
         * between brackets.
         */
        return ident_text.substring(
                    ident_text.indexOf("[") + 1, ident_text.indexOf("]")
        );
    }

    /*
     * ARRAY IDENTIFIER TRANSFORMATION FOR
     * SYMBOL TABLE LOOK-UP
     */
    public static String formatArrayIdent(LAParser.IdentificadorContext ctx, boolean reverse) {
        
        /*
         * Since our arrays are stored like "array.0" instead
         * of "array[0]", we need to arrange such a
         * transformation for visitors to use.
         * 
         * We also provide a reverse funcionality for exhibition
         * to the client.
         */
        
        if (!reverse) {
            String array_ref = LASemanticUtils.getArraySize(ctx);
            String array_ident = ctx.getText().substring(0, ctx.getText().indexOf("["));

            int array_size;
            try {
                // Try to convert array reference to an Integer
                array_size = Integer.parseInt(array_ref);

            } catch (NumberFormatException e) {
                // Otherwise (if it isn't an Integer), work around it
                array_size = 0;

            }

            return array_ident + "." + Integer.toString(array_size);
        }
        else {
            String array_ident = ctx.getText().replace('.', '[');
            return array_ident;
        }
    }

}
