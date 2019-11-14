package value.solver;

import checkers.inference.InferenceMain;
import checkers.inference.model.ComparableConstraint;
import checkers.inference.model.ComparableConstraint.ComparableOperationKind;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.model.SubtypeConstraint;
import checkers.inference.solver.backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.solver.backend.z3smt.Z3SmtSolver;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverEnvironment;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import java.util.Collection;
import value.representation.TypeCheckValue;
import value.solver.representation.Z3InferenceValue;

public class ValueSolver extends Z3SmtSolver<Z3InferenceValue, TypeCheckValue> {

    public ValueSolver(
            SolverEnvironment solverEnvironment,
            Collection<Slot> slots,
            Collection<Constraint> constraints,
            Z3SmtFormatTranslator<Z3InferenceValue, TypeCheckValue> z3SmtFormatTranslator,
            Lattice lattice) {
        super(solverEnvironment, slots, constraints, z3SmtFormatTranslator, lattice);
    }

    @Override
    protected void encodeSoftSubtypeConstraint(SubtypeConstraint stc) {
        Constraint eqc =
                InferenceMain.getInstance()
                        .getConstraintManager()
                        .createEqualityConstraint(stc.getSubtype(), stc.getSupertype());

        Expr simplifiedEQC = eqc.serialize(formatTranslator).simplify();

        if (!simplifiedEQC.isTrue()) {
            if (stc.getSubtype().getKind() == Slot.Kind.CONSTANT) {
                addSoftConstraint(simplifiedEQC, 3);
            } else if (stc.getSupertype().getKind() == Slot.Kind.CONSTANT) {
                addSoftConstraint(simplifiedEQC, 2);
            } else {
                addSoftConstraint(simplifiedEQC, 1);
            }
        }
    }
    
    @Override
    protected void encodeSoftComparableConstraint(ComparableConstraint cc) {
    	Z3InferenceValue fst = cc.getFirst().serialize(formatTranslator);
        Z3InferenceValue snd = cc.getSecond().serialize(formatTranslator);
    	switch(cc.getOperation()) {
    		case EQUAL_TO:
	    		Constraint eqc =
	                    InferenceMain.getInstance()
	                            .getConstraintManager()
	                            .createEqualityConstraint(cc.getFirst(), cc.getSecond());
	            Expr simplifiedEQC = eqc.serialize(formatTranslator).simplify();
	            if (!simplifiedEQC.isTrue()) {
	        		addSoftConstraint(simplifiedEQC, 1);
	            }
	            break;
    		case GREATER_THAN:
    			BoolExpr gt = ctx.mkOr(
                        ctx.mkAnd(
                        		fst.getIntRange(),
                                ctx.mkGt(fst.getIntRangeLower(), snd.getIntRangeLower()),
                                ctx.mkGt(
                                		fst.getIntRangeUpper(), snd.getIntRangeUpper())),
                        ctx.mkNot(fst.getIntRange()));
		        if (!gt.isTrue()) {
		    		addSoftConstraint(ctx.mkNot(gt), 1);
		        }
		        break;
    		case LESS_THAN:
    			BoolExpr lt = ctx.mkOr(
                        ctx.mkAnd(
                        		fst.getIntRange(),
                                ctx.mkLt(fst.getIntRangeLower(), snd.getIntRangeLower()),
                                ctx.mkLt(
                                		fst.getIntRangeUpper(), snd.getIntRangeUpper())),
                        ctx.mkNot(fst.getIntRange()));
		        if (!lt.isTrue()) {
		    		addSoftConstraint(ctx.mkNot(lt), 1);
		        }
		        break;
    		case GREATER_THAN_EQUAL:
    			BoolExpr gteq = ctx.mkOr(
                        ctx.mkAnd(
                        		fst.getIntRange(),
                                ctx.mkGe(fst.getIntRangeLower(), snd.getIntRangeLower()),
                                ctx.mkGe(
                                		fst.getIntRangeUpper(), snd.getIntRangeUpper())),
                        ctx.mkNot(fst.getIntRange()));
		        if (!gteq.isTrue()) {
		    		addSoftConstraint(ctx.mkNot(gteq), 1);
		        }
		        break;
    		case LESS_THAN_EQUAL:
    			BoolExpr lteq = ctx.mkOr(
                        ctx.mkAnd(
                        		fst.getIntRange(),
                                ctx.mkLe(fst.getIntRangeLower(), snd.getIntRangeLower()),
                                ctx.mkLe(
                                		fst.getIntRangeUpper(), snd.getIntRangeUpper())),
                        ctx.mkNot(fst.getIntRange()));
		        if (!lteq.isTrue()) {
		    		addSoftConstraint(ctx.mkNot(lteq), 1);
		        }
		        break;
			default:
				break;
    		
    	}
    			
    	
    	
    	
    }
}
