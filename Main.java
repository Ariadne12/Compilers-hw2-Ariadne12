import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import syntaxtree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length == 0){
            System.err.println("Usage: java Main <inputFile1> <inputFile2> ... <inputFileN>");
            System.exit(1);
        }

        FileInputStream fis = null;
        for (int i = 0; i < args.length; i++) {
            try {
                fis = new FileInputStream(args[0]);
                MiniJavaParser parser = new MiniJavaParser(fis);

                // Begins with syntax tree's root
                Goal root = parser.Goal();

                System.err.println("Program parsed successfully.");

                // New visitor creation
                SymbolTableVisitor eval1 = new SymbolTableVisitor();
                root.accept(eval1, null);

                // debugging
                // System.out.println(v.classes.keySet());
                // for (c in classes) {
                //     System.out.println(c.fields);
                // }

                // New visitor creation
                TypeCheckingVisitor eval2 = new TypeCheckingVisitor();
                root.accept(eval2, null);
            }
            catch(ParseException ex){
                System.out.println(ex.getMessage());
            }
            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage());
            }
            finally{
                try{
                    if(fis != null) fis.close();
                }
                catch(IOException ex){
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
}