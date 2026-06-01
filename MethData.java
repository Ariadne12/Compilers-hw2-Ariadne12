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

    public String getParameterString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paramTypes.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(paramTypes.get(i)).append(" ").append(paramNames.get(i));
        }
        return sb.toString();
    }
}