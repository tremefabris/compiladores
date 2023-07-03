package br.ufscar.dc.compiladores.lac;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.Token;


public class LASemanticUtils {
    public static List<String> semanticErrors = new ArrayList<>();

    public static void addSemanticError(Token token, String msg) {
        int line = token.getLine();
        int column = token.getCharPositionInLine();

        semanticErrors.add(String.format(
            "Erro %d:%d - %s", line, column, msg
        ));
    }
}
