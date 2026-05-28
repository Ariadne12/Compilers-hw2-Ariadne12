import java.util.*;

public class MethData {
    public String name;


    public ArrayList<String> paramTypes = new ArrayList<String>();
    public ArrayList<String> paramNames = new ArrayList<String>();
    
    public LinkedHashMap<String, String> localVars = new LinkedHashMap<>();
    // public ArrayList<String> localVars = new ArrayList<String>();

    public String returnType;

    // Constructor
    public MethData(String name, String returnType) {
        this.name = name;
        this.returnType = returnType;
    }
}