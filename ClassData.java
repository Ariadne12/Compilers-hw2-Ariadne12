import java.util.*;

public class ClassData {
    public String name;

    public String parent;   // if no extends null

    // <field name, field type>, key: field name
    public LinkedHashMap<String, String> fields = new LinkedHashMap<>();

    // <method name, method data>, key: method name
    // a certain name can return more than one methods (overloading)
    public LinkedHashMap<String, List<MethData>> methods = new LinkedHashMap<>();

    int finalFieldOffset;
    int finalMethodOffset;
    public TreeMap<Integer, String> FieldOffsets = new TreeMap<>();
    public TreeMap<Integer, String> MethodOffsets = new TreeMap<>();

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

    // overloading: same name, 
    // overriding: same name, same return type, same arguments typs for ALL
    public void addMethod(MethData currentMethod, boolean overload) throws Exception {
        // if it already exists
        if (overload) {
            methods.get(currentMethod.name).add(currentMethod);
        }
        else {
            // add new entry on the method hashmap
            List<MethData> newml = new ArrayList<MethData>();
            newml.add(currentMethod);
            methods.put(currentMethod.name, newml);
        }
    }
}