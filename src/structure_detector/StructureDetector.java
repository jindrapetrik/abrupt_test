/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package structure_detector;

import java.util.*;

/**
 * Detects control flow structures in a Control Flow Graph (CFG).
 * Capable of detecting:
 * - If statements (conditional branches)
 * - Loops (cycles in the graph)
 * - Break statements (edges that exit a loop early)
 * - Continue statements (edges that jump back to loop header)
 *
 * @author JPEXS
 */
public class StructureDetector {

    /**
     * Represents an if-statement structure detected in the CFG.
     */
    public static class IfStructure {
        public final Node conditionNode;
        public final Node trueBranch;
        public final Node falseBranch;
        public final Node mergeNode; // may be null if branches don't merge

        public IfStructure(Node conditionNode, Node trueBranch, Node falseBranch, Node mergeNode) {
            this.conditionNode = conditionNode;
            this.trueBranch = trueBranch;
            this.falseBranch = falseBranch;
            this.mergeNode = mergeNode;
        }

        @Override
        public String toString() {
            return "If{condition=" + conditionNode + 
                   ", true=" + trueBranch + 
                   ", false=" + falseBranch + 
                   ", merge=" + mergeNode + "}";
        }
    }

    /**
     * Represents a loop structure detected in the CFG.
     */
    public static class LoopStructure {
        public final Node header;        // loop header node
        public final Set<Node> body;     // all nodes in the loop body
        public final Node backEdgeSource; // node that jumps back to header
        public final List<BreakEdge> breaks;
        public final List<ContinueEdge> continues;

        public LoopStructure(Node header, Set<Node> body, Node backEdgeSource) {
            this.header = header;
            this.body = body;
            this.backEdgeSource = backEdgeSource;
            this.breaks = new ArrayList<>();
            this.continues = new ArrayList<>();
        }

        @Override
        public String toString() {
            return "Loop{header=" + header + 
                   ", body=" + body + 
                   ", backEdge=" + backEdgeSource + 
                   ", breaks=" + breaks + 
                   ", continues=" + continues + "}";
        }
    }

    /**
     * Represents a break edge - an edge that exits a loop early.
     */
    public static class BreakEdge {
        public final Node from;
        public final Node to;

        public BreakEdge(Node from, Node to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public String toString() {
            return "Break{" + from + " -> " + to + "}";
        }
    }

    /**
     * Represents a continue edge - an edge that jumps back to loop header.
     */
    public static class ContinueEdge {
        public final Node from;
        public final Node to;

        public ContinueEdge(Node from, Node to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public String toString() {
            return "Continue{" + from + " -> " + to + "}";
        }
    }

    private final List<Node> allNodes;
    private final Node entryNode;

    /**
     * Creates a new StructureDetector for the given CFG.
     * 
     * @param entryNode the entry node of the CFG
     */
    public StructureDetector(Node entryNode) {
        this.entryNode = entryNode;
        this.allNodes = collectAllNodes(entryNode);
    }

    /**
     * Collects all nodes reachable from the entry node using BFS.
     */
    private List<Node> collectAllNodes(Node entry) {
        List<Node> result = new ArrayList<>();
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        queue.add(entry);
        visited.add(entry);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            result.add(current);
            for (Node succ : current.succs) {
                if (!visited.contains(succ)) {
                    visited.add(succ);
                    queue.add(succ);
                }
            }
        }
        return result;
    }

    /**
     * Detects all if-statement structures in the CFG.
     * An if-statement is identified by a node with exactly 2 successors.
     * 
     * @return list of detected if structures
     */
    public List<IfStructure> detectIfs() {
        List<IfStructure> ifs = new ArrayList<>();
        
        for (Node node : allNodes) {
            if (node.isConditional()) {
                Node trueBranch = node.succs.get(0);
                Node falseBranch = node.succs.get(1);
                Node mergeNode = findMergeNode(trueBranch, falseBranch);
                ifs.add(new IfStructure(node, trueBranch, falseBranch, mergeNode));
            }
        }
        
        return ifs;
    }

    /**
     * Finds the merge node where two branches come together.
     * Uses post-dominator analysis simplified approach.
     */
    private Node findMergeNode(Node branch1, Node branch2) {
        // Collect all nodes reachable from branch1
        Set<Node> reachableFromBranch1 = getReachableNodes(branch1);
        
        // Find the first common node reachable from branch2 that is also reachable from branch1
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();
        queue.add(branch2);
        visited.add(branch2);
        
        // Check if branch2 itself is reachable from branch1
        if (reachableFromBranch1.contains(branch2)) {
            return branch2;
        }
        // Check if branch1 is reachable from branch2 using early-exit search
        if (isReachable(branch2, branch1)) {
            return branch1;
        }
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            for (Node succ : current.succs) {
                if (reachableFromBranch1.contains(succ)) {
                    return succ;
                }
                if (!visited.contains(succ)) {
                    visited.add(succ);
                    queue.add(succ);
                }
            }
        }
        
        return null; // No merge node found (e.g., branches don't converge)
    }

    /**
     * Checks if target is reachable from start using early-exit BFS.
     */
    private boolean isReachable(Node start, Node target) {
        if (start.equals(target)) {
            return true;
        }
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            for (Node succ : current.succs) {
                if (succ.equals(target)) {
                    return true;
                }
                if (!visited.contains(succ)) {
                    visited.add(succ);
                    queue.add(succ);
                }
            }
        }
        return false;
    }

    /**
     * Gets all nodes reachable from the given node.
     */
    private Set<Node> getReachableNodes(Node start) {
        Set<Node> reachable = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        queue.add(start);
        reachable.add(start);
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            for (Node succ : current.succs) {
                if (!reachable.contains(succ)) {
                    reachable.add(succ);
                    queue.add(succ);
                }
            }
        }
        
        return reachable;
    }

    /**
     * Detects all loop structures in the CFG using back-edge detection.
     * A back-edge is an edge from a node to one of its dominators,
     * indicating a loop.
     * 
     * @return list of detected loop structures
     */
    public List<LoopStructure> detectLoops() {
        List<LoopStructure> loops = new ArrayList<>();
        
        // Compute dominators
        Map<Node, Set<Node>> dominators = computeDominators();
        
        // Find back edges: edge (u, v) where v dominates u
        List<BackEdge> backEdges = new ArrayList<>();
        for (Node node : allNodes) {
            Set<Node> nodeDominators = dominators.get(node);
            for (Node succ : node.succs) {
                if (nodeDominators != null && nodeDominators.contains(succ)) {
                    backEdges.add(new BackEdge(node, succ));
                }
            }
        }
        
        // For each back edge, identify the natural loop
        for (BackEdge backEdge : backEdges) {
            Node header = backEdge.to;
            Node tail = backEdge.from;
            Set<Node> loopBody = findNaturalLoop(header, tail);
            LoopStructure loop = new LoopStructure(header, loopBody, tail);
            
            // Detect breaks and continues within the loop
            detectBreaksAndContinues(loop, dominators);
            
            loops.add(loop);
        }
        
        return loops;
    }

    private static class BackEdge {
        final Node from;
        final Node to;
        
        BackEdge(Node from, Node to) {
            this.from = from;
            this.to = to;
        }
    }

    /**
     * Computes dominators for all nodes using iterative dataflow analysis.
     * A node D dominates node N if every path from entry to N goes through D.
     */
    private Map<Node, Set<Node>> computeDominators() {
        Map<Node, Set<Node>> dominators = new HashMap<>();
        
        // Initialize: entry dominates only itself, others are dominated by all
        Set<Node> allNodesSet = new HashSet<>(allNodes);
        for (Node node : allNodes) {
            if (node.equals(entryNode)) {
                Set<Node> entryDom = new HashSet<>();
                entryDom.add(entryNode);
                dominators.put(node, entryDom);
            } else {
                dominators.put(node, new HashSet<>(allNodesSet));
            }
        }
        
        // Iterate until no changes
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Node node : allNodes) {
                if (node.equals(entryNode)) continue;
                
                Set<Node> newDom;
                
                // Handle nodes with no predecessors - they are only dominated by themselves
                if (node.preds.isEmpty()) {
                    newDom = new HashSet<>();
                } else {
                    newDom = new HashSet<>(allNodesSet);
                    // Intersect dominators of all predecessors
                    for (Node pred : node.preds) {
                        Set<Node> predDom = dominators.get(pred);
                        if (predDom != null) {
                            newDom.retainAll(predDom);
                        }
                    }
                }
                
                // Add the node itself
                newDom.add(node);
                
                if (!newDom.equals(dominators.get(node))) {
                    dominators.put(node, newDom);
                    changed = true;
                }
            }
        }
        
        return dominators;
    }

    /**
     * Finds the natural loop given a back edge.
     * The natural loop is the set of nodes that can reach the tail
     * without going through the header, plus the header itself.
     */
    private Set<Node> findNaturalLoop(Node header, Node tail) {
        Set<Node> loop = new HashSet<>();
        loop.add(header);
        
        if (!header.equals(tail)) {
            loop.add(tail);
            Stack<Node> stack = new Stack<>();
            stack.push(tail);
            
            while (!stack.isEmpty()) {
                Node node = stack.pop();
                for (Node pred : node.preds) {
                    if (!loop.contains(pred)) {
                        loop.add(pred);
                        stack.push(pred);
                    }
                }
            }
        }
        
        return loop;
    }

    /**
     * Detects break and continue edges within a loop.
     * - Break: edge from a loop body node to a node outside the loop
     * - Continue: edge from a loop body node (not the back-edge source) to the header
     */
    private void detectBreaksAndContinues(LoopStructure loop, Map<Node, Set<Node>> dominators) {
        for (Node node : loop.body) {
            for (Node succ : node.succs) {
                // Break: edge going outside the loop
                if (!loop.body.contains(succ)) {
                    loop.breaks.add(new BreakEdge(node, succ));
                }
                // Continue: edge to header that is not the main back-edge
                else if (succ.equals(loop.header) && !node.equals(loop.backEdgeSource)) {
                    loop.continues.add(new ContinueEdge(node, succ));
                }
            }
        }
    }

    /**
     * Generates a Graphviz/DOT representation of the CFG.
     * 
     * @return DOT format string representing the CFG
     */
    public String toGraphviz() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph {\n");
        
        for (Node node : allNodes) {
            for (Node succ : node.succs) {
                sb.append("  ").append(node.getLabel()).append("->").append(succ.getLabel()).append(";\n");
            }
        }
        
        sb.append("}");
        return sb.toString();
    }

    /**
     * Generates a pseudocode representation of the detected structures.
     * Outputs the control flow as structured pseudocode with if/else blocks and loops.
     * 
     * @return pseudocode string representing the detected structures
     */
    public String toPseudocode() {
        StringBuilder sb = new StringBuilder();
        Set<Node> visited = new HashSet<>();
        List<LoopStructure> loops = detectLoops();
        List<IfStructure> ifs = detectIfs();
        
        // Create lookup maps for quick access
        Map<Node, LoopStructure> loopHeaders = new HashMap<>();
        for (LoopStructure loop : loops) {
            // Only add the loop with the largest body for each header (handles nested loops)
            LoopStructure existing = loopHeaders.get(loop.header);
            if (existing == null || loop.body.size() > existing.body.size()) {
                loopHeaders.put(loop.header, loop);
            }
        }
        
        Map<Node, IfStructure> ifConditions = new HashMap<>();
        for (IfStructure ifStruct : ifs) {
            ifConditions.put(ifStruct.conditionNode, ifStruct);
        }
        
        generatePseudocode(entryNode, visited, sb, "", loopHeaders, ifConditions, null, null);
        
        return sb.toString();
    }

    private void generatePseudocode(Node node, Set<Node> visited, StringBuilder sb, String indent,
                                     Map<Node, LoopStructure> loopHeaders, Map<Node, IfStructure> ifConditions,
                                     LoopStructure currentLoop, Node stopAt) {
        if (node == null || visited.contains(node)) {
            return;
        }
        
        // Stop at merge node or loop exit
        if (stopAt != null && node.equals(stopAt)) {
            return;
        }
        
        visited.add(node);
        
        // Check if this is a loop header
        LoopStructure loop = loopHeaders.get(node);
        if (loop != null && currentLoop != loop) {
            sb.append(indent).append("while (").append(node.getLabel()).append(") {\n");
            
            // Find the node that continues the loop (not the exit)
            Node loopContinue = null;
            Node loopExit = null;
            for (Node succ : node.succs) {
                if (loop.body.contains(succ) && !succ.equals(node)) {
                    loopContinue = succ;
                } else if (!loop.body.contains(succ)) {
                    loopExit = succ;
                }
            }
            
            // Generate body of the loop
            if (loopContinue != null) {
                Set<Node> loopVisited = new HashSet<>();
                loopVisited.add(node); // Don't revisit header
                generatePseudocodeInLoop(loopContinue, loopVisited, sb, indent + "    ", loopHeaders, ifConditions, loop);
            }
            
            sb.append(indent).append("}\n");
            
            // Continue after the loop
            if (loopExit != null) {
                generatePseudocode(loopExit, visited, sb, indent, loopHeaders, ifConditions, currentLoop, stopAt);
            }
            return;
        }
        
        // Check if this is an if condition
        IfStructure ifStruct = ifConditions.get(node);
        if (ifStruct != null) {
            sb.append(indent).append("if (").append(node.getLabel()).append(") {\n");
            
            // Generate true branch
            Set<Node> trueVisited = new HashSet<>(visited);
            generatePseudocode(ifStruct.trueBranch, trueVisited, sb, indent + "    ", loopHeaders, ifConditions, currentLoop, ifStruct.mergeNode);
            
            sb.append(indent).append("} else {\n");
            
            // Generate false branch
            Set<Node> falseVisited = new HashSet<>(visited);
            generatePseudocode(ifStruct.falseBranch, falseVisited, sb, indent + "    ", loopHeaders, ifConditions, currentLoop, ifStruct.mergeNode);
            
            sb.append(indent).append("}\n");
            
            // Continue after merge
            if (ifStruct.mergeNode != null) {
                visited.addAll(trueVisited);
                visited.addAll(falseVisited);
                generatePseudocode(ifStruct.mergeNode, visited, sb, indent, loopHeaders, ifConditions, currentLoop, stopAt);
            }
            return;
        }
        
        // Regular node - just output it
        sb.append(indent).append(node.getLabel()).append(";\n");
        
        // Continue with successors
        for (Node succ : node.succs) {
            generatePseudocode(succ, visited, sb, indent, loopHeaders, ifConditions, currentLoop, stopAt);
        }
    }

    private void generatePseudocodeInLoop(Node node, Set<Node> visited, StringBuilder sb, String indent,
                                           Map<Node, LoopStructure> loopHeaders, Map<Node, IfStructure> ifConditions,
                                           LoopStructure currentLoop) {
        if (node == null || visited.contains(node)) {
            return;
        }
        
        // Don't go outside the loop
        if (!currentLoop.body.contains(node)) {
            return;
        }
        
        // Check for break
        for (BreakEdge breakEdge : currentLoop.breaks) {
            if (breakEdge.from.equals(node)) {
                // This node has a break
                IfStructure ifStruct = ifConditions.get(node);
                if (ifStruct != null) {
                    sb.append(indent).append("if (").append(node.getLabel()).append(") {\n");
                    
                    // Determine which branch is break
                    if (!currentLoop.body.contains(ifStruct.trueBranch)) {
                        sb.append(indent).append("    break;\n");
                        sb.append(indent).append("} else {\n");
                        Set<Node> elseVisited = new HashSet<>(visited);
                        elseVisited.add(node);
                        generatePseudocodeInLoop(ifStruct.falseBranch, elseVisited, sb, indent + "    ", loopHeaders, ifConditions, currentLoop);
                    } else {
                        Set<Node> thenVisited = new HashSet<>(visited);
                        thenVisited.add(node);
                        generatePseudocodeInLoop(ifStruct.trueBranch, thenVisited, sb, indent + "    ", loopHeaders, ifConditions, currentLoop);
                        sb.append(indent).append("} else {\n");
                        sb.append(indent).append("    break;\n");
                    }
                    sb.append(indent).append("}\n");
                    return;
                }
            }
        }
        
        // Check for continue
        for (ContinueEdge continueEdge : currentLoop.continues) {
            if (continueEdge.from.equals(node)) {
                IfStructure ifStruct = ifConditions.get(node);
                if (ifStruct != null) {
                    sb.append(indent).append("if (").append(node.getLabel()).append(") {\n");
                    
                    // Determine which branch is continue
                    if (ifStruct.trueBranch.equals(currentLoop.header)) {
                        sb.append(indent).append("    continue;\n");
                        sb.append(indent).append("} else {\n");
                        Set<Node> elseVisited = new HashSet<>(visited);
                        elseVisited.add(node);
                        generatePseudocodeInLoop(ifStruct.falseBranch, elseVisited, sb, indent + "    ", loopHeaders, ifConditions, currentLoop);
                    } else {
                        Set<Node> thenVisited = new HashSet<>(visited);
                        thenVisited.add(node);
                        generatePseudocodeInLoop(ifStruct.trueBranch, thenVisited, sb, indent + "    ", loopHeaders, ifConditions, currentLoop);
                        sb.append(indent).append("} else {\n");
                        sb.append(indent).append("    continue;\n");
                    }
                    sb.append(indent).append("}\n");
                    return;
                }
            }
        }
        
        visited.add(node);
        
        // Check if this is a nested loop header
        LoopStructure nestedLoop = loopHeaders.get(node);
        if (nestedLoop != null && nestedLoop != currentLoop) {
            sb.append(indent).append("while (").append(node.getLabel()).append(") {\n");
            
            Node loopContinue = null;
            Node loopExit = null;
            for (Node succ : node.succs) {
                if (nestedLoop.body.contains(succ) && !succ.equals(node)) {
                    loopContinue = succ;
                } else if (!nestedLoop.body.contains(succ)) {
                    loopExit = succ;
                }
            }
            
            if (loopContinue != null) {
                Set<Node> nestedVisited = new HashSet<>();
                nestedVisited.add(node);
                generatePseudocodeInLoop(loopContinue, nestedVisited, sb, indent + "    ", loopHeaders, ifConditions, nestedLoop);
            }
            
            sb.append(indent).append("}\n");
            
            if (loopExit != null && currentLoop.body.contains(loopExit)) {
                generatePseudocodeInLoop(loopExit, visited, sb, indent, loopHeaders, ifConditions, currentLoop);
            }
            return;
        }
        
        // Check if this is an if condition inside the loop
        IfStructure ifStruct = ifConditions.get(node);
        if (ifStruct != null) {
            sb.append(indent).append("if (").append(node.getLabel()).append(") {\n");
            
            Set<Node> trueVisited = new HashSet<>(visited);
            generatePseudocodeInLoop(ifStruct.trueBranch, trueVisited, sb, indent + "    ", loopHeaders, ifConditions, currentLoop);
            
            sb.append(indent).append("} else {\n");
            
            Set<Node> falseVisited = new HashSet<>(visited);
            generatePseudocodeInLoop(ifStruct.falseBranch, falseVisited, sb, indent + "    ", loopHeaders, ifConditions, currentLoop);
            
            sb.append(indent).append("}\n");
            
            if (ifStruct.mergeNode != null && currentLoop.body.contains(ifStruct.mergeNode)) {
                visited.addAll(trueVisited);
                visited.addAll(falseVisited);
                generatePseudocodeInLoop(ifStruct.mergeNode, visited, sb, indent, loopHeaders, ifConditions, currentLoop);
            }
            return;
        }
        
        // Regular node
        sb.append(indent).append(node.getLabel()).append(";\n");
        
        // Continue with successors inside the loop
        for (Node succ : node.succs) {
            if (currentLoop.body.contains(succ) && !succ.equals(currentLoop.header)) {
                generatePseudocodeInLoop(succ, visited, sb, indent, loopHeaders, ifConditions, currentLoop);
            }
        }
    }

    /**
     * Detects all control flow structures in the CFG.
     */
    public void analyze() {
        System.out.println("=== Control Flow Structure Analysis ===\n");
        
        System.out.println("Nodes in CFG: " + allNodes);
        System.out.println();
        
        List<IfStructure> ifs = detectIfs();
        System.out.println("Detected If Structures (" + ifs.size() + "):");
        for (IfStructure ifStruct : ifs) {
            System.out.println("  " + ifStruct);
        }
        System.out.println();
        
        List<LoopStructure> loops = detectLoops();
        System.out.println("Detected Loop Structures (" + loops.size() + "):");
        for (LoopStructure loop : loops) {
            System.out.println("  " + loop);
        }
    }

    /**
     * Demonstration with example CFGs.
     */
    public static void main(String[] args) {
        // Example 1: Simple if-else
        System.out.println("===== Example 1: Simple If-Else =====");
        Node.resetIdCounter();
        Node entry1 = new Node("entry");
        Node cond1 = new Node("if_cond");
        Node thenBlock = new Node("then");
        Node elseBlock = new Node("else");
        Node merge1 = new Node("merge");
        Node exit1 = new Node("exit");
        
        entry1.addSuccessor(cond1);
        cond1.addSuccessor(thenBlock);  // true branch
        cond1.addSuccessor(elseBlock);  // false branch
        thenBlock.addSuccessor(merge1);
        elseBlock.addSuccessor(merge1);
        merge1.addSuccessor(exit1);
        
        StructureDetector detector1 = new StructureDetector(entry1);
        detector1.analyze();
        System.out.println("\n--- Pseudocode ---");
        System.out.println(detector1.toPseudocode());
        System.out.println("--- Graphviz/DOT ---");
        System.out.println(detector1.toGraphviz());
        
        // Example 2: While loop
        System.out.println("\n===== Example 2: While Loop =====");
        Node.resetIdCounter();
        Node entry2 = new Node("entry");
        Node loopHeader = new Node("loop_header");
        Node loopBody = new Node("loop_body");
        Node exit2 = new Node("exit");
        
        entry2.addSuccessor(loopHeader);
        loopHeader.addSuccessor(loopBody);  // continue loop
        loopHeader.addSuccessor(exit2);     // exit loop
        loopBody.addSuccessor(loopHeader);  // back edge
        
        StructureDetector detector2 = new StructureDetector(entry2);
        detector2.analyze();
        System.out.println("\n--- Pseudocode ---");
        System.out.println(detector2.toPseudocode());
        System.out.println("--- Graphviz/DOT ---");
        System.out.println(detector2.toGraphviz());
        
        // Example 3: Loop with break and continue
        System.out.println("\n===== Example 3: Loop with Break and Continue =====");
        Node.resetIdCounter();
        Node entry3 = new Node("entry");
        Node header3 = new Node("loop_header");
        Node body3_1 = new Node("body_1");
        Node condBreak = new Node("cond_break");
        Node body3_2 = new Node("body_2");
        Node condContinue = new Node("cond_continue");
        Node body3_3 = new Node("body_3");
        Node exit3 = new Node("exit");
        
        entry3.addSuccessor(header3);
        header3.addSuccessor(body3_1);    // enter loop
        header3.addSuccessor(exit3);      // normal loop exit
        body3_1.addSuccessor(condBreak);
        condBreak.addSuccessor(body3_2);  // no break
        condBreak.addSuccessor(exit3);    // break!
        body3_2.addSuccessor(condContinue);
        condContinue.addSuccessor(body3_3);   // no continue
        condContinue.addSuccessor(header3);   // continue!
        body3_3.addSuccessor(header3);        // normal back edge
        
        StructureDetector detector3 = new StructureDetector(entry3);
        detector3.analyze();
        System.out.println("\n--- Pseudocode ---");
        System.out.println(detector3.toPseudocode());
        System.out.println("--- Graphviz/DOT ---");
        System.out.println(detector3.toGraphviz());
        
        // Example 4: Nested loops
        System.out.println("\n===== Example 4: Nested Loops =====");
        Node.resetIdCounter();
        Node entry4 = new Node("entry");
        Node outerHeader = new Node("outer_header");
        Node innerHeader = new Node("inner_header");
        Node innerBody = new Node("inner_body");
        Node outerEnd = new Node("outer_end");
        Node exit4 = new Node("exit");
        
        entry4.addSuccessor(outerHeader);
        outerHeader.addSuccessor(innerHeader);  // enter outer loop
        outerHeader.addSuccessor(exit4);        // exit outer loop
        innerHeader.addSuccessor(innerBody);    // enter inner loop
        innerHeader.addSuccessor(outerEnd);     // exit inner loop
        innerBody.addSuccessor(innerHeader);    // inner back edge
        outerEnd.addSuccessor(outerHeader);     // outer back edge
        
        StructureDetector detector4 = new StructureDetector(entry4);
        detector4.analyze();
        System.out.println("\n--- Pseudocode ---");
        System.out.println(detector4.toPseudocode());
        System.out.println("--- Graphviz/DOT ---");
        System.out.println(detector4.toGraphviz());
        
        // Example 5: If inside loop
        System.out.println("\n===== Example 5: If Inside Loop =====");
        Node.resetIdCounter();
        Node entry5 = new Node("entry");
        Node loopHead5 = new Node("loop_header");
        Node ifCond5 = new Node("if_cond");
        Node ifThen5 = new Node("if_then");
        Node ifElse5 = new Node("if_else");
        Node loopEnd5 = new Node("loop_end");
        Node exit5 = new Node("exit");
        
        entry5.addSuccessor(loopHead5);
        loopHead5.addSuccessor(ifCond5);   // enter loop
        loopHead5.addSuccessor(exit5);     // exit loop
        ifCond5.addSuccessor(ifThen5);     // if true
        ifCond5.addSuccessor(ifElse5);     // if false
        ifThen5.addSuccessor(loopEnd5);
        ifElse5.addSuccessor(loopEnd5);
        loopEnd5.addSuccessor(loopHead5);  // back edge
        
        StructureDetector detector5 = new StructureDetector(entry5);
        detector5.analyze();
        System.out.println("\n--- Pseudocode ---");
        System.out.println(detector5.toPseudocode());
        System.out.println("--- Graphviz/DOT ---");
        System.out.println(detector5.toGraphviz());
    }
}
