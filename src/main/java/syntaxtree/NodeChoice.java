/* Generated by JTB 1.4.4 */
package main.java.syntaxtree;

import main.java.visitor.IRetArguVisitor;
import main.java.visitor.IRetVisitor;
import main.java.visitor.IVoidArguVisitor;
import main.java.visitor.IVoidVisitor;

public class NodeChoice implements INode {

    private static final long serialVersionUID = 144L;
    public INode choice;
    public int which;
    public int total;

    public NodeChoice(final INode node) {
        this(node, -1, -1);
    }

    public NodeChoice(final INode node, final int whichChoice, final int totalChoices) {
        choice = node;
        which = whichChoice;
        total = totalChoices;
    }

    public <R, A> R accept(final IRetArguVisitor<R, A> vis, final A argu) {
        return choice.accept(vis, argu);
    }

    public <R> R accept(final IRetVisitor<R> vis) {
        return choice.accept(vis);
    }

    public <A> void accept(final IVoidArguVisitor<A> vis, final A argu) {
        choice.accept(vis, argu);
    }

    public void accept(final IVoidVisitor vis) {
        choice.accept(vis);
    }

}
