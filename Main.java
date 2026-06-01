import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

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
                System.err.println("checking..." + args[0]);
                MiniJavaParser parser = new MiniJavaParser(fis);

                // Begins with syntax tree's root
                Goal root = parser.Goal();

                // New visitor creation
                SymbolTableVisitor eval1 = new SymbolTableVisitor();
                root.accept(eval1, null);

                System.err.println("done with storing");
                print_offsets(eval1);

                // New visitor creation
                System.err.println("Now doing type check");
                TypeCheckingVisitor eval2 = new TypeCheckingVisitor(eval1.symbolTable);
                root.accept(eval2, null);
                System.err.println("Accepted!!!!");
            }
            catch(ParseException ex){
                System.out.println(ex.getMessage());
            }
            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage());
            }
            catch(Exception ex){    // for the actual semantic exceptions
                System.out.println(ex.getMessage());
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

    public static void print_offsets(SymbolTableVisitor v) {
        for (String className : v.symbolTable.classes.keySet()) {
            ClassData c = v.symbolTable.classes.get(className);
            for (Integer off : c.FieldOffsets.keySet()) {
                System.out.println(className + "." + c.FieldOffsets.get(off) + " : " + off);
            }
            for (Integer off : c.MethodOffsets.keySet()) {
                System.out.println(className + "." + c.MethodOffsets.get(off) + " : " + off);
            }
            System.out.println();
        }
    }

    public static void debug_print(SymbolTableVisitor v) {
        for (String className : v.symbolTable.classes.keySet()) {
            ClassData c = v.symbolTable.classes.get(className);
            System.out.println("this class has " + c.methods.size() + "methods");

            System.out.println("Class: " + className);

            if (c.parent != null)
                System.out.println("  extends: " + c.parent);

            System.out.println("  Fields:");
            for (String f : c.fields.keySet()) {
                System.out.println("    " + f + " : " + c.fields.get(f));
            }

            System.out.println("  Methods:");
            for (String m : c.methods.keySet()) {
                List<MethData> lm = c.methods.get(m);
                for(MethData mi : lm) {
                    System.out.print("    " + mi.returnType + " " + m + "(");
                    for (int i = 0; i < mi.paramTypes.size(); i++) {
                        System.out.print(mi.paramTypes.get(i) + " " + mi.paramNames.get(i));
                        if (i < mi.paramTypes.size() - 1) System.out.print(", ");
                    }
                    System.out.println(")");
                }
            }
            System.out.println();
        }
    }
}