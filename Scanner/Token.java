package Scanner;

import java.math.BigDecimal;

/**
 * @author SNaKeRUBIN
 */
public class Token {

    private String type;
    private String lexeme_S;
    private int line;
    private int column;
    private BigDecimal lexeme_num;

    public Token() {
        type = "";
        lexeme_S = null;
        lexeme_num = new BigDecimal(-1);
        line = -1;
        column = -1;

    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLexeme(String lexeme) {
        this.lexeme_S = lexeme;
    }

    public void setLexeme(int lexeme) {
        this.lexeme_num = new BigDecimal(lexeme);
    }

    public void setLexeme(double lexeme) {
        this.lexeme_num = new BigDecimal(Double.toString(lexeme));
    }

    public void setLexeme(float lexeme) {
        this.lexeme_num = new BigDecimal(Float.toString(lexeme));
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getType() {
        return this.type;
    }

    public String getLocation() {
        String temp = "Line : " + this.line + ", Column : " + this.column;
        return temp;
    }

    public String getLexeme() {
        if (type.equals("floatValue") || type.equals("intValue")) {
            return this.lexeme_num.toString();
        } else if (type.equals("id")) {
            return this.lexeme_S;
        } else {
            return this.type;
        }

    }

    public void printToken() {
        if (!type.equals("ERROR")) {

            if (type.equals("ID") || type.equals("ALPHNUM") || type.equals("LETTER") || type.equals("STRING")) {
                System.out.println("Token Type: " + type + ", LineNo: " + line + ", ColumnNo: " + column + ", Lexeme: " + lexeme_S);
            } else if (type.equals("INTEGER") || type.equals("DIGIT") || type.equals("NONZERO")) {
                System.out.println("Token Type: " + type + ", LineNo: " + line + ", ColumnNo: " + column + ", Lexeme: " + lexeme_num);
            } else if (type.equals("FLOAT") || type.equals("FRACTION")) {
                System.out.println("Token Type: " + type + ", LineNo: " + line + ", ColumnNo: " + column + ", Lexeme: " + lexeme_num);
            } else if (type.equals("EOL")) {
            } else {
                System.out.println("Token Type: " + type + ", LineNo: " + line + ", ColumnNo: " + column);
            }
        } else {
            System.out.println("Found ERROR - " + lexeme_S + ", LineNo: " + line + ", ColumnNo: " + column);
        }
    }

    public String writeToken() {
        if (!type.equals("ERROR")) {

            if (type.equals("id") || type.equals("ALPHNUM") || type.equals("LETTER") || type.equals("STRING")) {
                return ("Token Type: " + type + ", LineNo: " + line + ", ColumnNo: " + column + ", Lexeme: " + lexeme_S);
            } else if (type.equals("intValue") || type.equals("DIGIT") || type.equals("NONZERO")) {
                return ("Token Type: " + type + ", LineNo: " + line + ", ColumnNo: " + column + ", Lexeme: " + lexeme_num);
            } else if (type.equals("floatValue") || type.equals("FRACTION")) {
                return ("Token Type: " + type + ", LineNo: " + line + ", ColumnNo: " + column + ", Lexeme: " + lexeme_num);
            } else if (type.equals("EOL")) {
                return "EOL";
            } else {
                return ("Token Type: " + type + ", LineNo: " + line + ", ColumnNo: " + column);
            }
        } else {
            return ("Found ERROR - " + lexeme_S + ", LineNo: " + line + ", ColumnNo: " + column);
        }
    }
}
