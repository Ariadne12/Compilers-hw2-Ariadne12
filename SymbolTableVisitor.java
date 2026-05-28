import java.util.*;

import syntaxtree.*;
import visitor.*;


class SymbolTableVisitor extends GJDepthFirst<String, Void>{
    public LinkedHashMap<String, ClassData> classes = new LinkedHashMap<>();

    private ClassData currentClass = null;
    private MethData currentMethod = null;
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
        classes.put(classname, currentClass);

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
        n.f0.accept(this, argu);    // just visits token
        
        String classname = n.f1.accept(this, argu);     // get class name
        System.out.println("Normal! Class: " + classname);

        currentClass = new ClassData(classname);
        classes.put(classname, currentClass);

        n.f2.accept(this, argu);
        System.out.println("Fields: ");
        n.f3.accept(this, argu);    // Visit all fields
        System.out.println("Methods: ");
        n.f4.accept(this, argu);    // Visit all methods
        n.f5.accept(this, argu);

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

        classes.put(classname, currentClass);

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
        String varname = n.f1.accept(this, argu);
        System.out.println(varname + " " + type);

        if (currentMethod != null) {
            // inside method: local variables
            currentMethod.localVars.put(varname, type);
        } 
        else if (currentClass != null) {
            // inside class: fields
            currentClass.fields.put(varname, type);
        }

        super.visit(n, argu);   //?
        
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
        // f4.present() checks if it exists (parameters for the method)
        String paramsList = n.f4.present() ? n.f4.accept(this, null) : "";
        
        String returnType = n.f1.accept(this, null);
        String methodName = n.f2.accept(this, null);

        currentMethod = new MethData(methodName, returnType);
        currentClass.methods.put(methodName, currentMethod);

        System.out.println("Method: " + returnType + " " + methodName + " (" + paramsList + ")");
        System.out.println("Local vars:");

        super.visit(n, argu);   // Traverse inside method
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

        if (currentMethod != null) {
            currentMethod.paramTypes.add(type);
            currentMethod.paramNames.add(name);
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