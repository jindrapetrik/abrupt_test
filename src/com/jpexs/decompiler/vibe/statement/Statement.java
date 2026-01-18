package com.jpexs.decompiler.vibe.statement;

/**
 * Base class for all pseudocode statements.
 * Represents a single statement in the generated pseudocode.
 * 
 * @author JPEXS
 */
public abstract class Statement {
    
    /**
     * Converts this statement to its pseudocode string representation.
     * 
     * @return the pseudocode representation of this statement
     */
    @Override
    public abstract String toString();
    
    /**
     * Converts this statement to its pseudocode string representation with the given indentation.
     * 
     * @param indent the indentation to use
     * @return the pseudocode representation of this statement
     */
    public abstract String toString(String indent);
}
