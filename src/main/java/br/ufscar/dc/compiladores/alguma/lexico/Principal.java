package br.ufscar.dc.compiladores.alguma.lexico;

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;


public class Principal {
    
    /*
     * Guarantees that the file 'filePath' exists.
     * If such file already exists, then nothing is done.
     * Otherwise, it is created. 
     * 
     * Note that if the method 'createNewFile' is unable
     * to create the file, it will throw an IOException,
     * which will lead to termination of the program.
     */
    static void guaranteeExistingFile(String filePath) {
        try {
            File outFile = new File(filePath);
            outFile.createNewFile();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /*
     * Helper function.
     * 
     * Checks if the received token lies under one of the
     * "error rules". If that is the case, this function
     * returns the correct error message associated with it.
     * Otherwise, null is returned, indicating that the token
     * is not an "error".
     */
    static String checkForLexicalError(Token token, String tokenType) {
        String errorMessage = null;

        if (tokenType == "UNIDENTIFIED_SYMBOL")
            errorMessage = "Linha " + token.getLine() + ": " +
                            token.getText() + " - simbolo nao identificado\n";

        else if (tokenType == "UNCLOSED_COMMENT")
            errorMessage = "Linha " + token.getLine() + ": comentario nao fechado\n";

        else if (tokenType == "UNCLOSED_CADEIA")
            errorMessage = "Linha " + token.getLine() + ": cadeia literal nao fechada\n";
        
        return errorMessage;
    }

    /*
     * Main function.
     * 
     * Instantiates a CharStream and an AlgumaLexer (our primary
     * lexer class) to read through a program file. Also opens
     * a PrintWriter to store the program's output.
     * 
     * Receives two command-line arguments:
     * - program file path; and
     * - output file path.
     * 
     * The internal 'while' loop holds most of this program's
     * logic: it receives tokens; extracts their text and type;
     * checks for errors; and correctly outputs the necessary
     * information.
     * 
     * Look inside for more details.
     */
    public static void main(String[] args) {
        try{
            String errorMessage = null;

            CharStream cs = CharStreams.fromFileName(args[0]);
            AlgumaLexer lex = new AlgumaLexer(cs);

            String outputFilename = args[1];
            guaranteeExistingFile(outputFilename);
            PrintWriter outputWriter = new PrintWriter(outputFilename, "UTF-8");

            Token t = null;
            while((t = lex.nextToken()).getType() != Token.EOF) {

                String tokenText = "\'" + t.getText() + "\'";
                String tokenType = AlgumaLexer.VOCABULARY.getDisplayName(t.getType());
                
                /**
                 * Checks for errors before continuing
                 */
                if ((errorMessage = checkForLexicalError(t, tokenType)) != null) {
                    System.out.println(errorMessage);
                    outputWriter.write(errorMessage);
                    break;
                }

                /**
                 * Some lexical rules need to be printed as their
                 * "display name" - see tokenType definition above -,
                 * but some need to be printed as their own name.
                 * 
                 * This if-statement makes sure the latter get its
                 * proper treatment.
                 */
                if  (
                    tokenType == "PALAVRA_CHAVE"     ||  // Reserved keywords
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
                // System.out.println(parsedLine);
            }
            outputWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}