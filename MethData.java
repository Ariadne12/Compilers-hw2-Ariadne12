import java.util.*;

public class MethData {
    public String name;


    public ArrayList<String> paramTypes = new ArrayList<String>();
    public ArrayList<String> paramNames = new ArrayList<String>();
    
    // <var name, var type>, key: var name
    public LinkedHashMap<String, String> localVars = new LinkedHashMap<>();

    public String returnType;

    // Constructor
    public MethData(String name, String returnType) {
        this.name = name;
        this.returnType = returnType;
    }

    public void addLocal(String name, String type) throws Exception {
        if(localVars.containsKey(name))
            throw new Exception(
                "Variable " + name +
                " already defined in method " +
                this.name
            );

        localVars.put(name, type);
    }

    public void addParam(String name, String type) throws Exception {
        for (int i = 0; i < paramNames.size(); i++) {
            if (paramNames.get(i).equals(name)) {
                throw new Exception(
                    "Variable " + name +
                    " already defined in method " +
                    this.name
                );
            }
        }
        paramNames.add(name);
        paramTypes.add(type);
    }
}