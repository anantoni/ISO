/* Generated by JTB 1.4.4 */
package syntaxtree;

import visitor.*;

public class StmtExp implements INode {

  public NodeToken f0;

  public StmtList f1;

  public NodeToken f2;

  public SimpleExp f3;

  public NodeToken f4;

  private static final long serialVersionUID = 144L;

  public StmtExp(final NodeToken n0, final StmtList n1, final NodeToken n2, final SimpleExp n3, final NodeToken n4) {
    f0 = n0;
    f1 = n1;
    f2 = n2;
    f3 = n3;
    f4 = n4;
  }

  public StmtExp(final StmtList n0, final SimpleExp n1) {
    f0 = new NodeToken("BEGIN");
    f1 = n0;
    f2 = new NodeToken("RETURN");
    f3 = n1;
    f4 = new NodeToken("END");
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
