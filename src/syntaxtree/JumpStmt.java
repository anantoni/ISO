/* Generated by JTB 1.4.4 */
package syntaxtree;

import visitor.*;

public class JumpStmt implements INode {

  public NodeToken f0;

  public Label f1;

  private static final long serialVersionUID = 144L;

  public JumpStmt(final NodeToken n0, final Label n1) {
    f0 = n0;
    f1 = n1;
  }

  public JumpStmt(final Label n0) {
    f0 = new NodeToken("JUMP");
    f1 = n0;
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
