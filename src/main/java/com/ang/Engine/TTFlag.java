package com.ang.Engine;

/**
 * Enum for flags representing different node types in the search tree
 */
public enum TTFlag {
    PV  (3), // principal variation node (exact)
    ALL (1), // all node (lower bound) 
    CUT (2); // cut node (upper bound)

    private int precedence;

    /**
     * Initializes the node types with a precedence value for sorting their value
     * @param precedence the relative value of the node type
     */
    private TTFlag(int precedence) {
        this.precedence = precedence;
    }

    /**
     * @return precendence value for the node
     */
    public int precedence() {
        return precedence;
    }
}
