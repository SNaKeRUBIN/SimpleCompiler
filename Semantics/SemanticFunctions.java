package Semantics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * @author SNaKeRUBIN
 */
public class SemanticFunctions {

    Stack diffClassNames;

    FileWriter writerSymbolTables;
    FileWriter writerErrorsSemantic;

    public SemanticFunctions(Stack new_diffClassNames, String new_outputSymbolTables,
            String new_outputErrorsSemantic) throws IOException {
        diffClassNames = new_diffClassNames;
        writerSymbolTables = new FileWriter(new File(new_outputSymbolTables));
        writerErrorsSemantic = new FileWriter(new File(new_outputErrorsSemantic));
        System.out.println("");
    }

    public SymbolTable createGlobalTable() {
        return new SymbolTable();
    }

    public SymbolTable createTable(String new_name, int new_level, SymbolTable new_parentTable) {
        return new SymbolTable(new_name, new_level, new_parentTable);
    }

    public SymbolTable delete() {
        return null;
    }

    public SymbolTableRecord insert(SymbolTable parentTable, String value,
            String new_kind) {
        SymbolTableRecord ptr = new SymbolTableRecord();
        ptr.kind = new_kind;

        switch (new_kind) {
            case "class":
                ptr.name = value;
                ptr.type = null;
                ptr.linkForward = new SymbolTable(parentTable.name + " : "
                        + value, parentTable.level + 1, parentTable);
                break;
            case "program":
                ptr.kind = "function";
                ptr.name = "program";
                ptr.linkForward = new SymbolTable("program", parentTable.level
                        + 1, parentTable);
                break;

            case "function":
                // FUNCTION TYPE STILL MISSING

                // get types and names from String
                String abc = value;

                String returnType;
                String nameFunction;

                String[] x1 = abc.split("\\(");
                String[] z1 = x1[0].split(" ");

                returnType = z1[0];
                nameFunction = z1[1];

                ptr.name = nameFunction;
                ptr.linkForward = new SymbolTable(parentTable.name + " : "
                        + nameFunction, parentTable.level + 1, parentTable);

                x1[1] = x1[1].replace(")", "");

                if (x1[1].equals(" ")) {
                    ptr.type = new LinkedList<>();
                    ptr.type.add(returnType);
                    break;
                } else {
                    ptr.type = new LinkedList<>();
                    ptr.type.add(returnType);
                }

                ArrayList<String> pnames = new ArrayList<>();
                ArrayList<String> ptypes = new ArrayList<>();

                String[] fparams = x1[1].split(",");

                for (int i = 0; i < fparams.length; i++) {

                    fparams[i] = fparams[i].replace("[", "");
                    fparams[i] = fparams[i].replace("]", "");
                    fparams[i] = fparams[i].trim();

                }

                // only adding variable type not array size
                for (int i = 0; i < fparams.length; i++) {
                    String[] x2 = fparams[i].split(" ");

                    List<String> list1 = new ArrayList<String>();
                    for (String text : x2) {
                        if (text != null && text.length() > 0) {
                            list1.add(text.trim());
                        }
                    }
                    x2 = list1.toArray(new String[0]);

                    StringBuilder sb = new StringBuilder(x2[1]);

                    ptypes.add(x2[0].trim());
                    for (int k = 2; k < x2.length; k++) {
                        sb.append("[").append(x2[k].trim()).append("]");
                    }
                    pnames.add(sb.toString());

                }

                LinkedList<LinkedList<String>> parameters = new LinkedList<>();

                for (int i = 0; i < pnames.size(); i++) {
                    String temp = pnames.get(i);

                    String[] x2 = temp.split("[\\[.*\\]]");

                    List<String> list = new ArrayList<String>();
                    for (String text : x2) {
                        if (text != null && text.length() > 0) {
                            list.add(text);
                        }
                    }
                    x2 = list.toArray(new String[0]);

                    LinkedList<String> tempList = new LinkedList<>();
                    tempList.add(x2[0]);
                    tempList.add(ptypes.get(i));

                    for (int j = 1; j < x2.length; j++) {
                        tempList.add(x2[j]);
                    }

                    parameters.add(tempList);

                }

                for (int i = 0; i < parameters.size(); i++) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(parameters.get(i).get(1));
                    for (int j = 2; j < parameters.get(i).size(); j++) {

                        sb.append("[").append(parameters.get(i).get(j)).append("]");
                    }
                    ptr.type.add(sb.toString());
                }

                // TILL HERE - READING TYPE STRING
                // SETTING PARAMETERS OF RECORD AND ADDING RECORDS TO CHILD TABLE
                for (int i = 0; i < parameters.size(); i++) {
                    SymbolTableRecord tempRecord = new SymbolTableRecord();
                    tempRecord.name = parameters.get(i).get(0);
                    tempRecord.kind = "parameter";
                    tempRecord.linkForward = null;
                    tempRecord.type = new LinkedList<>();

                    for (int j = 1; j < parameters.get(i).size(); j++) {
                        tempRecord.type.add(parameters.get(i).get(j));
                    }

                    ptr.linkForward.list.add(tempRecord);
                }

                break;

            case "variable":

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

                ptr.name = y2[1];
                ptr.type = new LinkedList<>();
                ptr.type.add(y2[0]);
                for (int i = 1; i < y1.length; i++) {
                    ptr.type.add(y1[i]);
                }

                if (ptr.type.get(0).equals("int") || ptr.type.get(0).equals("float")) {
                } else {
                    diffClassNames.add(ptr.type.get(0));
                }

                break;
            default:
        }

        return ptr;
    }

    public SymbolTableRecord search(SymbolTable table, String name) {
        // use recursive
        // goes through all records searching for identifier, if found return true
        // if(lvl=1), return false
        // if not found, call search on linkBackward

        SymbolTableRecord ptr = null;

        for (int i = 0; i < table.list.size(); i++) {
            if (table.list.get(i).name.equals(name)) {
                //System.out.println("Record Found");
                ptr = table.list.get(i);
                return ptr;
            }
        }

        if (table.level != 1) {
            ptr = search(table.linkBackward, name);
        } else {
            return ptr;
        }

        return ptr;
    }

    public void print(SymbolTable table) throws IOException {

        if (table == null) {
            writerSymbolTables.write("Table no longer exists" + System.getProperty("line.separator"));
            return;
        } else if (!table.name.equals("Global")) {
            writerSymbolTables.write("Please give Global Table to me" + System.getProperty("line.separator"));
            return;
        }
        // add global table to linked list (which is an queue)
        // go thru all entried of list and print em
        // if u find an entry that has forward link, add that link to linked list
        // go thru all linked list elements and print em all

        Stack stack = new Stack();
        stack.push(table);

        while (!stack.isEmpty()) {
            SymbolTable tempTable = (SymbolTable) stack.pop();
            writerSymbolTables.write("Symbol Table: " + tempTable.name
                    + System.getProperty("line.separator"));
            for (int i = 0; i < tempTable.list.size(); i++) {
                if (tempTable.list.get(i).linkForward != null) {
                    stack.push(tempTable.list.get(i).linkForward);
                }

                writerSymbolTables.write("Name: " + tempTable.list.get(i).name
                        + ", Kind: " + tempTable.list.get(i).kind
                        + ", Type: " + tempTable.list.get(i).type
                        + System.getProperty("line.separator"));
            }
            writerSymbolTables.write(System.getProperty("line.separator"));
        }
        writerSymbolTables.close();
    }

    // CHKS IF VARIABLE USED ON RHS IS ACTUALLY DECLARED
    public void varDeclare(SymbolTable currTable, String statement) throws IOException {
        // ADD METHOD TO CHK IF VARIABLE IS USED ON RHS AND IF IT IS DECLARED
        // ALSO CHK IF TYPES AROUND "=" MATCH
        // find return type of lhs and store
        // split acc to /*-+ using StringTokenizer and for each chk return type
        // if any mismatch -> ERROR
        // else, chk if RHS.type == LHS.type

        // split acc to - "=", ".", "[}"
        String x1[] = statement.split("=");

        SymbolTable currTableBackup = currTable;

        String RHS = x1[1].trim();

        String x2[] = x1[0].split("[.*[.].*]");

        String typeRHS = "";
        String typeLHS = "";

        for (int i = 0; i < x2.length; i++) {

            String[] y1 = x2[i].split("[\\[.*\\]]");
            List<String> list = new ArrayList<String>();
            for (String text : y1) {
                text = text.trim();
                if (text != null && text.length() > 0) {
                    list.add(text);
                }
            }
            y1 = list.toArray(new String[0]);
            String varName = y1[0];

            SymbolTableRecord ptr = null;
            try {
                if (i == 0) {
                    ptr = search(currTable, varName);

                    // if ptr exists, get table pointer of ptr type and set it as currTable
                    if (ptr != null) {
                        SymbolTable GlobalTable = null;
                        int pointer = currTable.level;
                        while (pointer != 1) {
                            currTable = currTable.linkBackward;
                            pointer = currTable.level;
                        }
                        GlobalTable = currTable;

                        for (int j = 0; j < GlobalTable.list.size(); j++) {
                            if (currTable.list.get(j).name.equals(ptr.type.get(0))) {
                                currTable = currTable.list.get(j).linkForward;
                                break;
                            }
                        }
                    }
                } else {
                    ptr = search(currTable, varName);
                }
            } catch (Exception e) {
            }
            if (ptr != null) {
                typeLHS = ptr.type.get(0);
            } else {
                // THIS WILL TELL IF UNDECLARED VARAIBLE IS USED
                writerErrorsSemantic.write("InValid Case as variable \"" + varName
                        + "\" is not defined for: " + statement + System.getProperty("line.separator"));
                writerErrorsSemantic.flush();
                return;
            }

        }

        typeRHS = typeChk(currTableBackup, RHS);

        if (typeLHS.equals(typeRHS)) {
        } else {
            writerErrorsSemantic.write("Return types do not match for " + statement
                    + System.getProperty("line.separator"));
            writerErrorsSemantic.flush();
        }

    }

    // IF DECLARED VARIABLE NAME IS ALREADY INUSE
    public Boolean alreadyExist(SymbolTable currTable, String value) {
        // EXTRACT NAME OF VARIABLE FROM VALUE STRING

        String[] x1 = value.split(" ");

        String name = x1[1];
        for (int i = 0; i < currTable.list.size(); i++) {
            if (currTable.list.get(i).name.equals(name)) {
                return true;
            }
        }
        return false;

    }

    public void funcDefined(SymbolTable currTable, String statement) throws IOException {

        LinkedList<LinkedList<String>> parameters = new LinkedList<>();
        String funcName;
        String typeTemp;

        // STRING MANIPULATION HERE
        String[] z1 = statement.split("=");
        String LHS = z1[0].trim();
        String RHS = z1[1].trim();

        String[] z2 = RHS.split("[(*)]");
        funcName = z2[0].trim();
        String x1 = z2[1].trim();

        // get pointer to lhs variable
        SymbolTableRecord ptrLHS = search(currTable, LHS);

        if (ptrLHS == null) {
            writerErrorsSemantic.write("Function trying to return to undefined variable: "
                    + LHS + " with input: " + statement
                    + System.getProperty("line.separator"));
            writerErrorsSemantic.flush();
            return;
        }
        LinkedList<String> parametersLHS = new LinkedList<>();
        for (int i = 0; i < ptrLHS.type.size(); i++) {
            parametersLHS.add(ptrLHS.type.get(i));
        }

        // get pointer to fn
        SymbolTableRecord ptr = search(currTable, funcName);

        if (ptr != null) {

            // CHKING FOR RETURN TYPE
            LinkedList<String> returnFunction = new LinkedList<>();
            returnFunction.add(ptr.type.get(0));

            // CHKING FOR PARAMTERS NUMBER AND SIZE
            SymbolTable tableFrwd = ptr.linkForward;

            if (returnFunction.size() == parametersLHS.size()
                    && returnFunction.get(0).equals(parametersLHS.get(0))) {
            } else {
                writerErrorsSemantic.write("Return type does not match for function: "
                        + funcName + " with input: " + statement
                        + System.getProperty("line.separator"));
                writerErrorsSemantic.flush();
                return;
            }

            for (int i = 0; i < tableFrwd.list.size(); i++) {
                if (tableFrwd.list.get(i).kind.equals("parameter")) {
                    LinkedList<String> temp = new LinkedList<>();

                    temp.add(tableFrwd.list.get(i).type.get(0));
                    for (int j = 1; j < tableFrwd.list.get(i).type.size(); j++) {
                        temp.add(tableFrwd.list.get(i).type.get(j));
                    }

                    parameters.add(temp);
                }

            }

        } else {
            //  FUNCTION NOT DEFINED ??
            writerErrorsSemantic.write("Function not found: " + funcName
                    + System.getProperty("line.separator"));
            writerErrorsSemantic.flush();
        }

        // System.out.print("");
        LinkedList<LinkedList<String>> parametersStatement = new LinkedList();
        // NOT CHKING IF PTR IS NULL
        // TYPE CHK OF PASS PARAMETERS
        if (x1.equals("")) {
            String z3 = "";
        } else {

            String[] z3 = x1.split(",");
            //  z3 = m5.mc1v2 // no of parameters
            //  split over "." and goto last nest
            //  how abt i convt z3 into only last part of nested "."s after chking each one
            //  also see if curr table needs to be changed

            LinkedList<SymbolTable> typeTables = new LinkedList<>();

            SymbolTable currTableBackup = currTable;

            Boolean alreadyDoneParam[] = new Boolean[z3.length];
            for (int i = 0; i < z3.length; i++) {
                // splitting with "." can fuck up float value

                String parameterIsNumber = typeChk(currTable, z3[i]);
                if (parameterIsNumber.equals("int") || parameterIsNumber.equals("float")) {
                    alreadyDoneParam[i] = true;
                    continue;
                }
                String[] x2 = z3[i].trim().split("\\."); // "." nested paramter
                for (int j = 0; j < x2.length - 1; j++) {
                    SymbolTableRecord ptrTemp = search(currTable, x2[j].trim());

                    while (currTable.level != 1) {
                        currTable = currTable.linkBackward;
                    }
                    for (int k = 0; k < currTable.list.size(); k++) {
                        if (currTable.list.get(k).name.equals(ptrTemp.type.get(0))) {
                            currTable = currTable.list.get(k).linkForward;
                        } else {
                        }
                    }

                }
                // make an array if SymbolTables where last nested '." variable was found
                // use this array when type checking
                // make sure that if no nested '.'s is found, curr table is accurate
                typeTables.add(currTable);
                currTable = currTableBackup;
                z3[i] = x2[x2.length - 1].trim();
            }

            // HANDLE  ARRAYS HERE
            for (int i = 0; i < z3.length; i++) {
                if (alreadyDoneParam[i]) {
                    String parameterIsNumber = typeChk(currTable, z3[i]);
                    LinkedList<String> temp = new LinkedList();
                    temp.add(parameterIsNumber);
                    parametersStatement.add(temp);
                    continue;

                }

                String[] x2 = z3[i].trim().split("[\\[.*\\]]");

                for (int j = 0; j < x2.length; j++) {
                    x2[j] = x2[j].trim();
                }

                List<String> list = new ArrayList<String>();
                for (String text : x2) {
                    if (text != null && text.length() > 0) {
                        list.add(text.trim());
                    }
                }
                x2 = list.toArray(new String[0]);
                LinkedList<String> temp = new LinkedList();
                for (int j = 0; j < x2.length; j++) {
                    if (j == 0) {
                        if (typeTables.size() > 0) {
                            currTable = typeTables.get(i);
                        }
                        typeTemp = typeChk(currTable, x2[j].trim());
                        temp.add(typeTemp);
                    } else {
                        temp.add(x2[j]);
                    }

                }
                parametersStatement.add(temp);
            }

        }

        if (parameters.size() == parametersStatement.size()) {
            for (int i = 0; i < parameters.size(); i++) {
                if (parameters.get(i).size() == parametersStatement.get(i).size()) {
                    for (int j = 0; j < parameters.get(i).size(); j++) {
                        if (j == 0) {
                            if (parameters.get(i).get(j).equals(parametersStatement.get(i).get(j))) {
                            } else {
                            }
                        } else {
                            int a = Integer.parseInt(parameters.get(i).get(j));
                            int b = Integer.parseInt(parametersStatement.get(i).get(j));
                            if (a > b) {
                            } else {
                            }
                        }
                    }
                } else {
                }
            }
        } else {
        }

    }

    public String typeChk(SymbolTable currTable, String input) {
        // take various forms of inputs and type chk here
        // need to make out variable types so maybe even pass on some table

        String type = "";
        try {
            int a = Integer.parseInt(input.trim());
            type = "int";
        } catch (Exception e) {
            try {
                float b = Float.parseFloat(input.trim());
                type = "float";
            } catch (Exception e1) {
                if (input.contains("+") || input.contains("-") || input.contains("/")
                        || input.contains("*")) {
                    String delims = "+-*/";
                    StringTokenizer st = new StringTokenizer(input.trim(), delims);

                    LinkedList<String> abc = new LinkedList<>();

                    while (st.hasMoreTokens()) {
                        abc.add(st.nextToken());
                    }

                    Boolean valid = true;

                    String temp = typeChk(currTable, abc.get(0));
                    for (int i = 1; i < abc.size(); i++) {
                        type = typeChk(currTable, abc.get(i));
                        if (!type.equals(temp)) {
                            valid = false;
                        }
                        type = temp;
                    }

                    if (valid) {
                    } else {
                        type = "TYPE MISMATCH";
                    }

                }
                //System.out.println("VARIABLE DETECTED U MOTHAFUKA");
                if (type.equals("")) {
                    SymbolTableRecord ptr = search(currTable, input.trim());
                    if (ptr != null) {
                        type = ptr.type.get(0);
                    } else {
                        type = "UnDefined Variable: " + input;
                    }
                }
            }
        }

        // get type of variable here
        return type;

    }

    public SymbolTableRecord varProperlyDefined(SymbolTable table, String statement) {

        String[] z1 = statement.split(" ");
        String typeName = z1[0];

        SymbolTableRecord ptr = null;

        for (int i = 0; i < table.list.size(); i++) {
            if (table.list.get(i).name.equals(typeName)
                    && table.list.get(i).kind.equals("class")) {
                ptr = table.list.get(i);
            }
        }

        if (table.level != 1) {
            ptr = varProperlyDefined(table.linkBackward, typeName);
        } else {
            return ptr;
        }

        return ptr;
    }

    public void classCirDep(SymbolTable globaltable) throws IOException {
// INCOMPLETE

        Stack<String> unvisited = new Stack<>();
        Stack<String> visiting = new Stack<>();
        HashSet<String> visited = new HashSet<>();

        HashMap<String, Integer> map = new HashMap();

        for (int i = 0; i < globaltable.list.size(); i++) {
            SymbolTableRecord ptr = globaltable.list.get(i);
            if (ptr.kind.equals("class")) {
                map.put(ptr.name, i);
                unvisited.add(ptr.name);
            }
        }

        while (!unvisited.isEmpty()) {
            String currType = unvisited.pop();
            visiting.add(currType);

            while (!visiting.isEmpty()) {
                String currVisiting = visiting.peek();
                Boolean flag = false;
                int index = map.get(currVisiting);
                SymbolTable currTable = globaltable.list.get(index).linkForward;
                for (int i = 0; i < currTable.list.size(); i++) {
                    SymbolTableRecord ptr = currTable.list.get(i);
                    if (ptr.kind.equals("variable")) {
                        if ((!ptr.type.get(0).equals("int")) && (!ptr.type.get(0).equals("float"))) {
                            if (visited.contains(ptr.type.get(0))) {
                            } else if (unvisited.contains(ptr.type.get(0))) {
                                unvisited.remove(ptr.type.get(0));
                                visiting.add(ptr.type.get(0));
                                flag = true;
                                break;
                            } else if (visiting.contains(ptr.type.get(0))) {
                                writerErrorsSemantic.write("Cycle detected"
                                        + System.getProperty("line.separator"));
                                writerErrorsSemantic.flush();
                                return;
                            }
                        }
                    }
                }
                if (flag) {
                } else {
                    visiting.remove(currVisiting);
                    visited.add(currType);
                }
            }
        }
    }
}
