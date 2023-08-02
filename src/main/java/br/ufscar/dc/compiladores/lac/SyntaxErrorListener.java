package br.ufscar.dc.compiladores.lac;

import java.util.BitSet;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * Implementation for a SyntaxErrorListener
 * 
 * Does basically one thing: creates a custom error message
 * for syntax errors caught amidst the program.
 * 
 * Only implements the syntaxError function.
 */
public class SyntaxErrorListener implements ANTLRErrorListener {

    @Override
    public void reportAmbiguity(Parser arg0, DFA arg1, int arg2, int arg3, boolean arg4, BitSet arg5,
            ATNConfigSet arg6) {
        // TODO: Not necessary for T2  
    }

    @Override
    public void reportAttemptingFullContext(Parser arg0, DFA arg1, int arg2, int arg3, BitSet arg4, ATNConfigSet arg5) {
        // TODO: Not necessary for T2
    }

    @Override
    public void reportContextSensitivity(Parser arg0, DFA arg1, int arg2, int arg3, int arg4, ATNConfigSet arg5) {
        // TODO: Not necessary for T2        
    }

    @Override
    public void syntaxError(
                    Recognizer<?, ?> recognizer,
                    Object offendingSymbol,
                    int line,
                    int charPositionInLine,
                    String msg,
                    RecognitionException e
    ) {
        String errorMessage = null;

        Token t = (Token) offendingSymbol;

        String t_text = t.getText();
        if (t_text.equals("<EOF>")) {
            t_text = "EOF";
        }

        errorMessage = "Linha " + line
                       + ": erro sintatico proximo a " + t_text;

        throw new ParseCancellationException(errorMessage);
    }
}