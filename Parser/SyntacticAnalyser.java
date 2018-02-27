package Parser;

import Scanner.Token;
import Scanner.LexicalAnalyser;
import Semantics.SemanticFunctions;
import Semantics.SymbolTable;
import Semantics.SymbolTableRecord;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

/**
 * @author SNaKeRUBIN
 */
public class SyntacticAnalyser {

    public static void run(String[] args) throws FileNotFoundException, IOException {

        String input = "D:\\Compiler\\InputTest.txt";
        String inputGrammarAddress = "D:\\Compiler\\Grammar.txt";
        String outputTokens = "D:\\Compiler\\OutputTokens.txt";
        String outputErrorsScanner = "D:\\Compiler\\OutputErrorsScanner.txt";
        String outputErrorsParser = "D:\\Compiler\\OutputErrorsParser.txt";
        String outputDerivations = "D:\\Compiler\\OutputDerivations.txt";
        String outputErrorsSemantic = "D:\\Compiler\\OutputErrorsSemantic.txt";
        String outputSymbolTables = "D:\\Compiler\\OutputSymbolTables.txt";
        String outputCodegen = "D:\\Compiler\\CodeGen.m";

        LexicalAnalyser.setOutputFileLocations(input, outputTokens, outputErrorsScanner);

        File inputGrammar = new File(inputGrammarAddress);
        Scanner scanner = new Scanner(inputGrammar);

        FileWriter writerErrorsParser = new FileWriter(new File(outputErrorsParser));
        FileWriter writerDerivations = new FileWriter(new File(outputDerivations));
        FileWriter writerErrorsSemantic = new FileWriter(new File(outputErrorsSemantic));
        FileWriter writerCodeDec = new FileWriter(new File(outputCodegen));
        FileWriter writerCodeIns = new FileWriter(new File(outputCodegen), true);

        // READ THE FILE AND ADD PRODUCTIONS
        HashMap<Integer, ArrayList<String>> productionMap = new HashMap<>();
        HashMap<Integer, ArrayList<String>> productionMapAttributed = new HashMap<>();

        int counter = 0;    // there is a  static int counter in this class too
        while (scanner.hasNextLine()) {

            String tempString = scanner.nextLine();
            String[] tempStringArray = tempString.split("[ :]+");
            ArrayList<String> tempArrayList = new ArrayList<>();
            ArrayList<String> tempArrayList2 = new ArrayList<>();

            for (int i = 0; i < tempStringArray.length; i++) {
                if (!tempStringArray[i].startsWith("#")) {
                    tempArrayList.add(tempStringArray[i]);
                }
                tempArrayList2.add(tempStringArray[i]);
            }

            productionMap.put(counter, tempArrayList);
            productionMapAttributed.put(counter, tempArrayList2);
            counter++;
        }
        // END

        // CREATE TERMINALS AND NONTERMINALS SETS
        HashSet<String> terminals = new HashSet<>();
        HashSet<String> nonterminals = new HashSet<>();

        for (int i = 0; i < productionMap.size(); i++) {
            nonterminals.add(productionMap.get(i).get(0));
            for (int j = 1; j < productionMap.get(i).size(); j++) {
                String temp = productionMap.get(i).get(j);
                terminals.add(productionMap.get(i).get(j));
            }
        }
        terminals.removeAll(nonterminals);
        terminals.remove("EPSILON");
        terminals.add("$");
        // END

        // LINKING TERMINALS TO COLUMNS
        HashMap<String, Integer> colValues = new HashMap<>();
        String[] termArray = terminals.toArray(new String[terminals.size()]);
        for (int i = 0; i < termArray.length; i++) {
            colValues.put(termArray[i], i);
        }
        //END

        // LINKING NONTERMINALS TO ROWS
        HashMap<String, Integer> rowValues = new HashMap<>();
        String[] nontermArray = nonterminals.toArray(new String[nonterminals.size()]);
        for (int i = 0; i < nontermArray.length; i++) {
            rowValues.put(nontermArray[i], i);
        }
        // END

        // FIRST SETS 
        HashMap<String, ArrayList<String>> firstSet = new HashMap<>();
        HashMap<String, Boolean> done_sets_first = new HashMap<>();

        for (String s : nontermArray) {
            done_sets_first.put(s, Boolean.FALSE);
        }
        for (String s : termArray) {
            done_sets_first.put(s, Boolean.TRUE);
        }

        for (int i = 0; i < termArray.length; i++) {
            ArrayList<String> temp = new ArrayList<>();
            temp.add(termArray[i]);
            firstSet.put(termArray[i], temp);
        }
        firstSet.put("EPSILON", new ArrayList<String>() {
            {
                add("EPSILON");
            }
        });

        boolean notReady_first = true;
        while (notReady_first) {
            notReady_first = false;
            boolean doneNonTerminal = true;
            for (int i = 0; i < nontermArray.length; i++) {     // for each nonterminal
                doneNonTerminal = true;
                String curr = nontermArray[i];                  // curr gets the current nonterminal whose FIRST is to be found
                if (!firstSet.containsKey(curr)) {
                    firstSet.put(curr, new ArrayList<String>());
                }
                for (int j = 0; j < productionMap.size(); j++) {
                    if (productionMap.get(j).get(0).equals(curr)) {
                        String rhs = productionMap.get(j).get(1); // the current RHS char
                        if (terminals.contains(rhs) || rhs.equals("EPSILON")) {
                            ArrayList<String> temp = firstSet.get(curr);

                            boolean repeat = false;
                            for (int k = 0; k < temp.size(); k++) {
                                if (temp.get(k).equals(rhs)) {
                                    repeat = true;
                                    break;
                                }
                            }
                            if (repeat) {                   // prevents any change if same terminal is encountered
                                continue;
                            }
                            temp.add(rhs);
                            doneNonTerminal = false;        // not sure if this is right
                        }
                        if (nonterminals.contains(rhs)) {
                            doneNonTerminal = false;
                            ArrayList<String> atemp = productionMap.get(j);
                            int limit = 2;

                            for (int x = 1; x < atemp.size() && x < limit; x++) {
                                if (done_sets_first.get(atemp.get(x))) {
                                    doneNonTerminal = true;
                                    ArrayList<String> temp = firstSet.get(atemp.get(x));
                                    ArrayList<String> temp2 = firstSet.get(curr);

                                    for (int k = 0; k < temp.size(); k++) {
                                        if (temp2.contains(temp.get(k))) {
                                        } else {
                                            temp2.add(temp.get(k));
                                            doneNonTerminal = false;
                                        }
                                    }
                                    if (temp.contains("EPSILON")) {
                                        temp2.remove("EPSILON");
                                        limit++;
                                        doneNonTerminal = false;
                                    }
                                }
                                if (limit > atemp.size()) {
                                    doneNonTerminal = true;

                                    // only add if last nonterminal contains EPSILON
                                    ArrayList<String> temp2 = firstSet.get(curr);
                                    temp2.add("EPSILON");
                                }
                            }

                        }
                    }

                }
                if (doneNonTerminal) {
                    done_sets_first.remove(curr);
                    done_sets_first.put(curr, true);
                }
            }
            for (Object value : done_sets_first.values()) {
                boolean temp = (boolean) value;
                if (!temp) {
                    notReady_first = true;
                }
            }
        }
        // END

        // FOLLOW SETS 
        HashMap<String, ArrayList<String>> followSet = new HashMap<>();
        HashMap<String, Boolean> done_sets_follow = new HashMap<>();

        for (String s : nonterminals) {
            done_sets_follow.put(s, Boolean.FALSE);
        }

        Boolean notReady_follow = true;
        while (notReady_follow) {
            notReady_follow = false;
            boolean doneNonTerminal = true;
            for (int i = 0; i < nontermArray.length; i++) {         // for each nonterminal
                doneNonTerminal = true;
                String curr = nontermArray[i];                  // curr gets the current nonterminal whose FIRST is to be found

                if (!followSet.containsKey(curr)) {
                    followSet.put(curr, new ArrayList<String>());
                }
                for (int j = 0; j < productionMap.size(); j++) {
                    ArrayList<String> tempRule = productionMap.get(j);

                    if (j == 0) {
                        if (tempRule.get(0).equals(curr)) {
                            if (followSet.get(curr).contains("$")) {
                            } else {
                                ArrayList<String> temp_curr = followSet.get(curr);
                                temp_curr.add("$");
                                doneNonTerminal = false;
                            }
                        }
                    }
                    for (int k = 1; k < tempRule.size(); k++) {
                        if (tempRule.get(k).equals(curr)) {
                            if (k + 1 < tempRule.size() && terminals.contains(tempRule.get(k + 1))) {       // next is terminal
                                ArrayList<String> tempList = followSet.get(curr);

                                boolean repeat = false;
                                for (int l = 0; l < tempList.size(); l++) {
                                    if (tempList.get(l).equals(tempRule.get(k + 1))) {
                                        repeat = true;
                                        break;
                                    }
                                }
                                if (repeat) {                   // prevents any change if same terminal is encountered
                                    continue;
                                }
                                tempList.add(tempRule.get(k + 1));
                                doneNonTerminal = false;

                            } else if (k + 1 < tempRule.size() && nonterminals.contains(tempRule.get(k + 1))) {     // next is nonterminal
                                int limit = k + 2;

                                for (int l = k + 1; l < tempRule.size() && l < limit; l++) {
                                    // if (done_sets_follow.get(tempRule.get(l))) {                // i will need first set of following variables, why chk if they are done ??
                                    if (firstSet.get(tempRule.get(l)).contains("EPSILON")) {
                                        // add first set of l and if next is END of rule, add FOLLOW set of LHS else inc counter
                                        ArrayList<String> temp1 = firstSet.get(tempRule.get(l));
                                        ArrayList<String> temp_curr = followSet.get(curr);

                                        for (int m = 0; m < temp1.size(); m++) {
                                            if (temp_curr.contains(temp1.get(m)) || temp1.get(m).equals("EPSILON")) {
                                            } else {
                                                temp_curr.add(temp1.get(m));
                                                doneNonTerminal = false;
                                            }
                                        }
                                        if (l + 1 < tempRule.size()) {
                                            limit++;
                                        } else // add FOLLOW set of LHS
                                        if (done_sets_follow.get(tempRule.get(0))) {
                                            ArrayList<String> temp2 = followSet.get(tempRule.get(0));

                                            for (int m = 0; m < temp2.size(); m++) {
                                                if (temp_curr.contains(temp2.get(m))) {
                                                } else {
                                                    temp_curr.add(temp2.get(m));
                                                    doneNonTerminal = false;
                                                }
                                            }

                                        } else {
                                            doneNonTerminal = false;
                                        }
                                    } else {
                                        // only add first set of next
                                        ArrayList<String> temp1 = firstSet.get(tempRule.get(l));
                                        ArrayList<String> temp_curr = followSet.get(curr);

                                        for (int m = 0; m < temp1.size(); m++) {
                                            if (temp_curr.contains(temp1.get(m)) || temp1.get(m).equals("EPSILON")) {
                                            } else {
                                                temp_curr.add(temp1.get(m));
                                                doneNonTerminal = false;
                                            }
                                        }
                                    }
                                }
                            } else // at end of RHS
                            // add FOLLOW set of LHS
                            if (done_sets_follow.get(tempRule.get(0))) {
                                ArrayList<String> temp2 = followSet.get(tempRule.get(0));
                                ArrayList<String> temp_curr = followSet.get(curr);

                                for (int m = 0; m < temp2.size(); m++) {
                                    if (temp_curr.contains(temp2.get(m))) {
                                    } else {
                                        temp_curr.add(temp2.get(m));
                                        doneNonTerminal = false;
                                    }
                                }
                            } else {
                            }
                        }
                    }
                }
                if (doneNonTerminal) {
                    done_sets_follow.remove(curr);
                    done_sets_follow.put(curr, true);
                }
            }
            for (Object value : done_sets_follow.values()) {
                boolean temp = (boolean) value;
                if (!temp) {
                    notReady_follow = true;
                }
            }
        }
        // END

        // PARSE TABLE DECLARATION
        int[][] parseTable = new int[rowValues.size()][colValues.size()];
        for (int i = 0; i < rowValues.size(); i++) {
            for (int j = 0; j < colValues.size(); j++) {
                parseTable[i][j] = 0;
            }
        }
        // END

        // FILL THE PARSE TABLE 
        for (int i = 0; i < productionMap.size(); i++) {
            int row = -1, column = -1;
            int matchPresentation = i + 1;
            ArrayList<String> temp = productionMap.get(i);
            String[] tempArry = temp.toArray(new String[temp.size()]);

            row = rowValues.get(tempArry[0]);
            int tempLimit = 2;
            for (int j = 1; j < tempArry.length && j < tempLimit; j++) {
                if (firstSet.get(tempArry[j]).contains("EPSILON")) {
                    tempLimit++;
                }
                for (int k = 0; k < termArray.length; k++) {
                    if (firstSet.get(tempArry[j]).contains(termArray[k])) {
                        column = colValues.get(termArray[k]);
                        parseTable[row][column] = matchPresentation;
                    }
                }
            }

            for (int j = 0; j < termArray.length; j++) {
                if (firstSet.get(tempArry[1]).contains(termArray[j])) {
                    column = colValues.get(termArray[j]);
                    parseTable[row][column] = matchPresentation;
                }

            }

            if (tempLimit > tempArry.length) {
                tempLimit--;
            }
            if (firstSet.get(tempArry[1]).contains("EPSILON")) {
                for (int j = 0; j < termArray.length; j++) {
                    if (followSet.get(tempArry[0]).contains(termArray[j])) {
                        column = colValues.get(termArray[j]);
                        parseTable[row][column] = matchPresentation;
                    }
                }
            }
        }
        // END

        // INITIALIZE INPUT ARRAY
        ArrayList<Token> inputArrayList = LexicalAnalyser.getTokens();
        Token[] inputTokensArray = inputArrayList.toArray(new Token[inputArrayList.size()]);
        // END

        // PARSING ALGORITHM
        //  PASS - I
        Stack stack = new Stack();
        ArrayList<String> derivation = new ArrayList<>();

        Stack diffClassNames = new Stack();

        Stack stackSemantic = new Stack();
        int scopelvl = 0;
        SymbolTable currTable = null;
        SymbolTable globalTable = null;
        SemanticFunctions function = new SemanticFunctions(diffClassNames, outputSymbolTables, outputErrorsSemantic);

        stack.push("$");
        stack.push("prog");
        derivation.add("prog");

        writerDerivations.write("=> " + derivation.get(0) + System.getProperty("line.separator"));
        writerDerivations.write(System.getProperty("line.separator"));
        writerDerivations.flush();

        int counterParsing = 0;
        String a = inputTokensArray[counterParsing].getType();
        String x;
        boolean error = false;

        while (!stack.peek().equals("$")) {
            x = stack.peek().toString();
            if (terminals.contains(x)) {
                if (x.equals(a)) {
                    stack.pop();
                    counterParsing++;
                    a = inputTokensArray[counterParsing].getType();

                } else {
                    // SKIP ERROR
                    writerErrorsParser.write("Terminal on top of stack " + "\"" + x + "\"" + " does not match the input " + "\"" + a + "\"" + " at " + inputTokensArray[counterParsing].getLocation() + System.getProperty("line.separator"));
                    writerErrorsParser.flush();

                    stack.pop();
                    error = true;
                    // ERROR END
                }
            } else if (x.equals("EPSILON")) {
                stack.pop();
            } else if (x.startsWith("#")) {
                //  SEMANTIC ACTIONS
                String value = inputTokensArray[counterParsing].getLexeme();
                SymbolTable temp;
                SymbolTableRecord record;
                switch (x) {
                    //  create entry into table, add pointer to next table, increase scope and update currTable pointer
                    case "#GlobalTable":
                        globalTable = function.createGlobalTable();
                        currTable = globalTable;
                        scopelvl = 1;
                        break;
                    case "#ClassTable":
                        System.out.println(stackSemantic);
                        record = function.insert(currTable, stackSemantic.pop().toString(), "class");
                        currTable.list.add(record);
                        scopelvl++;
                        temp = currTable;
                        currTable = record.getLink();
                        break;
                    case "#ProgramTable":
                        record = function.insert(currTable, "", "program");
                        currTable.list.add(record);
                        scopelvl++;
                        temp = currTable;
                        currTable = record.getLink();
                        break;
                    case "#FunctionTable":
                        String sF = getString(stackSemantic);
                        System.out.println(stackSemantic);
                        record = function.insert(currTable, sF, "function");
                        currTable.list.add(record);
                        scopelvl++;
                        temp = currTable;
                        currTable = record.getLink();
                        break;
                    case "#ForTable":
                        break;
                    case "#VariableEntry":
                        // CREATE VARIABLE ENTRY AFTER CHKING IF SAME NAME VARIABLE ALREADY EXISTS

                        String sV = getString(stackSemantic);

                        if (function.alreadyExist(currTable, sV)) {
                            writerErrorsSemantic.write("Variable name already used" + System.getProperty("line.separator"));
                            writerErrorsSemantic.flush();
                            System.out.println(stackSemantic);
                        } else {
                            System.out.println(stackSemantic);
                            record = function.insert(currTable, sV, "variable");
                            currTable.list.add(record);
                        }
                        break;
                    case "#Scope":
                        scopelvl--;
                        temp = currTable;
                        currTable = temp.getParentTable();
                        break;
                    case "#Push":
                        stackSemantic.push(value);
                        break;
                    case "#ChkScope":
                        System.out.println(stackSemantic);
                        String sCS = getString(stackSemantic);
                        function.varDeclare(currTable, sCS);
                        break;

                    case "#InsChar":
                        stackSemantic.push("@");
                        break;

                    // PASS - II 
                    case "#FuncDefChk":
                        break;
                    case "#VarTypeChk":
                        break;

                    case "#StopHerePut":
                        sCS = getString(stackSemantic);
                        System.out.println("");
                        break;
                    case "#StopHereGet":
                        sCS = getString(stackSemantic);
                        System.out.println("");
                        break;
                    default:
                }
                stack.pop();
                // SEMANTIC ACTIONS END

            } else if (parseTable[rowValues.get(x)][colValues.get(a)] != 0) {
                stack.pop();
                // INVERSE RHS MULTIPLE PUSH (TT[x,a])
                ArrayList<String> tempList = productionMapAttributed.get(parseTable[rowValues.get(x)][colValues.get(a)] - 1);
                ArrayList<String> tempListPrint = productionMap.get(parseTable[rowValues.get(x)][colValues.get(a)] - 1);

                // PRINTING THE RULE USED
                writerDerivations.write("USING RULE : " + tempListPrint.get(0) + " -> ");
                for (int i = 1; i < tempListPrint.size(); i++) {
                    writerDerivations.write(tempListPrint.get(i) + " ");
                }
                writerDerivations.write(" INPUT : " + a);
                writerDerivations.write(System.getProperty("line.separator"));
                writerDerivations.flush();
                // END

                for (int i = tempList.size() - 1; i > 0; i--) {
                    stack.push(tempList.get(i));
                }

                // DERIVATIONS
                // REMOVE LHS FROM ARRAYLIST AND ADD THE DERIVATIONS AT ITS LOCATION
                int index = derivation.indexOf(tempListPrint.get(0));
                derivation.remove(index);
                for (int i = 1; i < tempListPrint.size(); i++) {
                    if (tempListPrint.get(i).equals("EPSILON")) {
                    } else {
                        derivation.add(index, tempListPrint.get(i));
                    }
                    index++;
                }
                writerDerivations.write("=> ");

                for (String temp : derivation) {
                    writerDerivations.write(temp + " ");
                }
                writerDerivations.write(System.getProperty("line.separator"));
                writerDerivations.write(System.getProperty("line.separator"));
                writerDerivations.flush();
                // DERIVATION TILL HERE

            } else {
                // SKIP ERROR
                if (a.equals("$")) {
                    writerErrorsParser.write("End of Input reached with nonterminal " + "\"" + x + "\"" + " on top of stack" + System.getProperty("line.separator"));
                    writerErrorsParser.flush();
                } else {
                    writerErrorsParser.write("Input " + "\"" + a + "\"" + " at " + inputTokensArray[counterParsing].getLocation()
                            + " cannot be derived by current top of stack " + "\"" + x + "\"" + System.getProperty("line.separator"));
                    writerErrorsParser.flush();
                }

                if (inputTokensArray[counterParsing].getType().equals("$") || followSet.get(x).contains(inputTokensArray[counterParsing].getType())) {
                    stack.pop();
                } else {
                    counterParsing++;
                    a = inputTokensArray[counterParsing].getType();
                }
                error = true;
                //ERROR END
            }
        }

        writerDerivations.close();
        writerErrorsParser.close();

        if ((!a.equals("$")) || (error)) {
            System.out.println("Parsing failed");
        } else {
            System.out.println("Parsing successful");
        }

        //  CODE TO PRINT ALL TABLES HERE
        function.print(globalTable);

        // ADD CODE HERE TO CHK FOR CIRCULAR CLASS DEPENDENCY
        function.classCirDep(globalTable);

        // TYPE SIZE TABLE
        HashMap<String, Integer> typeSizeTable = new HashMap();
        typeSizeTable.put("int", 4);
        typeSizeTable.put("float", 8);
        calcClassSize(globalTable, typeSizeTable, writerErrorsSemantic);

        boolean flag_afterprogram = false;
        boolean forloopVar = false;
        boolean logicalTemp = false;
        int counterVarUsed = 0;
        int counterIfCondition = 0;
        int counterForLoop = 0;
        writerCodeIns.write("       entry" + System.getProperty("line.separator"));

        // ADD CODE FOR PASS-II HERE
        stack = new Stack();

        stackSemantic = new Stack();

        scopelvl = 0;
        currTable = null;

        stack.push("$");
        stack.push("prog");

        counterParsing = 0;
        a = inputTokensArray[counterParsing].getType();
        x = null;
        error = false;

        String stacktop;

        while (!stack.peek().equals("$")) {
            x = stack.peek().toString();
            if (terminals.contains(x)) {
                if (x.equals(a)) {
                    stack.pop();
                    counterParsing++;
                    a = inputTokensArray[counterParsing].getType();

                } else {
                    // SKIP ERROR

                    stack.pop();
                    error = true;
                    // ERROR END
                }
            } else if (x.equals("EPSILON")) {
                stack.pop();
            } else if (x.startsWith("#")) {
                //  SEMANTIC ACTIONS
                String value = inputTokensArray[counterParsing].getLexeme();
                SymbolTable temp;
                SymbolTableRecord record;
                switch (x) {
                    //  create entry into table, add pointer to next table, increase scope and update currTable pointer
                    case "#GlobalTable":
                        currTable = globalTable;
                        scopelvl = 1;
                        break;
                    case "#ClassTable":
                        record = function.search(currTable, stackSemantic.pop().toString());
                        scopelvl++;
                        currTable = record.getLink();
                        break;
                    case "#ProgramTable":
                        record = function.search(currTable, "program");
                        scopelvl++;
                        currTable = record.getLink();
                        break;
                    case "#FunctionTable":
                        String sF = getString(stackSemantic);
                        String[] x1 = sF.split("\\(");
                        String[] z1 = x1[0].split(" ");
                        String nameFunction = z1[1];
                        record = function.search(currTable, nameFunction);
                        scopelvl++;
                        currTable = record.getLink();
                        writerCodeIns.write(System.getProperty("line.separator"));
                        writerCodeIns.write(nameFunction + "       nop" + System.getProperty("line.separator"));
                        break;
                    case "#ForLoopVar":
                        forloopVar = true;
                        break;
                    case "#VariableEntry":
                        // get var type, calc size, get name
                        String sV = getString(stackSemantic);
                        stacktop = sV;
                        String[] stringVarDeclare = getDeclare(stacktop);
                        int typeSize = typeSizeTable.get(stringVarDeclare[1]);
                        int arraySize = 1;
                        for (int i = 2; i < stringVarDeclare.length; i++) {
                            arraySize = arraySize * (Integer.parseInt(stringVarDeclare[i]));
                        }
                        String currScope = currTable.name;
                        String name = "";
                        if (currScope.contains("program")) {
                            flag_afterprogram = true;
                        }
                        if (!currScope.equals("program")) {
                            name = currScope + "_";
                        }
                        name = name.concat(stringVarDeclare[0]);
                        name = name.replaceFirst("Global : ", "");
                        name = name.replaceFirst(" : ", "_");
                        if (flag_afterprogram) {
                            writerCodeDec.write(name + "    " + "res" + "   " + arraySize * typeSize + System.getProperty("line.separator"));
                        }
                        if (forloopVar) {
                            stackSemantic.push("@");
                            stackSemantic.push(name);
                            forloopVar = false;
                        }
                        break;
                    case "#Scope":
                        scopelvl--;
                        temp = currTable;
                        currTable = temp.getParentTable();
                        break;
                    case "#Push":
                        stackSemantic.push(value);
                        break;
                    case "#ChkScope":
                        String sCS = getString(stackSemantic);
                        System.out.print("");
                        break;
                    case "#InsChar":
                        stackSemantic.push("@");
                        break;
                    case "#StopHereAdd":
                        String var2 = stackSemantic.pop().toString();
                        String op1 = stackSemantic.pop().toString();
                        if (op1.equals("@")) {
                            op1 = stackSemantic.pop().toString();
                        }
                        String var1 = stackSemantic.pop().toString();
                        int a1 = 0;
                        int a2 = 0;
                        int offset = 0;
                        writerCodeIns.write(System.getProperty("line.separator"));
                        try {
                            a1 = Integer.parseInt(var1);
                            writerCodeIns.write("       addi   r1,r0," + a1 + System.getProperty("line.separator"));
                        } catch (Exception e) {
                            writerCodeIns.write("       addi   r3,r0," + offset + System.getProperty("line.separator"));
                            writerCodeIns.write("       lw     r1," + var1 + "(" + "r3" + ")" + System.getProperty("line.separator"));
                        }
                        try {
                            a2 = Integer.parseInt(var2);
                            writerCodeIns.write("       addi   r2,r0," + a2 + System.getProperty("line.separator"));
                        } catch (Exception e) {
                            writerCodeIns.write("       addi   r3,r0," + offset + System.getProperty("line.separator"));
                            writerCodeIns.write("       lw     r2," + var2 + "(" + "r3" + ")" + System.getProperty("line.separator"));
                        }
                        int r1 = 0;
                        switch (op1) {
                            case "+":
                                writerCodeIns.write("       add    r1,r1,r2" + System.getProperty("line.separator"));
                                writerCodeIns.write("       sw     " + "var" + counterVarUsed + "(r0),r1" + System.getProperty("line.separator"));
                                stackSemantic.push("var" + counterVarUsed);
                                counterVarUsed++;
                                System.out.println("add " + var1 + " + " + var2);
                                break;
                            case "-":
                                writerCodeIns.write("       sub    r1,r1,r2" + System.getProperty("line.separator"));
                                writerCodeIns.write("       sw     " + "var" + counterVarUsed + "(r0),r1" + System.getProperty("line.separator"));
                                stackSemantic.push("var" + counterVarUsed);
                                counterVarUsed++;
                                System.out.println("sub " + var1 + " - " + var2);
                                break;
                            case "or":
                                if (!logicalTemp) {
                                    writerCodeDec.write("logtemp    res     4" + System.getProperty("line.separator"));
                                    logicalTemp = true;
                                }
                                writerCodeIns.write("       or     r3,r2,r1" + System.getProperty("line.separator"));
                                writerCodeIns.write("       bz     r3,zero" + counterVarUsed + System.getProperty("line.separator"));
                                writerCodeIns.write("       addi    r1,r0,1" + System.getProperty("line.separator"));
                                writerCodeIns.write("       sw     logtemp(r0),r1" + System.getProperty("line.separator"));
                                writerCodeIns.write("       j     endor" + counterVarUsed + System.getProperty("line.separator"));
                                writerCodeIns.write("zero" + counterVarUsed + "     sw     logtemp(r0),r0" + System.getProperty("line.separator"));
                                writerCodeIns.write("endor" + counterVarUsed + "        nop" + System.getProperty("line.separator"));
                                counterVarUsed++;
                                stackSemantic.push("logtemp");
                                System.out.println("or " + var1 + " OR " + var2);
                                break;
                        }
                        break;
                    case "#StopHereMul":
                        var2 = stackSemantic.pop().toString();
                        op1 = stackSemantic.pop().toString();
                        if (op1.equals("@")) {
                            op1 = stackSemantic.pop().toString();
                        }
                        var1 = stackSemantic.pop().toString();
                        a1 = 0;
                        a2 = 0;
                        offset = 0;
                        writerCodeIns.write(System.getProperty("line.separator"));
                        try {
                            a1 = Integer.parseInt(var1);
                            writerCodeIns.write("       addi   r1,r0," + a1 + System.getProperty("line.separator"));
                        } catch (Exception e) {
                            writerCodeIns.write("       addi   r3,r0," + offset + System.getProperty("line.separator"));
                            writerCodeIns.write("       lw     r1," + var1 + "(" + "r3" + ")" + System.getProperty("line.separator"));
                        }
                        try {
                            a2 = Integer.parseInt(var2);
                            writerCodeIns.write("       addi   r2,r0," + a2 + System.getProperty("line.separator"));
                        } catch (Exception e) {
                            writerCodeIns.write("       addi   r3,r0," + offset + System.getProperty("line.separator"));
                            writerCodeIns.write("       lw     r2," + var1 + "(" + "r3" + ")" + System.getProperty("line.separator"));
                        }
                        r1 = 0;
                        switch (op1) {
                            case "*":
                                //r1 = a1 * a2;
                                writerCodeIns.write("       mul    r1,r1,r2" + System.getProperty("line.separator"));
                                writerCodeIns.write("       sw     " + "var" + counterVarUsed + "(r0),r1" + System.getProperty("line.separator"));
                                stackSemantic.push("var" + counterVarUsed);
                                counterVarUsed++;
                                System.out.println("add " + var1 + " * " + var2);
                                break;
                            case "/":
                                //r1 = a1 / a2;
                                writerCodeIns.write("       div    r1,r1,r2" + System.getProperty("line.separator"));
                                writerCodeIns.write("       sw     " + "var" + counterVarUsed + "(r0),r1" + System.getProperty("line.separator"));
                                stackSemantic.push("var" + counterVarUsed);
                                counterVarUsed++;
                                System.out.println("add " + var1 + " / " + var2);
                                break;
                            case "and":
                                //r1 = a1 & a2;
                                if (!logicalTemp) {
                                    writerCodeDec.write("logtemp    res     4" + System.getProperty("line.separator"));
                                    logicalTemp = true;
                                }
                                writerCodeIns.write("       and     r3,r2,r1" + System.getProperty("line.separator"));
                                writerCodeIns.write("       bz     r3,zero" + counterVarUsed + System.getProperty("line.separator"));
                                writerCodeIns.write("       addi    r1,r0,1" + System.getProperty("line.separator"));
                                writerCodeIns.write("       sw     logtemp(r0),r1" + System.getProperty("line.separator"));
                                writerCodeIns.write("       j     endand" + counterVarUsed + System.getProperty("line.separator"));
                                writerCodeIns.write("zero" + counterVarUsed + "     sw     logtemp(r0),r0" + System.getProperty("line.separator"));
                                writerCodeIns.write("endand" + counterVarUsed + "       nop" + System.getProperty("line.separator"));
                                counterVarUsed++;
                                stackSemantic.push("logtemp");
                                System.out.println("and " + var1 + " AND " + var2);
                                break;
                        }
                        break;
                    case "#StopHereAss":
                        var2 = stackSemantic.pop().toString();
                        op1 = stackSemantic.pop().toString();
                        if (op1.equals("@")) {
                            op1 = stackSemantic.pop().toString();
                        }
                        var1 = stackSemantic.pop().toString();
                        offset = 0;
                        writerCodeIns.write(System.getProperty("line.separator"));
                        try {
                            a2 = Integer.parseInt(var2);
                            writerCodeIns.write("       addi   r1,r0," + a2 + System.getProperty("line.separator"));
                        } catch (Exception e) {
                            if (var2.equals("r1")) {
                            } else {
                                writerCodeIns.write("       addi   r3,r0," + offset + System.getProperty("line.separator"));
                                writerCodeIns.write("       lw     r1," + var2 + "(" + "r3" + ")" + System.getProperty("line.separator"));
                            }
                        }
                        writerCodeIns.write("       addi   r3,r0," + offset + System.getProperty("line.separator"));
                        writerCodeIns.write("       sw     " + var1 + "(r3),r1" + System.getProperty("line.separator"));

                        System.out.println("assign " + var1 + "=" + var2);
                        break;
                    case "#StopHereFunction":
                        sCS = getFunctionName(stackSemantic);
                        writerCodeIns.write("       jl        r15," + sCS + System.getProperty("line.separator"));
                        System.out.print("");
                        break;
                    case "#StopHereRel":
                        var2 = stackSemantic.pop().toString();
                        op1 = stackSemantic.pop().toString();
                        if (op1.equals("@")) {
                            op1 = stackSemantic.pop().toString();
                        }
                        var1 = stackSemantic.pop().toString();
                        offset = 0;
                        writerCodeIns.write(System.getProperty("line.separator"));
                        try {
                            a1 = Integer.parseInt(var1);
                            writerCodeIns.write("       addi   r1,r0," + a1 + System.getProperty("line.separator"));
                        } catch (Exception e) {
                            writerCodeIns.write("       addi   r3,r0," + offset + System.getProperty("line.separator"));
                            writerCodeIns.write("       lw     r1," + var1 + "(" + "r3" + ")" + System.getProperty("line.separator"));
                        }
                        try {
                            a2 = Integer.parseInt(var2);
                            writerCodeIns.write("       addi   r2,r0," + a2 + System.getProperty("line.separator"));
                        } catch (Exception e) {
                            writerCodeIns.write("       addi   r3,r0," + offset + System.getProperty("line.separator"));
                            writerCodeIns.write("       lw     r2," + var2 + "(" + "r3" + ")" + System.getProperty("line.separator"));
                        }
                        r1 = 0;
                        switch (op1) {
                            case "<":
                                writerCodeIns.write("       clt   r1,r1,r2" + System.getProperty("line.separator"));
                                writerCodeIns.write("       sw     " + "var" + counterVarUsed + "(r0),r1" + System.getProperty("line.separator"));
                                stackSemantic.push("var" + counterVarUsed);
                                counterVarUsed++;
                                System.out.println("lt " + var1 + " < " + var2);
                                break;
                            case "<=":
                                writerCodeIns.write("       cle   r1,r1,r2" + System.getProperty("line.separator"));
                                writerCodeIns.write("       sw     " + "var" + counterVarUsed + "(r0),r1" + System.getProperty("line.separator"));
                                stackSemantic.push("var" + counterVarUsed);
                                counterVarUsed++;
                                System.out.println("lte " + var1 + " <= " + var2);
                                break;
                            case "<>":
                                writerCodeIns.write("       cne   r1,r1,r2" + System.getProperty("line.separator"));
                                writerCodeIns.write("       sw     " + "var" + counterVarUsed + "(r0),r1" + System.getProperty("line.separator"));
                                stackSemantic.push("var" + counterVarUsed);
                                counterVarUsed++;
                                System.out.println("neq " + var1 + " <> " + var2);
                                break;
                            case ">":
                                writerCodeIns.write("       cgt   r1,r1,r2" + System.getProperty("line.separator"));
                                writerCodeIns.write("       sw     " + "var" + counterVarUsed + "(r0),r1" + System.getProperty("line.separator"));
                                stackSemantic.push("var" + counterVarUsed);
                                counterVarUsed++;
                                System.out.println("gt " + var1 + " > " + var2);
                                break;
                            case ">=":
                                writerCodeIns.write("       cge   r1,r1,r2" + System.getProperty("line.separator"));
                                writerCodeIns.write("       sw     " + "var" + counterVarUsed + "(r0),r1" + System.getProperty("line.separator"));
                                stackSemantic.push("var" + counterVarUsed);
                                counterVarUsed++;
                                System.out.println("gte " + var1 + " >= " + var2);
                                break;
                            case "==":
                                writerCodeIns.write("       ceq   r1,r1,r2" + System.getProperty("line.separator"));
                                writerCodeIns.write("       sw     " + "var" + counterVarUsed + "(r0),r1" + System.getProperty("line.separator"));
                                stackSemantic.push("var" + counterVarUsed);
                                counterVarUsed++;
                                System.out.println("eq " + var1 + " == " + var2);
                                break;
                        }
                        break;
                    case "#StopHereNot":
                        var1 = stackSemantic.pop().toString();
                        op1 = stackSemantic.pop().toString();
                        if (op1.equals("@")) {
                            op1 = stackSemantic.pop().toString();
                        }
                        offset = 0;
                        if (!logicalTemp) {
                            writerCodeDec.write("logtemp    res     4" + System.getProperty("line.separator"));
                            logicalTemp = true;
                        }
                        try {
                            a1 = Integer.parseInt(var1);
                            writerCodeIns.write("       addi   r1,r0," + a1 + System.getProperty("line.separator"));
                        } catch (Exception e) {
                            writerCodeIns.write("       addi   r3,r0," + offset + System.getProperty("line.separator"));
                            writerCodeIns.write("       lw     r1," + var1 + "(" + "r3" + ")" + System.getProperty("line.separator"));
                        }
                        writerCodeIns.write("       not     r2,r1" + System.getProperty("line.separator"));
                        writerCodeIns.write("       bz     r2,zero" + counterVarUsed + System.getProperty("line.separator"));
                        writerCodeIns.write("       addi    r1,r0,0" + System.getProperty("line.separator"));
                        writerCodeIns.write("       sw     logtemp(r0),r1" + System.getProperty("line.separator"));
                        writerCodeIns.write("       j     endnot" + counterVarUsed + System.getProperty("line.separator"));
                        writerCodeIns.write("zero" + counterVarUsed + "     sw     logtemp(r0),r0" + System.getProperty("line.separator"));
                        writerCodeIns.write("endnot" + counterVarUsed + "     nop" + System.getProperty("line.separator"));
                        counterVarUsed++;
                        stackSemantic.push("logtemp");
                        System.out.println("not " + " NOT " + var1);
                        break;
                    case "#StopHereIfThen":
                        writerCodeIns.write("       bz     r1,else" + counterIfCondition + System.getProperty("line.separator"));
                        System.out.println("then");
                        break;
                    case "#StopHereIfElse":
                        writerCodeIns.write("       j       endif" + counterIfCondition + System.getProperty("line.separator"));
                        writerCodeIns.write("else" + counterIfCondition + "      nop" + System.getProperty("line.separator"));
                        System.out.println("else");
                        break;
                    case "#StopHereIfEnd":
                        writerCodeIns.write("endif" + counterIfCondition + "      nop" + System.getProperty("line.separator"));
                        counterVarUsed++;
                        System.out.println("end");
                        break;
                    case "#StopHereForCondition":
                        writerCodeIns.write("loop" + counterForLoop + "     nop" + System.getProperty("line.separator"));
                        System.out.println("condition");
                        break;
                    case "#StopHereForIterCode":
                        writerCodeIns.write("       bz   r1,    outofloop" + counterForLoop + System.getProperty("line.separator"));
                        writerCodeIns.write("       j   innercode" + counterForLoop + System.getProperty("line.separator"));
                        writerCodeIns.write("itercode" + counterForLoop + "     nop" + System.getProperty("line.separator"));
                        System.out.println("Itercode");
                        break;
                    case "#StopHereForCode":
                        writerCodeIns.write("       j   loop" + counterForLoop + System.getProperty("line.separator"));
                        writerCodeIns.write("innercode" + counterForLoop + "     nop" + System.getProperty("line.separator"));
                        System.out.println("code");
                        break;
                    case "#StopHereForEnd":
                        writerCodeIns.write("       j   itercode" + counterForLoop + System.getProperty("line.separator"));
                        writerCodeIns.write("outofloop" + counterForLoop + "     nop" + System.getProperty("line.separator"));
                        counterForLoop++;
                        System.out.println("end");
                        break;
                    case "#StopHerePut":
                        var1 = stackSemantic.pop().toString();
                        offset = 0;
                        writerCodeIns.write(System.getProperty("line.separator"));
                        try {
                            a1 = Integer.parseInt(var1);
                            writerCodeIns.write("       addi   r1,r0," + a1 + System.getProperty("line.separator"));
                        } catch (Exception e) {
                            writerCodeIns.write("       addi   r3,r0," + offset + System.getProperty("line.separator"));
                            writerCodeIns.write("       lw     r1," + var1 + "(" + "r3" + ")" + System.getProperty("line.separator"));
                        }
                        writerCodeIns.write("       jl      r15,putint" + System.getProperty("line.separator"));
                        writerCodeIns.write("       jl      r15,putline" + System.getProperty("line.separator"));
                        System.out.println("Put " + var1);
                        break;
                    case "#StopHereGet":
                        var1 = stackSemantic.pop().toString();
                        offset = 0;
                        writerCodeIns.write(System.getProperty("line.separator"));
                        writerCodeIns.write("       jl      r15,    getint" + System.getProperty("line.separator"));
                        try {
                            a1 = Integer.parseInt(var1);
                        } catch (Exception e) {
                            writerCodeIns.write("       addi   r3,r0," + offset + System.getProperty("line.separator"));
                            writerCodeIns.write("       sw     " + var1 + "(" + "r3" + "), r1" + System.getProperty("line.separator"));
                        }
                        System.out.println("Get " + var1);
                        break;
                    case "#StopHereReturn":
                        var1 = stackSemantic.pop().toString();
                        writerCodeIns.write("       addi   r3,r0,0" + System.getProperty("line.separator"));
                        writerCodeIns.write("       lw      r1," + var1 + "(" + "r3" + ")" + System.getProperty("line.separator"));
                        System.out.println("Return " + var1);
                        break;
                    case "#StopHereProgramEnd":
                        writerCodeIns.write("       hlt" + System.getProperty("line.separator"));
                        break;
                    case "#StopHereFunctionEnd":
                        writerCodeIns.write("       jr      r15" + System.getProperty("line.separator"));
                        break;

                    // PASS - II , NEED TO MOVED LATER
                    case "#FuncDefChk":
                        sCS = getStringSafe(stackSemantic);
                        function.funcDefined(currTable, sCS);
                        break;
                    case "#VarTypeChk": // properly defined type
                        sCS = getStringSafe(stackSemantic);
                        SymbolTableRecord ptr = function.varProperlyDefined(currTable, sCS);
                        if (ptr == null) {
                            writerErrorsSemantic.write("Variable type not defined for: " + stackSemantic.peek().toString() + System.getProperty("line.separator"));
                            writerErrorsSemantic.flush();
                        }
                        break;
                    default:
                }
                stack.pop();
                // SEMANTIC ACTIONS END

            } else if (parseTable[rowValues.get(x)][colValues.get(a)] != 0) {
                stack.pop();
                // INVERSE RHS MULTIPLE PUSH (TT[x,a])
                ArrayList<String> tempList = productionMapAttributed.get(parseTable[rowValues.get(x)][colValues.get(a)] - 1);

                for (int i = tempList.size() - 1; i > 0; i--) {
                    stack.push(tempList.get(i));
                }

            } else {
                // SKIP ERROR
                if (inputTokensArray[counterParsing].getType().equals("$") || followSet.get(x).contains(inputTokensArray[counterParsing].getType())) {
                    stack.pop();
                } else {
                    counterParsing++;
                    a = inputTokensArray[counterParsing].getType();
                }
                error = true;
                //ERROR END
            }
        }
        // END OF PARSE II

        writeVariablesUsed(writerCodeDec, counterVarUsed);
        writerCodeDec.close();
        writerCodeIns.flush();
        writeStaticCode(writerCodeIns);
        System.out.println("");
    }

    // UNUSED FUNCTION
    // PURPOSE MOVED TO calcClassSize() FUNCTION
    static void buildTypeSize(SymbolTable globaltable, Hashtable table) {
        for (int i = 0; i < globaltable.list.size(); i++) {
            if (globaltable.list.get(i).kind.equals("class")) {
                table.put(globaltable.list.get(i).name, globaltable.list.get(i).size);
            }
        }

    }

    static void calcClassSize(SymbolTable globaltable, HashMap<String, Integer> table, FileWriter writer) throws IOException {
        boolean done = true;
        for (int i = 0; i < globaltable.list.size(); i++) {
            if (globaltable.list.get(i).kind.equals("class")) {
                done = true;
                SymbolTable innerTable = globaltable.list.get(i).getLink();
                int size = 0;
                int multiplier = 0;
                for (int j = 0; j < innerTable.list.size(); j++) {
                    SymbolTableRecord ptr = innerTable.list.get(j);
                    if (ptr.kind.equals("variable")) {
                        int sizetemp = 1;
                        // calc size here
                        if (table.containsKey(ptr.type.get(0))) {
                            multiplier = table.get(ptr.type.get(0));

                            for (int k = 1; k < ptr.type.size(); k++) {
                                sizetemp = sizetemp * Integer.parseInt(ptr.type.get(k));
                            }

                            size = size + sizetemp * multiplier;
                        } else {
                            done = false;
                            counter++;
                            break;
                        }
                    }

                }
                if (!done) {
                    counter++;
                    break;
                }
                globaltable.list.get(i).size = size;
                table.put(globaltable.list.get(i).name, size);
            }
        }
        if (!done) {
            if (counter == 100) {
                writer.write("Please chk for circular dependency" + System.getProperty("line.separator"));
                return;
            }
            calcClassSize(globaltable, table, writer);
        }
    }

    static String getString(Stack stackSemantic) {
        StringBuilder sbV = new StringBuilder();

        LinkedList<String> abc = new LinkedList();
        LinkedList<String> def = new LinkedList();

        while (stackSemantic.contains("@")) {
            if (stackSemantic.peek().equals("@")) {
                stackSemantic.pop();
            } else {
                abc.add(stackSemantic.pop().toString());
            }
        }

        for (int i = abc.size() - 1; i >= 0; i--) {
            def.add(abc.get(i));
        }
        for (int i = 0; i < def.size(); i++) {
            sbV.append(" ").append(def.get(i));
        }

        String sV = new StringBuilder(sbV).toString().trim();
        return sV;
    }

    static String getStringSafe(Stack stackSemantic) {
        StringBuilder sbV = new StringBuilder();

        LinkedList<String> stackRebuilder = new LinkedList<>();

        LinkedList<String> abc = new LinkedList();
        LinkedList<String> def = new LinkedList();

        while (stackSemantic.contains("@")) {
            if (stackSemantic.peek().equals("@")) {
                String temp = stackSemantic.pop().toString();
                stackRebuilder.add(temp);
            } else {
                String temp = stackSemantic.pop().toString();
                abc.add(temp);
                stackRebuilder.add(temp);
            }
        }
        for (int i = stackRebuilder.size() - 1; i > -1; i--) {
            stackSemantic.push(stackRebuilder.get(i));
        }

        for (int i = abc.size() - 1; i >= 0; i--) {
            def.add(abc.get(i));
        }
        for (int i = 0; i < def.size(); i++) {
            sbV.append(" ").append(def.get(i));
        }

        String sV = new StringBuilder(sbV).toString().trim();
        return sV;
    }

    static String getFunctionName(Stack stackSemantic) {

        String topStack = stackSemantic.pop().toString();
        while (!topStack.equals("(")) {
            topStack = stackSemantic.pop().toString();
        }

        String funcName = stackSemantic.pop().toString();
        stackSemantic.pop();
        stackSemantic.push("r1");

        return funcName;
    }

    static String[] getDeclare(String value) {
        String[] finalAns = null;

        //   STRING MANIPULATION
        String[] y1 = value.split("[\\[.*\\]]");
        List<String> list = new ArrayList<String>();
        for (String text : y1) {
            text = text.trim();
            if (text != null && text.length() > 0) {
                list.add(text);
            }
        }
        y1 = list.toArray(new String[0]);
        String[] y2 = y1[0].split(" ");
        // STRING MANIPULATION - END HERE

        finalAns = new String[y2.length + y1.length - 1];
        finalAns[0] = y2[1];
        finalAns[1] = y2[0];

        for (int i = 1; i < y1.length; i++) {
            finalAns[i + 1] = y1[i];
        }

        return finalAns;
    }

    // writes the get and put function to assembly code so it can be easily called from written program
    static void writeStaticCode(FileWriter writer) throws IOException {
        writer.write(System.getProperty("line.separator") + System.getProperty("line.separator")
                + "putint9	res	12 " + System.getProperty("line.separator")
                + "newline db	\"\",10" + System.getProperty("line.separator")
                + "		align" + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "     % Prints numbers" + System.getProperty("line.separator")
                + "putint	align" + System.getProperty("line.separator")
                + "	add	r2,r0,r0		" + System.getProperty("line.separator")
                + "	cge	r3,r1,r0		" + System.getProperty("line.separator")
                + "	bnz	r3,putint1		" + System.getProperty("line.separator")
                + "	sub	r1,r0,r1		" + System.getProperty("line.separator")
                + "putint1	modi	r4,r1,10	" + System.getProperty("line.separator")
                + "	addi	r4,r4,48		" + System.getProperty("line.separator")
                + "	divi	r1,r1,10		" + System.getProperty("line.separator")
                + "	sb	putint9(r2),r4		" + System.getProperty("line.separator")
                + "	addi	r2,r2,1			" + System.getProperty("line.separator")
                + "	bnz	r1,putint1		" + System.getProperty("line.separator")
                + "	bnz	r3,putint2		" + System.getProperty("line.separator")
                + "	addi	r3,r0,45                " + System.getProperty("line.separator")
                + "	sb	putint9(r2),r3		" + System.getProperty("line.separator")
                + "	addi	r2,r2,1			" + System.getProperty("line.separator")
                + "	add	r1,r0,r0		" + System.getProperty("line.separator")
                + "putint2	subi	r2,r2,1		" + System.getProperty("line.separator")
                + "	lb	r1,putint9(r2)		" + System.getProperty("line.separator")
                + "	putc	r1			" + System.getProperty("line.separator")
                + "	bnz	r2,putint2		" + System.getProperty("line.separator")
                + "	jr	r15			" + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "	% Prints new line" + System.getProperty("line.separator")
                + "putline	nop" + System.getProperty("line.separator")
                + "		add r2,r0,r0" + System.getProperty("line.separator")
                + "pri 	lb r3,newline(r2)" + System.getProperty("line.separator")
                + "		ceqi r4,r3,0" + System.getProperty("line.separator")
                + "		bnz r4,pr2" + System.getProperty("line.separator")
                + "		putc r3" + System.getProperty("line.separator")
                + "		addi r2,r2,1" + System.getProperty("line.separator")
                + "		j pri" + System.getProperty("line.separator")
                + "pr2		add r2,r0,r0" + System.getProperty("line.separator")
                + "		jr r15" + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "     % Gets numbers" + System.getProperty("line.separator")
                + "getint	align" + System.getProperty("line.separator")
                + "	add	r1,r0,r0		" + System.getProperty("line.separator")
                + "	add	r2,r0,r0		" + System.getProperty("line.separator")
                + "	add	r4,r0,r0		" + System.getProperty("line.separator")
                + "getint1	getc	r1		" + System.getProperty("line.separator")
                + "	ceqi	r3,r1,43		" + System.getProperty("line.separator")
                + "	bnz	r3,getint1		" + System.getProperty("line.separator")
                + "	ceqi	r3,r1,45		" + System.getProperty("line.separator")
                + "	bz	r3,getint2		" + System.getProperty("line.separator")
                + "	addi	r4,r0,1			" + System.getProperty("line.separator")
                + "	j	getint1			" + System.getProperty("line.separator")
                + "getint2	clti	r3,r1,48	" + System.getProperty("line.separator")
                + "	bnz	r3,getint3		" + System.getProperty("line.separator")
                + "	cgti	r3,r1,57		" + System.getProperty("line.separator")
                + "	bnz	r3,getint3		" + System.getProperty("line.separator")
                + "	sb	getint9(r2),r1		" + System.getProperty("line.separator")
                + "	addi	r2,r2,1			" + System.getProperty("line.separator")
                + "	j	getint1			" + System.getProperty("line.separator")
                + "getint3	sb	getint9(r2),r0	" + System.getProperty("line.separator")
                + "	add	r2,r0,r0		" + System.getProperty("line.separator")
                + "	add	r1,r0,r0		" + System.getProperty("line.separator")
                + "	add	r3,r0,r0		" + System.getProperty("line.separator")
                + "getint4	lb	r3,getint9(r2)	" + System.getProperty("line.separator")
                + "	bz	r3,getint5		" + System.getProperty("line.separator")
                + "	subi	r3,r3,48		" + System.getProperty("line.separator")
                + "	muli	r1,r1,10		" + System.getProperty("line.separator")
                + "	add	r1,r1,r3		" + System.getProperty("line.separator")
                + "	addi	r2,r2,1			" + System.getProperty("line.separator")
                + "	j	getint4			" + System.getProperty("line.separator")
                + "getint5	bz	r4,getint6	" + System.getProperty("line.separator")
                + "	sub	r1,r0,r1		" + System.getProperty("line.separator")
                + "getint6	jr	r15		" + System.getProperty("line.separator")
                + "getint9	res	12		" + System.getProperty("line.separator")
                + "	align"
        );

        writer.flush();
    }
    static int counter = 0;

    static void writeVariablesUsed(FileWriter writer, int counter) throws IOException {

        for (int i = 0; i < counter; i++) {
            writer.write("var" + i + "      res     4" + System.getProperty("line.separator"));
        }
    }

}
