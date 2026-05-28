import java.util.*;

public class ClassData {
    public String name;

    public String parent;   // null if no extends

    // dk yet what structure to use
    // arraylist vs linked hash map??
    public ArrayList<String> fields = new ArrayList<String>();
    public ArrayList<MethData> methods = new ArrayList<MethData>();

    public ClassData(String name) {
        this.name = name;
        this.parent = null;
    }
}