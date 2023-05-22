package br.ufscar.dc.compiladores.alguma.lexico;

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

/**
 * TODO: COMMENT ABOUT MAIN FUNCTIONALITIES OF PRINCIPAL CLASS
 */
public class Principal {
    
    // Used to open (or create) the output file
    static void guaranteeExistingFile(String fileName) {
        try {
            File outFile = new File(fileName);
            outFile.createNewFile();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    static String checkForLexicalError(Token token, String tokenType) {
        String errorMessage = null;
        if (tokenType == "UNIDENTIFIED_SYMBOL")
            errorMessage = "Linha " + token.getLine() + ": " +
                            token.getText() + " - simbolo nao identificado";

        else if (tokenType == "UNCLOSED_COMMENT")
            errorMessage = "Linha " + token.getLine() + ": comentario nao fechado";

        else if (tokenType == "UNCLOSED_CADEIA")
            errorMessage = "Linha " + token.getLine() + ": cadeia literal nao fechada";
        
        return errorMessage;
    }

    public static void main(String[] args) {
        try{
            String errorMessage = null;

            CharStream cs = CharStreams.fromFileName(args[0]);
            AlgumaLexer lex = new AlgumaLexer(cs);

            String outputFilename = args[1];
            guaranteeExistingFile(outputFilename);  // If it didn't exit, the file now exists
            PrintWriter outputWriter = new PrintWriter(outputFilename, "UTF-8");

            Token t = null;
            while((t = lex.nextToken()).getType() != Token.EOF) {

                String tokenText = "\'" + t.getText() + "\'";
                String tokenType = AlgumaLexer.VOCABULARY.getDisplayName(t.getType());
                
                /**
                 * TODO: WRITE COMMENT
                 * 
                 * Handling errors
                 */
                if ((errorMessage = checkForLexicalError(t, tokenType)) != null) {
                    System.out.println(errorMessage);
                    outputWriter.write(errorMessage);
                    break;
                }

                /**
                 * TODO: WRITE COMMENT
                 * 
                 * Handling rules with special print-outs
                 */
                if  (
                    tokenType == "PALAVRA_CHAVE"     ||  // Keywords
                    tokenType == "SEPARATION_SYMBOL" ||  // Symbols that separate statements and expressions
                    tokenType == "ARIT_OP"           ||  // Arithmetic operations
                    tokenType == "RELAC_OP"          ||  // Relational operations
                    tokenType == "INDEX_OP"              // Array indexing symbols
                    )
                {
                    tokenType = tokenText;
                }
                
                String parsedLine = "<" + tokenText + "," + tokenType + ">";
                
                outputWriter.println(parsedLine);
                System.out.println(parsedLine);
            }
            outputWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
