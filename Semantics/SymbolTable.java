package Semantics;

import java.util.ArrayList;

/**
 * This class stores the information about identifiers(variables, functions,
 * classes) defined in its own scope.
 */
public class SymbolTable {

    int level;
    public String name;
    public ArrayList<SymbolTableRecord> list;
    SymbolTable linkBackward;

    // only used to create global table
    /**
     * Constructor to create the global Symbol table. The level of Global Symbol
     * Table is always 1
     */
    public SymbolTable() {
        name = "Global";
        level = 1;
        list = new ArrayList<>();
        linkBackward = null;
    }

    /**
     * Constructor to create a child Symbol Table
     *
     * @param new_name name for Symbol Table
     * @param new_level the level of the Symbol Table
     * @param new_parentTable link to the parent Symbol Table
     */
    public SymbolTable(String new_name, int new_level, SymbolTable new_parentTable) {
        name = new_name;
        level = new_level;
        list = new ArrayList<>();
        linkBackward = new_parentTable;
    }

    // COULD THIS HELP IN MAKING SYMBOL TABLE FOR FUNCTIONS
//    public SymbolTable(String new_name, int new_level, SymbolTable new_parentTable, LinkedList<String> toBeAddedParameters) {
//        name = new_name;
//        level = new_level;
//        list = new ArrayList<>();
//
//        list = new ArrayList<>();
//        for (String temp : toBeAddedParameters) {
//            // for each parameter in toBeAddedParamters, add entries into this list
//        }
//
//        linkBackward = new_parentTable;
//    }
    /**
     * Return the link to the parent Symbol Table
     *
     * @return link to parent Symbol Table
     */
    public SymbolTable getParentTable() {
        return linkBackward;
    }

}
