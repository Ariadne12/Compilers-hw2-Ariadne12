import java.util.*;

public class ClassData {
    public String name;

    public String parent;   // if no extends null

    // <field name, field type>, key: field name
    public LinkedHashMap<String, String> fields = new LinkedHashMap<>();

    // <method name, method data>, key: method name
    public LinkedHashMap<String, MethData> methods = new LinkedHashMap<>();

    public ClassData(String name) {
        this.name = name;
        this.parent = null;
    }

    public void addField(String name, String type) throws Exception {
        if(fields.containsKey(name))
            throw new Exception(
                "Field " + name +
                " already defined in class " +
                this.name
            );
        fields.put(name, type);
    }

    public void addMethod(String name, String returnType) throws Exception {
        // see if it is method overloading
        // if name + type + all parameter types are the same it's not overloading and it's illegal
        if(methods.containsKey(name)) {
            MethData m = methods.get(name);     // the method that will 
            if(m.returnType.equals(returnType)) {
                for (int i = 0; i < m.paramNames.size(); i++) {
                    if (m.paramNames.get(i).equals(name)) {
                        throw new Exception(
                            "Variable " + name +
                            " already defined in method " +
                            this.name
                        );
                    }
                }
            }
            throw new Exception(
                "Field " + name +
                " already defined in class " +
                this.name
            );
        
        }
    }
}