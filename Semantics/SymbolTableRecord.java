package Semantics;

import java.util.LinkedList;

/**
 * This class is used to store data as a record in a Symbol Table
 */
public class SymbolTableRecord {

    public String name;
    public String kind;
    public LinkedList<String> type;
    public int size;

    SymbolTable linkForward;

    /**
     * Return the forward link of the table record
     *
     * @return link to forward table
     */
    public SymbolTable getLink() {
        return linkForward;
    }

}
