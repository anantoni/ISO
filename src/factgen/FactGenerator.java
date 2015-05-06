package factgen;

import syntaxtree.*;
import visitor.DepthFirstRetArguVisitor;
import visitor.IRetArguVisitor;
import visitor.IVoidArguVisitor;

import java.util.*;

/**
 * Created by anantoni on 1/5/2015.
 */
public class FactGenerator extends DepthFirstRetArguVisitor<String, String> implements IRetArguVisitor<String, String> {
    private int instructionCounter;
    private IVoidArguVisitor<String> useVarFactGen;
    private IVoidArguVisitor<String> defVarFactGen;
    private IVoidArguVisitor<String> instructionHasLabelGen;
    private Map<String, Set<String>> methodVarsMap;

    public FactGenerator() {
        useVarFactGen = new UseVarFactGen(this);
        defVarFactGen = new DefVarFactGen(this);
        instructionHasLabelGen = new InstructionHasLabelGen(this);
        instructionCounter = 0;
        methodVarsMap = new HashMap<>();
    }

    public int getInstructionCounter() {
        return instructionCounter;
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        return true;
    }

    public String visit(final NodeChoice n, final String argu) {

        final String nRes = n.choice.accept(this, argu);
        return nRes;
    }

    public String visit(final NodeList n, final String argu) {
        String nRes = null;
        for (final Iterator<INode> e = n.elements(); e.hasNext();) {
            @SuppressWarnings("unused")
            final String sRes = e.next().accept(this, argu);
        }
        return nRes;
    }

    public String visit(final NodeListOptional n, final String argu) {
        if (n.present()) {
            String nRes = null;
            int counter = 0;

            for (final Iterator<INode> e = n.elements(); e.hasNext();) {
                String sRes = e.next().accept(this, argu);

                if (counter == 0)
                    nRes = sRes;
                else
                    nRes += " " + sRes;
                counter++;
            }
            return nRes;
        } else
            return null;
    }

    public String visit(final NodeOptional n, final String argu) {
        if (n.present()) {
            final String nRes = n.node.accept(this, argu);
            return nRes;
        } else
            return null;
    }

    public String visit(final NodeSequence n, final String argu) {
        String nRes = null;
        for (final Iterator<INode> e = n.elements(); e.hasNext();) {
            INode next = e.next();
            String subRet = next.accept(this, argu);
            next.accept(this.instructionHasLabelGen, argu);
        }
        return nRes;
    }

    public String visit(final NodeToken n, @SuppressWarnings("unused") final String argu) {
        String nRes = null;
        @SuppressWarnings("unused")
        final String tkIm = n.tokenImage;
        return nRes;
    }

    public String visit(final Goal n, String argu) {
        String nRes = null;
        argu = "MAIN";
        methodVarsMap.put(argu, new HashSet<>());
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return nRes;
    }

    public String visit(final StmtList n, final String argu) {
        String nRes = null;
        n.f0.accept(this, argu);

        return nRes;
    }

    public String visit(final Procedure n, String argu) {
        String nRes = null;
        argu = n.f0.accept(this, argu);
        methodVarsMap.put(argu, new HashSet<>());
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return nRes;
    }

    public String visit(final Stmt n, final String argu) {
        String nRes = null;
        n.f0.accept(this, argu);
        return nRes;
    }

    public String visit(final NoOpStmt n, final String argu) {
        n.f0.accept(this, argu);

        String instructionEDB = "instruction(\'" + argu + "\'," + this.instructionCounter++ + ",\'NOOP\').";
        System.out.println(instructionEDB);

        return n.f0.toString();
    }

    public String visit(final ErrorStmt n, final String argu) {
        n.f0.accept(this, argu);

        String instructionEDB = "instruction(\'" + argu + "\'," + this.instructionCounter++ + ",\'ERROR\').";
        System.out.println(instructionEDB);
        return n.f0.toString();
    }

    public String visit(final CJumpStmt n, final String argu) {
        String instructionLiteral = n.f0.toString();

        n.f0.accept(this, argu);
        instructionLiteral += " " + n.f1.accept(this, argu);
        n.f1.accept(this.useVarFactGen, argu);
        String label = n.f2.accept(this, argu);
        instructionLiteral += " " + label;

        String instructionCJumpsToLabelEDB = "instructionCJumpsToLabel(\'" + argu + "\'," + instructionCounter + ",\'" + label + "\').";
        System.out.println(instructionCJumpsToLabelEDB);

        String instructionEDB = "instruction(\'" + argu + "\'," + this.instructionCounter++ + ",\'" + instructionLiteral + "\').";
        System.out.println(instructionEDB);
        return instructionLiteral;
    }

    public String visit(final JumpStmt n, final String argu) {
        String instructionLiteral = n.f0.toString();

        n.f0.accept(this, argu);
        String label = n.f1.accept(this, argu);
        instructionLiteral += " " + label;

        String instructionCJumpsToLabelEDB = "instructionJumpsToLabel(\'" + argu + "\'," + instructionCounter + ",\'" + label + "\').";
        System.out.println(instructionCJumpsToLabelEDB);

        String instructionEDB = "instruction(\'" + argu + "\'," + this.instructionCounter++ + ",\'"  + instructionLiteral + "\').";
        System.out.println(instructionEDB);
        return instructionLiteral;
    }

    public String visit(final HStoreStmt n, final String argu) {
        String instructionLiteral = n.f0.toString();

        n.f0.accept(this, argu);
        instructionLiteral += " " + n.f1.accept(this, argu);
        n.f1.accept(this.useVarFactGen, argu);

        instructionLiteral += " " + n.f2.accept(this, argu);
        instructionLiteral += n.f3.accept(this, argu);
        n.f3.accept(this.useVarFactGen, argu);

        String instructionEDB = "instruction(\'" + argu + "\'," + this.instructionCounter++ + ",\'" +  instructionLiteral + "\').";
        System.out.println(instructionEDB);

        return instructionLiteral;
    }

    public String visit(final HLoadStmt n, final String argu) {
        String instructionLiteral = n.f0.toString();

        n.f0.accept(this, argu);
        instructionLiteral += " " + n.f1.accept(this, argu);
        n.f1.accept(this.defVarFactGen, argu);

        instructionLiteral += " " + n.f2.accept(this, argu);
        n.f2.accept(this.useVarFactGen, argu);
        instructionLiteral += " " + n.f3.accept(this, argu);

        String instructionEDB = "instruction(\'" + argu + "\'," + instructionCounter++ + ",\'" + instructionLiteral + "\').";
        System.out.println(instructionEDB);

        return instructionLiteral;
    }

    public String visit(final MoveStmt n, final String argu) {
        String instructionLiteral = n.f0.toString();

        n.f0.accept(this, argu);
        String varDecl = n.f1.accept(this, argu);
        instructionLiteral += " " + varDecl;
        n.f1.accept(this.defVarFactGen, argu);

        String simpleExp = n.f2.accept(this, argu);
        instructionLiteral += " " + simpleExp;
        if (simpleExp.startsWith("TEMP")) {
            String varMoveEDB = "varMove(\'" + argu + "\'," + this.instructionCounter + ",\'" + simpleExp + "\').";
            System.out.println(varMoveEDB);
        }
        else if (isInteger(simpleExp)) {
            String varMoveEDB = "constMove(\'" + argu + "\'," + this.instructionCounter + ",\'" + simpleExp + "\').";
            System.out.println(varMoveEDB);
        }

        String instructionEDB = "instruction(\'" + argu + "\'," + this.instructionCounter++ + ",\'" + instructionLiteral + "\').";
        System.out.println(instructionEDB);

        assert(methodVarsMap.containsKey(argu));
        if (!methodVarsMap.get(argu).contains(varDecl)) {
            String varEDB = "var(\'" + argu + "\', \'" + varDecl + "\').";
            System.out.println(varEDB);
        }

        return instructionLiteral;
    }

    public String visit(final PrintStmt n, final String argu) {
        String instructionLiteral = n.f0.toString();

        n.f0.accept(this, argu);
        instructionLiteral += " " + n.f1.accept(this, argu);
        n.f1.accept(this.useVarFactGen, argu);

        String instructionEDB = "instruction(\'" + argu + "\'," + this.instructionCounter++ + ",\'" + instructionLiteral + "\').";
        System.out.println(instructionEDB);
        return instructionLiteral;
    }

    public String visit(final Exp n, final String argu) {
        return n.f0.accept(this, argu);
    }

    public String visit(final StmtExp n, final String argu) {
        String nRes = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return nRes;
    }

    public String visit(final Call n, final String argu) {
        String instructionLiteral = n.f0.toString();

        n.f0.accept(this, argu);
        instructionLiteral += " " + n.f1.accept(this, argu);
        n.f1.accept(this.useVarFactGen, argu);
        n.f2.accept(this, argu);
        instructionLiteral += " " + n.f2.tokenImage;
        instructionLiteral += " " + n.f3.accept(this, argu);
        n.f3.accept(this.useVarFactGen, argu);
        n.f4.accept(this, argu);
        instructionLiteral += " " + n.f4.tokenImage;

        return instructionLiteral;
    }

    public String visit(final HAllocate n, final String argu) {
        String instructionLiteral = n.f0.toString();
        n.f0.accept(this, argu);
        instructionLiteral += " " + n.f1.accept(this, argu);
        n.f1.accept(this.useVarFactGen, argu);

        return instructionLiteral;
    }

    public String visit(final BinOp n, final String argu) {
        String instructionLiteral = n.f0.f0.choice.toString();
        n.f0.accept(this, argu);
        instructionLiteral += " " + n.f1.accept(this, argu);
        n.f1.accept(this.useVarFactGen, argu);
        instructionLiteral += " " + n.f2.accept(this, argu);
        n.f2.accept(this.useVarFactGen, argu);

        return instructionLiteral;
    }

    public String visit(final Operator n, final String argu) {
        String nRes = null;
        n.f0.accept(this, argu);

        return nRes;
    }

    public String visit(final SimpleExp n, final String argu) {
        String simpleExp = n.f0.accept(this, argu);

        return simpleExp;
    }

    public String visit(final Temp n, final String argu) {
        n.f0.accept(this, argu);
        String var = n.f0.toString();
        var += " " + n.f1.accept(this, argu);

        return var;
    }

    public String visit(final IntegerLiteral n, final String argu) {
        n.f0.accept(this, argu);
        return n.f0.toString();
    }

    public String visit(final Label n, final String argu) {
        n.f0.accept(this, argu);
        return n.f0.toString();
    }
}
