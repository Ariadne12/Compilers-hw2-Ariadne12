import syntaxtree.*;
import visitor.*;

class SymbolTableVisitor extends GJDepthFirst<String, Void>{
    SymbolTable symbolTable = new SymbolTable();

    private ClassData currentClass = null;
    private MethData currentMethod = null;

    // may keep
    private int currentFieldOffset = 0;
    private int currentMethodOffset = 0;

    /**
    * int = 4 bytes
    * boolean = 1 byte
    * pointers (int[], custom objects) = 8 bytes
    */
    public int getTypeSize(String type) {
        if ("int".equals(type)) {
            return 4;
        }
        else if ("boolean".equals(type)) {
            return 1;
        }
        else {
            return 8;
        }
    }

    /**
     * Helper method
     * initializes offsets for the current class
     * If the class extends a parent, it inherits the parent's next available offsets
     */
    private void initializeOffsets(ClassData currentClass) {
        // find ancestors of class with same method. so see if override
        String parentName = currentClass.parent;
        if (parentName != null) {
            // Start where the parent left off
            ClassData parentClass = symbolTable.classes.get(parentName);
            currentFieldOffset = parentClass.finalFieldOffset;
            currentMethodOffset = parentClass.finalMethodOffset;
        } 
        else {
            // Base class starts at 0
            currentFieldOffset = 0;
            currentMethodOffset = 0;
        }
    }

    /**
     * Helper method
     * saves the final offsets when we finish visiting a class
     */
    private void finalizeOffsets() {
        if (currentClass != null) {
            currentClass.finalFieldOffset = currentFieldOffset;
            currentClass.finalMethodOffset = currentMethodOffset;
        }
        currentClass = null;
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

        currentClass = new ClassData(classname);
        symbolTable.classes.put(classname, currentClass);

        initializeOffsets(currentClass);

        super.visit(n, argu);

        finalizeOffsets();
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
        n.f0.accept(this, argu);    // just visits token
        
        String classname = n.f1.accept(this, argu);     // get class name
        System.out.println("Normal! Class: " + classname);

        currentClass = new ClassData(classname);
        symbolTable.classes.put(classname, currentClass);

        // Base class: no parent
        initializeOffsets(currentClass);

        n.f2.accept(this, argu);
        System.out.println("Fields: ");
        n.f3.accept(this, argu);    // Visit all fields
        System.out.println("Methods: ");
        n.f4.accept(this, argu);    // Visit all methods
        n.f5.accept(this, argu);

        finalizeOffsets();
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
    // same as the previous but with inheritance
    @Override
    public String visit(ClassExtendsDeclaration n, Void argu) throws Exception {
        n.f0.accept(this, argu);

        String classname = n.f1.accept(this, null);
        System.out.println("Inherited! Class: " + classname);

        n.f2.accept(this, argu);
        String parentname = n.f3.accept(this, argu);    // f3, identifier is from parent class, since it is after f2:extends
        
        currentClass = new ClassData(classname);
        currentClass.parent = parentname;

        symbolTable.classes.put(classname, currentClass);

        // Derived class: will inherit offsets
        initializeOffsets(currentClass);

        n.f4.accept(this, argu);
        System.out.println("Fields: ");
        n.f5.accept(this, argu);
        System.out.println("Methods: ");
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);

        finalizeOffsets();
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
        String varname = n.f1.accept(this, argu);
        System.out.println(varname + " " + type);

        if (currentMethod != null) {
            // inside method: local variables
            // check if we have seen this variable before
            currentMethod.addLocal(varname, type);
        } 
        else if (currentClass != null) {
            // inside class: fields
            // check if we have seen this field before
            currentClass.addField(varname, type);
            // update offset
            currentClass.FieldOffsets.put(currentFieldOffset, varname);
            currentFieldOffset += getTypeSize(type);
        }

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
        String returnType = n.f1.accept(this, null);
        String methodName = n.f2.accept(this, null);

        currentMethod = new MethData(methodName, returnType);

        // f4.present() checks if it exists (parameters for the method)
        String paramsList = n.f4.present() ? n.f4.accept(this, null) : "";

        // see if it is method overloading
        boolean overload = false;
        if (symbolTable.validateOverload(currentClass, currentMethod)) overload = true;
        symbolTable.validateOverride(currentClass, currentMethod);
        // if none of them have poped an exception, it is a valid method (in terms of either overloading or overriding)
        // so we can add it
        currentClass.addMethod(currentMethod, overload);

        // Methods take up 8 bytes in the vtable
        System.out.println("  Method: " + methodName + " : offset " + currentMethodOffset);
        if (currentMethod.overrideBit == false) {
            currentClass.MethodOffsets.put(currentMethodOffset, methodName);
            currentMethodOffset += 8;
        }
        
        System.out.println("Method: " + returnType + " " + methodName + " (" + paramsList + ")");
        System.out.println("Local vars:");

        n.f7.accept(this, argu); // local vars
        n.f8.accept(this, argu); // statements
        currentMethod = null;
        return null;
    }

    // PARAMETERS

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

    // TYPES
    // Convert AST nodes to readable strings
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

    /**
    * f0 -> <IDENTIFIER>
    */
    @Override
    public String visit(Identifier n, Void argu) {
        return n.f0.toString();     // Extract actual name (variable, class, method)
    }
}