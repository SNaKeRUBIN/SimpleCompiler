
import Parser.SyntacticAnalyser;
import java.io.IOException;

/**
 * Class to run the compiler
 */
public class Driver {

    /**
     * The main function
     */
    public static void main(String[] args) throws IOException {

        SyntacticAnalyser.run(args);
    }
}
