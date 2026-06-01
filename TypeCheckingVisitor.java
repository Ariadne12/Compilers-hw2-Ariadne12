import java.util.ArrayList;
import java.util.List;

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

        currentClass = symbolTable.findClass(classname);
        super.visit(n, argu);

        System.out.println();
        currentClass = null;

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
        String classname = n.f1.accept(this, argu);
        System.out.println("Class: " + classname);

        currentClass = symbolTable.findClass(classname);
        System.err.println(currentClass.fields);
        System.out.println("Fields: ");
        n.f3.accept(this, argu);
        System.out.println("Methods: ");
        n.f4.accept(this, argu);

        System.out.println();
        currentClass = null;
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

        currentClass = symbolTable.findClass(classname);

        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        System.out.println("Fields: ");
        n.f5.accept(this, argu);
        System.out.println("Methods: ");
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);

        System.out.println();
        currentClass = null;
        return null;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public String visit(VarDeclaration n, Void argu) throws Exception {
        String type = n.f0.accept(this, argu);
        String var = n.f1.accept(this, argu);
        System.err.println("passing from var decls" + var);
        super.visit(n, argu);
        
        return type;
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
        String returnType = n.f1.accept(this, null);
        String methodName = n.f2.accept(this, null);
        currentMethod = new MethData(methodName, returnType);

        String paramsList = n.f4.present() ? n.f4.accept(this, null) : "";

        System.out.println("Method: " + returnType + " " + methodName + " (" + paramsList + ")");
        System.out.println("Local vars:");

        currentMethod = symbolTable.findMethod(currentMethod, currentClass);

        n.f7.accept(this, argu); // local vars
        n.f8.accept(this, argu); // statements

        String retExprType = n.f10.accept(this, argu);
        // need to check if what is returned is actually compatible with what the method says it will
        if (!returnType.equals(retExprType)) {
            throw new Exception(
                retExprType + " not compatible with return type " + returnType + " of " + methodName
            );
        }

        currentMethod = null;
        return null;
    }

    //visit(Statement n, Void argu)

    //visit(Block n, Void argu)

    // MessageSend
    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    @Override
    public String visit(MessageSend n, Void argu) throws Exception {
        String receiverType = n.f0.accept(this, null);

        // Receiver must be an already established class type
        ClassData receiverClass = symbolTable.findClass(receiverType);
        if (receiverClass == null)
            throw new Exception(
                "Non-class type: " + receiverType + " cannot be derefernced"
            );

        String messageMethodName = n.f2.accept(this, null);
        // all info not necessary for our cause (like the return type of the method) 
        // will be left null
        MethData messageMethod = new MethData(messageMethodName, null);

        // Collect the argument types (if any)
        if (n.f4.present()) {
            // ExpressionList returns "type1,type2,..." so we will need to divide them manually
            String paramString = n.f4.accept(this, null);
            if (!paramString.equals("")){       // temp sol
                for (String t : paramString.split(",")) {
                    // again keeping the name of the parameters null since they do not matter in this instance
                    messageMethod.addParam(null, t.trim());
                }
            }
        }

        MethData found = symbolTable.findMethod(messageMethod, currentClass);

        if (found == null)
            throw new Exception(
                "No matching method " + messageMethodName +
                " found in class " + receiverType +
                " (or any inherited parent classes of " + receiverType + 
                ") for argument types " + messageMethod.paramTypes
            );

        // Return the method's return type to know what to expect
        return found.returnType;
    }
    
    // parameters
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
        for (syntaxtree.Node node: n.f0.nodes) {
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

        if (currentMethod != null) {
            // check if we have seen this parameter before in this method
            currentMethod.addParam(name, type);
        }
        return type + " " + name;
    }

    // expressions
    // they  must strictly return types correctly
    // collect argument types into a list
    // Returns a comma-separated string of types, e.g. "int,boolean,Foo"
    /**
     Similarly to parameters structure before we got:
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    @Override
    public String visit(ExpressionList n, Void argu) throws Exception {
        String ret = n.f0.accept(this, null);
        if (n.f1 != null) {
            ret += n.f1.accept(this, null);   // ExpressionTail returns ",type,type..."
        }
            return ret;
    }

     /**
     * f0 -> ","
     * f1 -> Expression()
     */
    @Override
    public String visit(ExpressionTerm n, Void argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    // zero or more additional arguments
    /**
     * f0 -> ( ExpressionTerm() )*
     */
    @Override
    public String visit(ExpressionTail n, Void argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += ", " + node.accept(this, null);  // each ExpressionTerm is before a ","
        }
        return ret;
    }
    
    /**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | PrimaryExpression()
     */
    @Override
    public String visit(Expression n, Void argu) throws Exception {
        return n.f0.accept(this, null);
    }

    // returns type
    /**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | NotExpression()
     *       | BracketExpression()
     */
    @Override
    public String visit(PrimaryExpression n, Void argu) throws Exception {
        if (n.f0.choice instanceof Identifier) {
            String name = ((Identifier) n.f0.choice).f0.toString();
            String type = symbolTable.findVar(name, currentMethod, currentClass);
            if (type == null)
                throw new Exception("Undeclared variable: " + name);
            return type;
        }
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
    @Override
    public String visit(Statement n, Void argu) throws Exception {
        return n.f0.accept(this, null);
    }

    @Override
    public String visit(IfStatement n, Void argu) throws Exception {
        String condType = n.f2.accept(this, null);

        if (!condType.equals("boolean"))
            throw new Exception(
                "If condition must be boolean. Found " + condType
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
                "While condition must be boolean. Found " + condType
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
                "println expects int. Found " + exprType
            );

        return null;
    }

    // assignment
    // Finally, the assignment A a = new B(); when B extends A is correct, 
    // and the same applies when a method expects a parameter of type A and a B instance is given instead
    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    @Override
    public String visit(AssignmentStatement n, Void argu) throws Exception{
        String varName = n.f0.accept(this, null);
        System.err.println("meth" + currentMethod.name);
        String varType = symbolTable.findVar(varName, currentMethod, currentClass);
        System.err.println("left: " + varType);
        // not sure
        if (varType == null)
            throw new Exception(
                "Variable " + varName + " not declared"
            );

        String exprType = n.f2.accept(this, null);
        System.err.println("right: " + exprType);
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
                arrName + " is not an int[]. Found" + arrType
            );

        String indexType = n.f2.accept(this, null);

        if(!indexType.equals("int"))
            throw new Exception(
                "Array index must be int. Found " + indexType
            );

        String assignedValueType = n.f5.accept(this, null);

        if(!assignedValueType.equals("int"))
            throw new Exception(
                "Array element must be int. Found " + assignedValueType
            );

        return null;
    }

    // array details
    // need to put index in limmits
    @Override
    public String visit(ArrayLookup n, Void argu) throws Exception {
        String arrayType = n.f0.accept(this, null);

        if(!arrayType.equals("int[]"))
            throw new Exception(
                "Array lookup on non-array type. Found " + arrayType
            );

        String indexType = n.f2.accept(this, null);

        if(!indexType.equals("int"))
            throw new Exception(
                "Array index must be int. Found " + indexType
            );

        return "int";
    }

    @Override
    public String visit(ArrayLength n, Void argu) throws Exception {
        String arrayType = n.f0.accept(this, null);

        if(!arrayType.equals("int[]"))
            throw new Exception(
                ".length applied to non-array. Found" + arrayType
            );

        return "int";
    }

    // arithmetic operations
    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(PlusExpression n, Void argu) throws Exception{
        String left = n.f0.accept(this, null);
        String right = n.f2.accept(this, null);
        System.err.println("so um left operand: " + left + " and right op: " + right);

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
        return "boolean";
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
    public String visit(ArrayType n, Void argu) {
        return "int[]";
    }

    @Override
    public String visit(BooleanType n, Void argu) {
        return "boolean";
    }

    @Override
    public String visit(IntegerType n, Void argu) {
        return "int";
    }

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