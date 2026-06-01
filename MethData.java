import java.util.*;

public class MethData {
    public String name;

    public ArrayList<String> paramTypes = new ArrayList<String>();
    public ArrayList<String> paramNames = new ArrayList<String>();
    
    // <var name, var type>, key: var name
    public LinkedHashMap<String, String> localVars = new LinkedHashMap<>();

    public String returnType;

    public boolean overrideBit;

    // Constructor
    public MethData(String name, String returnType) {
        this.name = name;
        this.returnType = returnType;
        this.overrideBit = false;
    }

    public void addLocal(String name, String type) throws Exception {
        localVars.put(name, type);
    }

    public void addParam(String name, String type) throws Exception {
        for (int i = 0; i < paramNames.size(); i++) {
            if (paramNames.get(i).equals(name)) {
                throw new Exception(
                    "Variable " + name +
                    " already defined in method " +
                    this.name + " (in parameters)"
                );
            }
        }
        paramNames.add(name);
        paramTypes.add(type);
    }

    // true -> same name, same types on all parameters
    public boolean compareMethods(MethData other) {
        if (!name.equals(other.name))
            return false;
        if (paramTypes.size() != other.paramTypes.size())
            return false;
        for (int i = 0; i < paramTypes.size(); i++) {
            if (!paramTypes.get(i).equals(other.paramTypes.get(i)))
                return false;
        }
        return true;
    }
}