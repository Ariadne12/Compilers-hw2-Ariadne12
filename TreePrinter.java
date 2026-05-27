import syntaxtree.*;
import visitor.*;

public class TreePrinter extends DepthFirstVisitor {
    private int depth = 0;

    private void indent() {
        for (int i = 0; i < depth; i++) System.out.print("  ");
    }

    @Override
    public void visit(NodeToken n) {
        indent();
        System.out.println("Token: " + n.tokenImage);
    }

    public void visit(NodeSequence n) {
        indent();
        System.out.println("NodeSequence");
        depth++;
        super.visit(n);
        depth--;
    }
    // Override other visit methods as needed to label your specific nodes
}