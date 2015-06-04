/* Generated by JTB 1.4.4 */
package main.java.syntaxtree;

import main.java.visitor.IRetArguVisitor;
import main.java.visitor.IRetVisitor;
import main.java.visitor.IVoidArguVisitor;
import main.java.visitor.IVoidVisitor;

public class Goal implements INode {

    private static final long serialVersionUID = 144L;
    public NodeToken f0;
    public StmtList f1;
    public NodeToken f2;
    public NodeListOptional f3;
    public NodeToken f4;

    public Goal(final NodeToken n0, final StmtList n1, final NodeToken n2, final NodeListOptional n3, final NodeToken n4) {
        f0 = n0;
        f1 = n1;
        f2 = n2;
        f3 = n3;
        f4 = n4;
    }

    public Goal(final StmtList n0, final NodeListOptional n1) {
        f0 = new NodeToken("MAIN");
        f1 = n0;
        f2 = new NodeToken("END");
        f3 = n1;
        f4 = new NodeToken("");
    }

    public <R, A> R accept(final IRetArguVisitor<R, A> vis, final A argu) {
        return vis.visit(this, argu);
    }

    public <R> R accept(final IRetVisitor<R> vis) {
        return vis.visit(this);
    }

    public <A> void accept(final IVoidArguVisitor<A> vis, final A argu) {
        vis.visit(this, argu);
    }

    public void accept(final IVoidVisitor vis) {
        vis.visit(this);
    }

}
