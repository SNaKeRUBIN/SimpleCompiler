package Scanner;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 *
 * @author SNaKeRUBIN
 */
public class LexicalAnalyser {

    public static boolean state = true;
    public static boolean slashslashComment = false;
    public static boolean slashstarComment = false;

    public static File input = null;
    public static File outputTokens = null;
    public static File outputErrorsScanner = null;

    //public static void main(String[] args) throws IOException {
    public static void setOutputFileLocations(String new_input, String new_tokens,
            String new_errors_scanner) {
        input = new File(new_input);
        outputTokens = new File(new_tokens);
        outputErrorsScanner = new File(new_errors_scanner);
    }

    public static ArrayList<Token> getTokens() throws IOException {
        ArrayList<Token> tokenList = new ArrayList<>();
        Token EndOfTokens = new Token();
        int lineCntr = -1;
        int colCntr = -1;
        try {

            Scanner fileScanner = new Scanner(input);

            lineCntr = -1;

            List<Token> errorlist = new ArrayList<>();

            while (fileScanner.hasNextLine()) {

                String line = fileScanner.nextLine();
                lineCntr++;
                colCntr = -1;

                StringBuilder sb = new StringBuilder(line);
                sb.append('#');                                 // '#' is EOL character

                InputStream stream = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
                Reader r = new BufferedReader(new InputStreamReader(stream));

                StreamTokenizer st = new StreamTokenizer(r);
                st = configTokenizer(st);

                while (state) {
                    colCntr++;
                    Token tk;

                    tk = read(st);
                    tk.setLine(lineCntr);
                    tk.setColumn(colCntr);
                    //tk.printToken();

                    switch (tk.getType()) {
                        case "CMT":
                            break;
                        case "ERROR":
                            errorlist.add(tk);
                            break;
                        case "EOL":
                            break;
                        default:
                            tokenList.add(tk);
                            break;
                    }
                }
                state = true;
            }

            fileScanner.close();
            writeFiles(tokenList, errorlist, outputTokens, outputErrorsScanner);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(LexicalAnalyser.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        EndOfTokens.setType("$");
        EndOfTokens.setLine(lineCntr + 1);
        EndOfTokens.setColumn(colCntr + 1);

        tokenList.add(EndOfTokens);

        return tokenList;
    }

    public static Token read(StreamTokenizer st) throws IOException {

        boolean localComment = false;

        if (slashslashComment || slashstarComment) {
            localComment = true;
        }

        int c = st.nextToken();
        Token token = new Token();

        switch (c) {
            case '=':
                if (st.nextToken() == '=') {
                    token.setType("==");
                    token.setLexeme("==");
                } else {
                    token.setType("=");
                    token.setLexeme("=");
                    st.pushBack();
                }
                break;
            case '<':
                switch (st.nextToken()) {
                    case '=':
                        token.setType("<=");
                        token.setLexeme("<=");
                        break;
                    case '>':
                        token.setType("<>");
                        token.setLexeme("<>");
                        break;
                    default:
                        token.setType("<");
                        token.setLexeme("<");
                        st.pushBack();
                        break;
                }
                break;
            case '>':
                switch (st.nextToken()) {
                    case '=':
                        token.setType(">=");
                        token.setLexeme(">=");
                        break;
                    default:
                        token.setType(">");
                        token.setLexeme(">");
                        st.pushBack();
                }
                break;
            case ';':
                token.setType(";");
                token.setLexeme(";");
                break;
            case ',':
                token.setType(",");
                token.setLexeme(",");
                break;
            case '.':
                switch (st.nextToken()) {
                    case StreamTokenizer.TT_NUMBER:
                        if (st.nval % 1 > 0) {
                            token.setType("ERROR");
                            token.setLexeme("Several Dots(.) In One Variable");

                            boolean temp = true;
                            while (temp) {
                                int y = st.nextToken();
                                switch (y) {
                                    case '.':
                                        break;
                                    case StreamTokenizer.TT_NUMBER:
                                        break;
                                    default:
                                        temp = false;
                                        st.pushBack();
                                        break;
                                }
                            }
                        } else {
                            int a = (int) st.nval;
                            token.setType("floatValue");
                            token.setLexeme(Double.parseDouble(0 + "." + a));
                        }
                        break;
                    default:
                        token.setType(".");
                        token.setLexeme(".");
                        st.pushBack();
                }
                break;
            case '+':
                token.setType("+");
                token.setLexeme("+");
                break;
            case '-':
                token.setType("-");
                token.setLexeme("-");
                break;
            case '*':
                if (st.nextToken() == '/') {
                    if (localComment) {
                        slashstarComment = false;
                    } else {
                        token.setType("*");
                        token.setLexeme("*");
                        st.pushBack();
                    }
                } else {
                    token.setType("*");
                    token.setLexeme("*");
                    st.pushBack();
                }
                break;
            case '/':
                switch (st.nextToken()) {
                    case '/':
                        if (!localComment) {
                            localComment = true;
                            slashslashComment = true;
                        }
                        break;
                    case '*':
                        if (!localComment) {
                            localComment = true;
                            slashstarComment = true;
                        }
                        break;
                    default:
                        token.setType("/");
                        token.setLexeme("/");
                        st.pushBack();
                }
                break;
            case '(':
                token.setType("(");
                token.setLexeme("(");
                break;
            case ')':
                token.setType(")");
                token.setLexeme(")");
                break;
            case '{':
                token.setType("{");
                token.setLexeme("{");
                break;
            case '}':
                token.setType("}");
                token.setLexeme("}");
                break;
            case '[':
                token.setType("[");
                token.setLexeme("[");
                break;
            case ']':
                token.setType("]");
                token.setLexeme("]");
                break;
            case StreamTokenizer.TT_WORD:
                switch (st.sval) {
                    case "if":
                        token.setType("if");
                        token.setLexeme("if");
                        break;
                    case "then":
                        token.setType("then");
                        token.setLexeme("then");
                        break;
                    case "else":
                        token.setType("else");
                        token.setLexeme("else");
                        break;
                    case "for":
                        token.setType("for");
                        token.setLexeme("for");
                        break;
                    case "class":
                        token.setType("class");
                        token.setLexeme("class");
                        break;
                    case "int":
                        token.setType("int");
                        token.setLexeme("int");
                        break;
                    case "float":
                        token.setType("float");
                        token.setLexeme("float");
                        break;
                    case "get":
                        token.setType("get");
                        token.setLexeme("get");
                        break;
                    case "put":
                        token.setType("put");
                        token.setLexeme("put");
                        break;
                    case "return":
                        token.setType("return");
                        token.setLexeme("return");
                        break;
                    case "program":
                        token.setType("program");
                        token.setLexeme("program");
                        break;
                    case "and":
                        token.setType("and");
                        token.setLexeme("and");
                        break;
                    case "not":
                        token.setType("not");
                        token.setLexeme("not");
                        break;
                    case "or":
                        token.setType("or");
                        token.setLexeme("or");
                        break;
                    default:
                        token.setType("id");
                        token.setLexeme(st.sval);
                        break;
                }
                break;
            case StreamTokenizer.TT_NUMBER:
                double temp = st.nval;
                if (st.nextToken() == StreamTokenizer.TT_WORD) {
                    token.setType("ERROR");
                    token.setLexeme("Alphanums Not Allowed To Terminate A Number");
                    break;
                }
                st.pushBack();
                if (temp % 1 > 0) {
                    float a = (float) st.nval;
                    token.setType("floatValue");
                    token.setLexeme(a);
                } else {
                    int a = (int) temp;
                    if (a == Integer.MAX_VALUE) {
                        token.setType("ERROR");
                        token.setLexeme("Possible Numeric Overflow Encountered");
                    } else {
                        token.setType("intValue");
                        token.setLexeme(a);
                    }
                }
                break;
            case '`':
                token.setType("ERROR");
                token.setLexeme("Unspecified Token: `");
                break;
            case '~':
                token.setType("ERROR");
                token.setLexeme("Unspecified Token: ~");
                break;
            case '!':
                token.setType("ERROR");
                token.setLexeme("Unspecified Token: !");
                break;
            case '@':
                token.setType("ERROR");
                token.setLexeme("Unspecified Token: @");
                break;
            case '$':
                token.setType("ERROR");
                token.setLexeme("Unspecified Token: $");
                break;
            case '%':
                token.setType("ERROR");
                token.setLexeme("Unspecified Token: %");
                break;
            case '^':
                token.setType("ERROR");
                token.setLexeme("Unspecified Token: ^");
                break;
            case '&':
                token.setType("ERROR");
                token.setLexeme("Unspecified Token: &");
                break;
            case '"':
                token.setType("ERROR");
                token.setLexeme("Unspecified Token: \"");
                break;
            case '\\':
                token.setType("ERROR");
                token.setLexeme("Unspecified Token: \\");
                break;
            case '\'':
                token.setType("ERROR");
                token.setLexeme("Unspecified Token: \'");
                break;
            case '#':
                token.setType("EOL");
                state = false;
                slashslashComment = false;
                break;
            case ' ':
                token.setType("EOL");
                break;
            case '\t':
                token.setType("EOL");
                break;

            default:
                token.setType("ERROR");
                token.setLexeme("Unidentified Token Found ");
        }

        if (localComment) {
            token.setType("CMT");
        }

        return token;
    }

    public static StreamTokenizer configTokenizer(StreamTokenizer st) {

        st.resetSyntax();
        st.wordChars('a', 'z');
        st.wordChars('A', 'Z');
        st.wordChars(128 + 32, 255);
        st.wordChars('_', '_');
        //st.whitespaceChars(0, ' ');
        st.parseNumbers();

        st.parseNumbers();
        st.ordinaryChar('-');
        st.ordinaryChar('/');
        st.ordinaryChar(';');
        st.ordinaryChar('+');
        st.ordinaryChar('(');
        st.ordinaryChar(')');
        st.ordinaryChar('.');
        st.ordinaryChar('>');
        st.ordinaryChar('<');
        st.ordinaryChar(',');
        st.ordinaryChar('*');
        st.ordinaryChar('/');
        st.ordinaryChar('{');
        st.ordinaryChar('}');
        st.ordinaryChar('[');
        st.ordinaryChar(']');
        st.ordinaryChar('#');
        st.ordinaryChar(9);

        //to display diff unwanted characters
        st.ordinaryChar('`');
        st.ordinaryChar('~');
        st.ordinaryChar('!');
        st.ordinaryChar('@');
        st.ordinaryChar('$');
        st.ordinaryChar('%');
        st.ordinaryChar('^');
        st.ordinaryChar('&');
        st.ordinaryChar('\\');
        st.ordinaryChar('"');
        st.ordinaryChar('\'');
        st.ordinaryChar(' ');

        return st;
    }

    public static void writeFiles(List<Token> tokenList, List<Token> errorList,
            File fileTokens, File fileErrors) throws IOException {

        FileWriter output = new FileWriter(fileTokens);
        for (Token token : tokenList) {
            output.write(token.writeToken() + System.getProperty("line.separator"));
        }
        output.close();

        output = new FileWriter(fileErrors);
        for (Token error : errorList) {
            output.write(error.writeToken() + System.getProperty("line.separator"));
        }
        output.close();
    }
}
