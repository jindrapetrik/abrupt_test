# Vibe decompiler

This project is an attempt to implement a general decompiler - or, more precisely,
a control-flow graph structure detector - developed with extensive use of AI
(Copilot, agents) during its implementation.

The project is still a work in progress...

## Usage

### Basic Usage

```java
import com.jpexs.decompiler.vibe.StructureDetector;

// Create a StructureDetector from Graphviz/DOT format
String dot = "digraph { start->a; a->b; a->c; b->d; c->d; d->end; }";
StructureDetector detector = StructureDetector.fromGraphviz(dot);

// Generate pseudocode
String pseudocode = detector.toPseudocode();
System.out.println(pseudocode);

// Or get the structured statement list for further processing
List<Statement> statements = detector.toStatementList();
```

### With Exception Handling (Try-Catch)

```java
String dot = "digraph {\n" +
    "start->trybody;\n" +
    "trybody->end;\n" +
    "catchbody->end;\n" +
    "}";

StructureDetector detector = StructureDetector.fromGraphviz(dot);

// Define exception ranges: tryNodes => catchNodes
detector.parseExceptions("trybody => catchbody");

String pseudocode = detector.toPseudocode();
// Output:
// start;
// try {
//     trybody;
// } catch(0) {
//     catchbody;
// }
// end;
```

### Public API

#### Factory Methods

- `StructureDetector.fromGraphviz(String dot)` - Creates a detector from Graphviz/DOT format

#### Constructor

- `StructureDetector(Node entryNode)` - Creates a detector with the given entry node

#### Configuration Methods

- `parseExceptions(String exceptionDef)` - Parses exception definitions in format `"tryNodes => catchNodes; ..."`
- `addException(Set<Node> tryNodes, Set<Node> catchNodes)` - Adds a try-catch structure programmatically
- `addLabeledBlock(String label, Node startNode, Node endNode)` - Registers a labeled block structure

#### Output Methods

- `toPseudocode()` - Generates pseudocode string representation
- `toStatementList()` - Returns structured `List<Statement>` for further processing
- `toGraphviz()` - Generates Graphviz/DOT format string

#### Analysis Methods

- `analyze()` - Prints detected control flow structures to stdout
- `detectIfs()` - Returns `List<IfStructure>` of detected if statements
- `detectLoops()` - Returns `List<LoopStructure>` of detected loops
- `getTryStructures()` - Returns `List<TryStructure>` of registered exception handlers
- `getLabeledBlocks()` - Returns `List<LabeledBlockStructure>` of registered labeled blocks

#### Accessor Methods

- `getEntryNode()` - Returns the entry node of the CFG

### Running Examples

```bash
ant run
```

## Author
Jindra Petřík aka JPEXS giving prompts to agents...