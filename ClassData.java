import java.util.*;

public class ClassData {
    public String name;

    public String parent;   // if no extends null

    // <field name, field type>, key: field name
    public LinkedHashMap<String, String> fields = new LinkedHashMap<>();

    // <method name, method data>, key: method name
    // a certain name can return more than one methods (overloading)
    public LinkedHashMap<String, List<MethData>> methods = new LinkedHashMap<>();

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

    // give current method
    // overloading: same name, 
    // overriding: same name, same return type, same arguments typs for ALL
    public void addMethod(MethData currentMethod) throws Exception {
        // see if it is method overloading
        // if name + all parameter types are the same it's not overloading and it's illegal
        String name = currentMethod.name;
        if(methods.containsKey(name)) {     // same name
            List<MethData> lm = methods.get(name);
            outerloop:
            for(MethData m : lm) {      // need to check for all methods with same name
                if (m.paramTypes.size() != currentMethod.paramTypes.size()) break;      // different sizes are always allowed
                for (int i = 0; i < m.paramTypes.size(); i++) {     // need to check for all the parameters as well
                    if (!m.paramTypes.get(i).equals(currentMethod.paramTypes.get(i))) {     // if at least one of them is different its overloading
                        // add method on the list
                        this.methods.get(name).add(currentMethod);
                        break outerloop;
                    }
                }
                throw new Exception(
                    "Method " + name +
                    " already defined in class " +
                    this.name
                );
            }
        }
        // check for overriding

    }

    
}