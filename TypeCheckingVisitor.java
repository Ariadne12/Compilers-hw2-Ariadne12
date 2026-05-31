import syntaxtree.*;
import visitor.*;


class TypeCheckingVisitor extends GJDepthFirst<String, Void>{
    private SymbolTable symbolTable;
    private ClassData currentClass = null;
    private MethData currentMethod = null;

    // constructor
    public TypeCheckingVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }
    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    @Override
    public String visit(MainClass n, Void argu) throws Exception {
        String classname = n.f1.accept(this, null);
        System.out.println("Class: " + classname);

        super.visit(n, argu);

        System.out.println();

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        
        String classname = n.f1.accept(this, argu);
        System.out.println("Class: " + classname);

        n.f2.accept(this, argu);
        System.out.println("Fields: ");
        n.f3.accept(this, argu);
        System.out.println("Methods: ");
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);

        System.out.println();

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, Void argu) throws Exception {
        n.f0.accept(this, argu);

        String classname = n.f1.accept(this, null);
        System.out.println("Class: " + classname);

        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        System.out.println("Fields: ");
        n.f5.accept(this, argu);
        System.out.println("Methods: ");
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);

        System.out.println();

        return null;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public String visit(VarDeclaration n, Void argu) throws Exception {
        String _ret=null;
        String type = n.f0.accept(this, argu);
        String var = n.f1.accept(this, argu);
        System.out.println(var + " " + type);
        super.visit(n, argu);
        
        return _ret;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    @Override
    public String visit(MethodDeclaration n, Void argu) throws Exception {
        String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";

        String myType = n.f1.accept(this, null);
        String myName = n.f2.accept(this, null);

        System.out.println("Method: " + myType + " " + myName + " (" + argumentList + ")");
        System.out.println("Local vars:");

        super.visit(n, argu);
        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, Void argu) throws Exception {
        String ret = n.f0.accept(this, null);

        if (n.f1 != null) {
            ret += n.f1.accept(this, null);
        }

        return ret;
    }

    /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */
    public String visit(FormalParameterTerm n, Void argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
    * f0 -> ( FormalParameterTerm() )*
    */
    @Override
    public String visit(FormalParameterTail n, Void argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += ", " + node.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, Void argu) throws Exception{
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);
        return type + " " + name;
    }

    // expressions
    // they  must strictly return types correctly
    @Override
    public String visit(Expression n, Void argu) throws Exception {
        return n.f0.accept(this, null);
    }

    @Override
    public String visit(PrimaryExpression n, Void argu) throws Exception {
        return n.f0.accept(this, null);
    }

    // parentheses don't change type, so we keep as is
    @Override
    public String visit(BracketExpression n, Void argu) throws Exception {
        return n.f1.accept(this, null);
    }

    // this
    @Override
    public String visit(ThisExpression n, Void argu) {
        return currentClass.name;
    }

    // allocation
    @Override
    public String visit(AllocationExpression n, Void argu) throws Exception {
        String className = n.f1.accept(this, null);

        if (!symbolTable.classes.containsKey(className))
            throw new Exception(
                "Unknown class " + className);

        return className;
    }

    @Override
    public String visit(ArrayAllocationExpression n, Void argu) throws Exception {
        String sizeType = n.f3.accept(this, null);

        if (!sizeType.equals("int"))
            throw new Exception(
                "Array size must be int");

        return "int[]";
    }

    // statements

    // assignment
    // Finally, the assignment A a = new B(); when B extends A is correct, 
    // and the same applies when a method expects a parameter of type A and a B instance is given instead
    @Override
    public String visit(AssignmentStatement n, Void argu) throws Exception{
        String varName = n.f0.accept(this, null);
        String varType = symbolTable.findVar(varName, currentMethod, currentClass);
        
        // not sure
        if (varType == null)
            throw new Exception(
                "Variable " + varName + " not declared"
            );

        String exprType = n.f2.accept(this, null);

        if (!symbolTable.isSubtype(exprType, varType)) {
            throw new RuntimeException(
                "Incompatible types in assignment");
        }

        return null;
    }

    @Override
    public String visit(ArrayAssignmentStatement n, Void argu) throws Exception {
        String arrName = n.f0.accept(this, null);
        String arrType = symbolTable.findVar(arrName, currentMethod, currentClass);

        if(arrType == null)
            throw new Exception(
                "Variable " + arrName + " has not been declared"
            );

        if(!arrType.equals("int[]"))
            throw new Exception(
                arrName + " is not an int[]"
            );

        String indexType = n.f2.accept(this, null);

        if(!indexType.equals("int"))
            throw new Exception(
                "Array index must be int"
            );

        String assignedValueType = n.f5.accept(this, null);

        if(!assignedValueType.equals("int"))
            throw new Exception(
                "Array element must be int"
            );

        return null;
    }

    // statements
    @Override
    public String visit(IfStatement n, Void argu) throws Exception {
        String condType = n.f2.accept(this, null);

        if (!condType.equals("boolean"))
            throw new Exception(
                "If condition must be boolean"
            );

        n.f4.accept(this, null);
        n.f6.accept(this, null);

        return null;
    }

    // same limitations
    @Override
    public String visit(WhileStatement n, Void argu) throws Exception {
        String condType = n.f2.accept(this, null);

        if (!condType.equals("boolean"))
            throw new Exception(
                "While condition must be boolean"
            );

        n.f4.accept(this, null);

        return null;
    }

    // minijava accepts only int in prints
    @Override
    public String visit(PrintStatement n, Void argu) throws Exception {
        String exprType = n.f2.accept(this, null);

        if (!exprType.equals("int"))
            throw new Exception(
                "println expects int"
            );

        return null;
    }

    // arithmetic operations
    @Override
    public String visit(PlusExpression n, Void argu) throws Exception{
        String left = n.f0.accept(this, null);
        String right = n.f2.accept(this, null);

        if (!left.equals("int") || !right.equals("int")) {
            throw new RuntimeException("Bad operand types for binary operator '+'. Must be between integers");
        }
        return "int";
    }

    @Override
    public String visit(MinusExpression n, Void argu) throws Exception{
        String left = n.f0.accept(this, null);
        String right = n.f2.accept(this, null);

        if (!left.equals("int") || !right.equals("int")) {
            throw new RuntimeException("Bad operand types for binary operator '-'. Must be between integers");
        }
        return "int";
    }

    @Override
    public String visit(TimesExpression n, Void argu) throws Exception{
        String left = n.f0.accept(this, null);
        String right = n.f2.accept(this, null);

        if (!left.equals("int") || !right.equals("int")) {
            throw new RuntimeException("Bad operand types for binary operator '*'. Must be between integers");
        }
        return "int";
    }

    @Override
    public String visit(CompareExpression n, Void argu) throws Exception{
        String left = n.f0.accept(this, null);
        String right = n.f2.accept(this, null);

        if (!left.equals("int") || !right.equals("int")) {
            throw new RuntimeException("Bad operand types for binary operator '<'. Must be between integers");
        }
        return "int";
    }

    // logic operations
    @Override
    public String visit(AndExpression n, Void argu) throws Exception{
        String left = n.f0.accept(this, null);
        String right = n.f2.accept(this, null);

        if (!left.equals("boolean") || !right.equals("boolean")) {
            throw new RuntimeException("Bad operand types for binary operator '&&'. Must be between booleans");
        }
        return "boolean";
    }

    @Override
    public String visit(NotExpression n, Void argu) throws Exception{
        String primExpr = n.f1.accept(this, null);

        if (!primExpr.equals("boolean")) {
            throw new RuntimeException("Bad operand type for unary operator '!'. Must be boolean");
        }
        return "boolean";
    }

    // types
    @Override
    public String visit(TrueLiteral n, Void argu) {
        return "boolean";
    }

    @Override
    public String visit(FalseLiteral n, Void argu) {
        return "boolean";
    }

    @Override
    public String visit(IntegerLiteral n, Void argu) {
        return "int";
    }

    // just return the name
    @Override
    public String visit(Identifier n, Void argu) {
        return n.f0.toString();
    }
}