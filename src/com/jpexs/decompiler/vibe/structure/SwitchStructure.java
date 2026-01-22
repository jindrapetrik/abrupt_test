package com.jpexs.decompiler.vibe.structure;

import com.jpexs.decompiler.vibe.Node;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a switch structure detected in the CFG.
 * A switch is detected when there's a chain of conditional nodes where each has:
 * - One branch going to the next condition (fall-through)
 * - One branch going to a case body
 * - All case bodies lead to the same merge node
 */
public class SwitchStructure {
    public final Node startNode;         // first condition node
    public final List<SwitchCase> cases; // list of cases
    public final Node mergeNode;         // where most cases converge (switch merge)
    public final Node outerMergeNode;    // where cases that skip mergeNode go (for labeled block), may be null
    
    public SwitchStructure(Node startNode, List<SwitchCase> cases, Node mergeNode) {
        this(startNode, cases, mergeNode, null);
    }
    
    public SwitchStructure(Node startNode, List<SwitchCase> cases, Node mergeNode, Node outerMergeNode) {
        this.startNode = startNode;
        this.cases = new ArrayList<>(cases);
        this.mergeNode = mergeNode;
        this.outerMergeNode = outerMergeNode;
    }
    
    /**
     * Returns true if this switch has cases that skip the merge node and require a labeled block.
     */
    public boolean hasOuterMerge() {
        return outerMergeNode != null && !outerMergeNode.equals(mergeNode);
    }
    
    @Override
    public String toString() {
        String outer = outerMergeNode != null ? ", outerMerge=" + outerMergeNode : "";
        return "Switch{start=" + startNode + ", cases=" + cases.size() + ", merge=" + mergeNode + outer + "}";
    }
}
