import java.util.*;

public class SymbolTable {
    public Map<String, ClassData> classes;

    public SymbolTable() {
        classes = new LinkedHashMap<>();
    }

    public ClassData findClass(String className) {
        return classes.get(className);
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
}