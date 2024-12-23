import java.io.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a "Simple" program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a sequence (for nodes that may have a variable number of children)
// or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     Subclass            Kids
//     --------            ----
//     ProgramNode         IdNode, ClassBodyNode
//     ClassBodyNode       DeclListNode
//     DeclListNode        sequence of DeclNode
//     FormalsListNode     sequence of FormalDeclNode
//     MethodBodyNode      DeclListNode, StmtListNode
//     StmtListNode        sequence of StmtNode
//     ExpListNode         sequence of ExpNode
//
//     DeclNode:
//       FieldDeclNode     TypeNode, IdNode
//       VarDeclNode       TypeNode, IdNode
//       MethodDeclNode    IdNode, FormalsListNode, MethodBodyNode
//       FormalDeclNode    TypeNode, IdNode
//
//     TypeNode:
//       IntNode             -- none --
//       BooleanNode         -- none --
//       StringNode          -- none --
//
//     StmtNode:
//       PrintStmtNode       ExpNode
//       AssignStmtNode      IdNode, ExpNode
//       IfStmtNode          ExpNode, StmtListNode
//       IfElseStmtNode      ExpNode, StmtListNode, StmtListNode
//       WhileStmtNode       ExpNode, StmtListNode
//       CallStmtNode        IdNode, ExpListNode
//       ReturnStmtNode      -- none --
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with sequences of kids, or internal
// nodes with a fixed number of kids:
//
// (1) Leaf nodes:
//        IntNode, BooleanNode, StringNode, IntLitNode,
//	  StrLitNode, TrueNode, FalseNode, IdNode, ReturnStmtNode
//
// (2) Internal nodes with (possibly empty) sequences of children:
//        DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of kids:
//        ProgramNode,    ClassBodyNode, MethodBodyNode,
//        FieldDeclNode,  VarDeclNode,   MethodDeclNode, FormalDeclNode,
//        PrintStmtNode,  AssignStmtNode,IfStmtNode,     IfElseStmtNode,
//        WhileStmtNode,  CallStmtNode,  UnaryExpNode,   BinaryExpNode,
//        UnaryMinusNode, NotNode,       PlusNode,       MinusNode,
//        TimesNode,      DivideNode,    AndNode,        OrNode,
//        EqualsNode,     NotEqualsNode, LessNode,       GreaterNode,
//        LessEqNode,     GreaterEqNode
//
// **********************************************************************

// **********************************************************************
// ASTnode class (base class for all other kinds of nodes)
// **********************************************************************
abstract class ASTnode { 
    // every subclass must provide an decompile operation
    abstract public void decompile(PrintWriter p, int indent);

    // this method can be used by the decompile methods to do indenting
    protected void doIndent(PrintWriter p, int indent) {
	for (int k=0; k<indent; k++) p.print(" ");
    }
}

// **********************************************************************
// ProgramNode, ClassBodyNode, DeclListNode, FormalsListNode,
// MethodBodyNode, StmtListNode, ExpListNode
// **********************************************************************
class ProgramNode extends ASTnode {
    public ProgramNode(IdNode id, ClassBodyNode classBody) {
	myId = id;
	myClassBody = classBody;
    }

    public void decompile(PrintWriter p, int indent) {
	p.print("public class ");
	myId.decompile(p, 0);
	p.println(" {");
	myClassBody.decompile(p, 2);
	p.println("}");
    }

    // 2 kids
    private IdNode myId;
    private ClassBodyNode myClassBody;
}

class ClassBodyNode extends ASTnode {
    public ClassBodyNode(DeclListNode declList) {
	myDeclList = declList;
    }

    public void decompile(PrintWriter p, int indent) {
	myDeclList.decompile(p, indent);
    }

    // 1 kid
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(Sequence S) {
	myDecls = S;
    }

    public void decompile(PrintWriter p, int indent) {
	try {
	    for (myDecls.start(); myDecls.isCurrent(); myDecls.advance()) {
		((DeclNode)myDecls.getCurrent()).decompile(p, indent);
	    }
	} catch (NoCurrentException ex) {
	    System.err.println("unexpected NoCurrentException in DeclListNode.print");
	    System.exit(-1);
	}
    }

  // sequence of kids (DeclNodes)
  private Sequence myDecls;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(Sequence S) {
	myFormals = S;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(");
        int counter = 0;
        try {
	        for (myFormals.start(); myFormals.isCurrent(); myFormals.advance()) {
                counter++;
		        ((DeclNode)myFormals.getCurrent()).decompile(p, indent);
                if(counter < myFormals.length()){
                    p.print(", ");
                }
	        }
	    } catch (NoCurrentException ex) {
	        System.err.println("unexpected NoCurrentException in FormalsListNode.print");
	        System.exit(-1);
	    }
        p.print(")");
    }

  // sequence of kids (FormalDeclNodes)
    private Sequence myFormals;
}

class MethodBodyNode extends ASTnode {
    public MethodBodyNode(DeclListNode declList, StmtListNode stmtList) {
	myDeclList = declList;
	myStmtList = stmtList;
    }

    public void decompile(PrintWriter p, int indent) {
        myDeclList.decompile(p, indent);
        myStmtList.decompile(p, indent);
    }

    // 2 kids
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class StmtListNode extends ASTnode {
    public StmtListNode(Sequence S) {
	myStmts = S;
    }

    public void decompile(PrintWriter p, int indent) {
        try {
	        for (myStmts.start(); myStmts.isCurrent(); myStmts.advance()) {
		    ((StmtNode)myStmts.getCurrent()).decompile(p, indent);
	        }
	    } catch (NoCurrentException ex) {
	        System.err.println("unexpected NoCurrentException in StmtListNode.print");
	        System.exit(-1);
	    }
    }

    // sequence of kids (StmtNodes)
    private Sequence myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(Sequence S) {
	myExps = S;
    }
    public void decompile(PrintWriter p, int indent) {
        int counter = 0;
        try {
	        for (myExps.start(); myExps.isCurrent(); myExps.advance()) {
            counter++;
		    ((ExpNode)myExps.getCurrent()).decompile(p, indent);
            if(counter < myExps.length()){
                    p.print(", ");
            }
	        }
	    } catch (NoCurrentException ex) {
	        System.err.println("unexpected NoCurrentException in ExpListNode.print");
	        System.exit(-1);
	    }
    }

    // sequence of kids (ExpNodes)
    private Sequence myExps;
}

class SwitchBodyNode extends ASTnode{
    public SwitchBodyNode(Sequence S){
        myCases = S;
    }

    public void decompile(PrintWriter p, int indent) {
        try {
	        for (myCases.start(); myCases.isCurrent(); myCases.advance()) {
		    ((DeclNode)myCases.getCurrent()).decompile(p, indent);
	        }
	    } catch (NoCurrentException ex) {
	        System.err.println("unexpected NoCurrentException in StmtListNode.print");
	        System.exit(-1);
	    }
    }

    private Sequence myCases;
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************
abstract class DeclNode extends ASTnode
{
}

class FieldDeclNode extends DeclNode {
    public FieldDeclNode(TypeNode type, IdNode id) {
	myType = type;
	myId = id;
    }
    public void decompile(PrintWriter p, int indent) {
	doIndent(p, indent);
	p.print("static ");
	myType.decompile(p, 0);
	p.print(" ");
	myId.decompile(p, 0);
	p.println(";");
    }

    // 2 kids
    private TypeNode myType;
    private IdNode myId;
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id) {
	myType = type;
	myId = id;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
	    myType.decompile(p, 0);
	    p.print(" ");
	    myId.decompile(p, 0);
	    p.println(";");
    }

    // 2 kids
    private TypeNode myType;
    private IdNode myId;
}

class MethodDeclNode extends DeclNode {
    public MethodDeclNode(TypeNode type, IdNode id, FormalsListNode formalList,
			  MethodBodyNode body) {
    myType = type;
	myId = id;
	myFormalsList = formalList;
	myBody = body;
    }

    public MethodDeclNode(IdNode id, FormalsListNode formalList,
			  MethodBodyNode body) {
    myType = null;
	myId = id;
	myFormalsList = formalList;
	myBody = body;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
	    p.print("public static ");
        if(myType == null){
            p.print("void");
        }
        else{
            myType.decompile(p, 0);
        }
        p.print(" ");
	    myId.decompile(p, 0);
        myFormalsList.decompile(p, indent);
	    p.println("{");
        myBody.decompile(p,indent+2);
        doIndent(p, indent);
        p.println("}");

    }

    // 3 kids
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private MethodBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
	myType = type;
	myId = id;
    }

    public void decompile(PrintWriter p, int indent) {
	    myType.decompile(p, 0);
	    p.print(" ");
	    myId.decompile(p, 0);
    }

    // 2 kids
    private TypeNode myType;
    private IdNode myId;
}

class CaseDeclNode extends DeclNode {
    public CaseDeclNode(ExpNode exp, DeclListNode declList, StmtListNode stmtList) {
    myExp = exp;
	myDeclList = declList;
	myStmtList = stmtList;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("case ");
        myExp.decompile(p,0);
        p.println(":");
        myDeclList.decompile(p, indent+2);
        myStmtList.decompile(p, indent+2);
    }

    // 2 kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class DefaultDeclNode extends DeclNode {
    public DefaultDeclNode(DeclListNode declList, StmtListNode stmtList) {  
	myDeclList = declList;
	myStmtList = stmtList;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.println("default:");
        myDeclList.decompile(p, indent+2);
        myStmtList.decompile(p, indent+2);
    }

    // 2 kids
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************
abstract class TypeNode extends ASTnode {
}

class IntNode extends TypeNode
{
    public IntNode() {
    }

    public void decompile(PrintWriter p, int indent) {
	p.print("int");
    }
}

class BooleanNode extends TypeNode
{
    public BooleanNode() {
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("boolean");
    }
}

class StringNode extends TypeNode
{
    public StringNode() {
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("String");
    }
}

// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
}

class PrintStmtNode extends StmtNode {
    public PrintStmtNode(ExpNode exp) {
	myExp = exp;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("System.out.println(");
        myExp.decompile(p,0);
        p.println(");");
    }

    // 1 kid
    private ExpNode myExp;
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(IdNode id, ExpNode exp) {
	myId = id;
	myExp = exp;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        myId.decompile(p,0);
        p.print(" = ");
        myExp.decompile(p,0);
        p.println(";");
    }

    // 2 kids
    private IdNode myId;
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, StmtListNode slist) {
	myExp = exp;
	myStmtList = slist;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if(");
        myExp.decompile(p,0);
        p.println("){");
        myStmtList.decompile(p,indent+2);
        doIndent(p, indent);
        p.println("}");
    }

    // 2 kids
    private ExpNode myExp;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, StmtListNode slist1,
			  StmtListNode slist2) {
	myExp = exp;
	myThenStmtList = slist1;
	myElseStmtList = slist2;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if(");
        myExp.decompile(p,indent);
        p.println("){");
        myThenStmtList.decompile(p,indent+2);
        doIndent(p, indent);
        p.println("}");
        doIndent(p, indent);
        p.println("else{");
        myElseStmtList.decompile(p,indent+2);
        doIndent(p, indent);
        p.println("}");
    }

    // 3 kids
    private ExpNode myExp;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, StmtListNode slist) {
	myExp = exp;
	myStmtList = slist;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.println("do{");
        myStmtList.decompile(p,indent+2);
        //System.out.println();
        doIndent(p, indent);
        p.print("}while(");
        myExp.decompile(p,indent);
        p.println(")");
    }

    // 2 kids
    private ExpNode myExp;
    private StmtListNode myStmtList;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(IdNode id, ExpListNode elist) {
	myId = id;
	myExpList = elist;
    }

    public CallStmtNode(IdNode id) {
	myId = id;
	myExpList = new ExpListNode(new Sequence());
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        myId.decompile(p,0);
        p.print("(");
        myExpList.decompile(p,0);
        p.println(");");
    }

    // 2 kids
    private IdNode myId;
    private ExpListNode myExpList;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode() {
    }

    public ReturnStmtNode(ExpNode exp){
        myExp = exp;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("return");
        if(myExp != null){
            p.print(" ");
            myExp.decompile(p,indent);
        }
        p.println(";");
    }

    private ExpNode myExp;
}

class NestedBlockNode extends StmtNode {
    public NestedBlockNode(DeclListNode declList, StmtListNode stmtList) {
	myDeclList = declList;
	myStmtList = stmtList;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.println("{");
        myDeclList.decompile(p, indent+2);
        myStmtList.decompile(p, indent+2);
        doIndent(p, indent);
        p.println("}");
    }

    // 2 kids
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class SwitchStmtNode extends StmtNode {
    public SwitchStmtNode(ExpNode e){
        myExp = e;
    }

    public SwitchStmtNode(ExpNode e, SwitchBodyNode sb){
        myExp = e;
        mySwitchBody = sb;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("switch(");
        myExp.decompile(p,0);
        p.println("){");
        mySwitchBody.decompile(p,indent+2);
        doIndent(p, indent);
        p.println("}");

    }

    private ExpNode myExp;
    private SwitchBodyNode mySwitchBody;
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int colNum, int intVal) {
	myLineNum = lineNum;
	myColNum = colNum;
	myIntVal = intVal;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    private int myLineNum;
    private int myColNum;
    private int myIntVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int colNum, String strVal) {
	myLineNum = lineNum;
	myColNum = colNum;
	myStrVal = strVal;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("\"");
        p.print(myStrVal);
        p.print("\"");
    }

    private int myLineNum;
    private int myColNum;
    private String myStrVal;
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int colNum) {
	myLineNum = lineNum;
	myColNum = colNum;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("true");
    }

    private int myLineNum;
    private int myColNum;
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int colNum) {
	myLineNum = lineNum;
	myColNum = colNum;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("false");
    }

    private int myLineNum;
    private int myColNum;
}

class IdNode extends ExpNode
{
    public IdNode(int lineNum, int charNum, String strVal) {
	myLineNum = lineNum;
	myCharNum = charNum;
	myStrVal = strVal;
    }

    public void decompile(PrintWriter p, int indent) {
	p.print(myStrVal);
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
	myExp = exp;
    }

    // one child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode
{
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
	myExp1 = exp1;
	myExp2 = exp2;
    }

    // two kids
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode id, ExpListNode elist) {
	myId = id;
	myExpList = elist;
    }

    public CallExpNode(IdNode id) {
	myId = id;
	myExpList = new ExpListNode(new Sequence());
    }

    public void decompile(PrintWriter p, int indent) {
        myId.decompile(p,indent);
        p.print("(");
        myExpList.decompile(p,indent);
        p.println(");");
    }

    // 2 kids
    private IdNode myId;
    private ExpListNode myExpList;
}

// **********************************************************************
// Subclasses of UnaryExpNode
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode
{
    public UnaryMinusNode(ExpNode exp) {
	super(exp);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(");
        p.print("-");
        myExp.decompile(p,indent);
        p.print(")");
    }
}

class NotNode extends UnaryExpNode
{
    public NotNode(ExpNode exp) {
	super(exp);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(");
        p.print("!");
        myExp.decompile(p,indent);
        p.print(")");
    }
}

// **********************************************************************
// Subclasses of BinaryExpNode
// **********************************************************************

class PlusNode extends BinaryExpNode
{
    public PlusNode(ExpNode exp1, ExpNode exp2) {
	super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(");
        myExp1.decompile(p,indent);
        p.print(" + ");
        myExp2.decompile(p,indent);
        p.print(")");
    }
}

class MinusNode extends BinaryExpNode
{
    public MinusNode(ExpNode exp1, ExpNode exp2) {
	super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(");
        myExp1.decompile(p,indent);
        p.print(" - ");
        myExp2.decompile(p,indent);
        p.print(")");
    }
}

class TimesNode extends BinaryExpNode
{
    public TimesNode(ExpNode exp1, ExpNode exp2) {
	super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(");
        myExp1.decompile(p,indent);
        p.print(" * ");
        myExp2.decompile(p,indent);
        p.print(")");
    }
}

class DivideNode extends BinaryExpNode
{
    public DivideNode(ExpNode exp1, ExpNode exp2) {
	super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(");
        myExp1.decompile(p,indent);
        p.print(" / ");
        myExp2.decompile(p,indent);
        p.print(")");
    }
}

class PowerNode extends BinaryExpNode
{
    public PowerNode(ExpNode exp1, ExpNode exp2){
        super(exp1,exp2);
    }

    public void decompile(PrintWriter p, int indent){
        p.print("(");
        myExp1.decompile(p,indent);
        p.print(" ** ");
        myExp2.decompile(p,indent);
        p.print(")");

    }
}

class AndNode extends BinaryExpNode
{
    public AndNode(ExpNode exp1, ExpNode exp2) {
	super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(");
        myExp1.decompile(p,indent);
        p.print(" && ");
        myExp2.decompile(p,indent);
        p.print(")");
    }
}

class OrNode extends BinaryExpNode
{
    public OrNode(ExpNode exp1, ExpNode exp2) {
	super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(");
        myExp1.decompile(p,indent);
        p.print(" || ");
        myExp2.decompile(p,indent);
        p.print(")");
    }
}

class EqualsNode extends BinaryExpNode
{
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
	super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(");
        myExp1.decompile(p,indent);
        p.print(" == ");
        myExp2.decompile(p,indent);
        p.print(")");
    }
}

class NotEqualsNode extends BinaryExpNode
{
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
	super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(");
        myExp1.decompile(p,indent);
        p.print(" != ");
        myExp2.decompile(p,indent);
        p.print(")");
    }
}

class LessNode extends BinaryExpNode
{
    public LessNode(ExpNode exp1, ExpNode exp2) {
	super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(");
        myExp1.decompile(p,indent);
        p.print(" < ");
        myExp2.decompile(p,indent);
        p.print(")");
    }
}

class GreaterNode extends BinaryExpNode
{
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
	super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(");
        myExp1.decompile(p,indent);
        p.print(" > ");
        myExp2.decompile(p,indent);
        p.print(")");
    }
}

class LessEqNode extends BinaryExpNode
{
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
	super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(");
        myExp1.decompile(p,indent);
        p.print(" <= ");
        myExp2.decompile(p,indent);
        p.print(")");
    }
}

class GreaterEqNode extends BinaryExpNode
{
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
	super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(");
        myExp1.decompile(p,indent);
        p.print(" >= ");
        myExp2.decompile(p,indent);
        p.print(")");
    }
}
