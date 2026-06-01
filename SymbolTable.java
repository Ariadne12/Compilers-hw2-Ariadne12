import java.util.*;

public class SymbolTable {
    public Map<String, ClassData> classes;

    public SymbolTable() {
        classes = new LinkedHashMap<>();
    }

    public ClassData findClass(String className) {
        return classes.get(className);
    }

    public MethData findMethod(MethData targetMethod, ClassData currentClass) {
        ClassData c = currentClass;
        String methodName = targetMethod.name;
        while (c != null) {
            if (c.methods.containsKey(methodName)) {
                List<MethData> lm = c.methods.get(methodName);
                outer:
                for(MethData m : lm) {
                    if (m.paramTypes.size() != targetMethod.paramTypes.size()) continue;
                    for (int i = 0; i < m.paramTypes.size(); i++) {
                        // argument type must be subtype of parameter type
                        if (!isSubtype(targetMethod.paramTypes.get(i), m.paramTypes.get(i)))
                            continue outer;
                    }
                    return m;
                }
            }
            if (c.parent == null)
                break;
            c = classes.get(c.parent);
        }
        return null;
    }

    public String findVar(String name, MethData currentMethod, ClassData currentClass) {
        // inside the method
        // local variables
        if (currentMethod != null &&
            currentMethod.localVars.containsKey(name))
            return currentMethod.localVars.get(name);

        // parameters
        if (currentMethod != null) {
            for (int i = 0; i < currentMethod.paramNames.size(); i++) {
                if (currentMethod.paramNames.get(i).equals(name))
                    return currentMethod.paramTypes.get(i);
            }
        }

        // outside the method
        // fields + inherited fields
        ClassData c = currentClass;
        while (c != null) {
            if (c.fields.containsKey(name))
                return c.fields.get(name);
            if (c.parent == null)
                break;
            c = classes.get(c.parent);
        }
        return null;
    }

    public boolean isSubtype(String child, String parent) {
        if(child.equals(parent))
            return true;

        ClassData c = classes.get(child);
        while(c != null) {
            if(c.parent == null)
                return false;

            if(c.parent.equals(parent))
                return true;

            c = classes.get(c.parent);
        }
        return false;
    }

    public boolean compareMethods(MethData currentMethod, MethData other, boolean subtypeCheck) {
        if (!currentMethod.name.equals(other.name))
            return false;
        if (currentMethod.paramTypes.size() != other.paramTypes.size())
            return false;
        for (int i = 0; i < currentMethod.paramTypes.size(); i++) {
            if (!currentMethod.paramTypes.get(i).equals(other.paramTypes.get(i))){
                if (subtypeCheck == true) {
                    // if it isnt sub or supertype either
                    if ((this.isSubtype(other.paramTypes.get(i), currentMethod.paramTypes.get(i)) == false) &&
                        (this.isSubtype(currentMethod.paramTypes.get(i), other.paramTypes.get(i)) == false)) 
                        return false;
                }
                else return false;
            }
        }
        return true;
    }

    // if name + all parameter types are the same it's not overloading and it's illegal
    // same also counts for subtype-supertype pairs in parameter types
    public boolean validateOverload(ClassData currentClass, MethData currentMethod) throws Exception {
        String name = currentMethod.name;
        if(currentClass.methods.containsKey(name)) {     // same name
            List<MethData> lm = currentClass.methods.get(name);
            for(MethData m : lm) {      // need to check for all methods with same name
                if(compareMethods(currentMethod, m, true)){
                //if(currentMethod.compareMethods(m)){
                    throw new Exception(
                        "Method " + name + "(" + currentMethod.getParameterString() + ")" +
                        " already defined in class " +
                        currentClass.name + " (check for sub/supertypes)"
                    );
                }
            }
            System.err.println("overload");
            return true;
        }
        else {
            return false;
        }
    }

    public void validateOverride(ClassData currentClass, MethData currentMethod) throws Exception {
        if (currentClass.parent == null) return;
        ClassData ancestor = classes.get(currentClass.parent);
        while (ancestor.name != null) {
            List<MethData> sameNameMethods = ancestor.methods.get(currentMethod.name);
            if (sameNameMethods != null) {
                for (MethData ancestorMethod : sameNameMethods) {
                    if (compareMethods(currentMethod, ancestorMethod, false)) {
                        // found override candidate
                        if (!ancestorMethod.returnType.equals(currentMethod.returnType)) {
                            throw new Exception(
                                "Invalid override of method "
                                + currentMethod.name
                                + " in class "
                                + currentClass.name
                                + ". Return type differs from ancestor."
                            );
                        }
                        // valid override
                        currentMethod.overrideBit = true;
                        return;
                    }
                }
            }
            if (ancestor.parent == null)
                break;
            ancestor = classes.get(ancestor.parent);
        }
    }
}