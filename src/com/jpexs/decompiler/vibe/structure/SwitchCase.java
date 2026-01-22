package com.jpexs.decompiler.vibe.structure;

import com.jpexs.decompiler.vibe.Node;

/**
 * Represents a single case in a switch structure.
 */
public class SwitchCase {
    public final Node conditionNode;  // the condition node (e.g., "if1")
    public final Node caseBody;       // the case body node (e.g., "case1"), null for label-only merged cases
    public final boolean isDefault;   // true if this is the default case
    public final boolean hasBreak;    // true if this case should have a break statement
    public final boolean negated;     // true if the condition is negated
    public final boolean skipsMerge;  // true if this case skips the switch merge (needs labeled block break)
    
    public SwitchCase(Node conditionNode, boolean negated, Node caseBody, boolean isDefault) {
        this(conditionNode, negated, caseBody, isDefault, true, false);
    }
    
    public SwitchCase(Node conditionNode, boolean negated, Node caseBody, boolean isDefault, boolean hasBreak) {
        this(conditionNode, negated, caseBody, isDefault, hasBreak, false);
    }
    
    public SwitchCase(Node conditionNode, boolean negated, Node caseBody, boolean isDefault, boolean hasBreak, boolean skipsMerge) {
        this.conditionNode = conditionNode;
        this.negated = negated;
        this.caseBody = caseBody;
        this.isDefault = isDefault;
        this.hasBreak = hasBreak;
        this.skipsMerge = skipsMerge;
    }
    
    @Override
    public String toString() {
        if (isDefault) {
            return "default:" + caseBody;
        }
        return "case " + conditionNode + ":" + caseBody;
    }
}
