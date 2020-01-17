package value.solver.encoder;

import checkers.inference.InferenceMain;
import checkers.inference.model.ArithmeticConstraint;
import checkers.inference.model.CombineConstraint;
import checkers.inference.model.ComparableConstraint;
import checkers.inference.model.Constraint;
import checkers.inference.model.EqualityConstraint;
import checkers.inference.model.ExistentialConstraint;
import checkers.inference.model.ImplicationConstraint;
import checkers.inference.model.InequalityConstraint;
import checkers.inference.model.PreferenceConstraint;
import checkers.inference.model.Slot;
import checkers.inference.model.SubtypeConstraint;
import checkers.inference.solver.backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.solver.backend.z3smt.encoder.Z3SmtSoftConstraintEncoder;
import checkers.inference.solver.frontend.Lattice;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import value.representation.TypeCheckValue;
import value.solver.representation.Z3InferenceValue;

public class ValueZ3SmtSoftConstraintEncoder
        extends Z3SmtSoftConstraintEncoder<Z3InferenceValue, TypeCheckValue> {

    public ValueZ3SmtSoftConstraintEncoder(
            Lattice lattice,
            Context ctx,
            Z3SmtFormatTranslator<Z3InferenceValue, TypeCheckValue> z3SmtFormatTranslator) {
        super(lattice, ctx, z3SmtFormatTranslator);
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
                addSoftConstraint(simplifiedEQC, 4);
            } else if (stc.getSupertype().getKind() == Slot.Kind.CONSTANT) {
                addSoftConstraint(simplifiedEQC, 3);
            } else {
                addSoftConstraint(simplifiedEQC, 2);
            }
        }
    }

    @Override
    protected void encodeSoftComparableConstraint(ComparableConstraint cc) {
        //		Z3InferenceValue fst = cc.getFirst().serialize(formatTranslator);
        //        Z3InferenceValue snd = cc.getSecond().serialize(formatTranslator);
        //    	switch(cc.getOperation()) {
        //    		case EQUAL_TO:
        //	    		Constraint eqc =
        //	                    InferenceMain.getInstance()
        //	                            .getConstraintManager()
        //	                            .createEqualityConstraint(cc.getFirst(), cc.getSecond());
        //	            Expr simplifiedEQC = eqc.serialize(formatTranslator).simplify();
        //	            if (!simplifiedEQC.isTrue()) {
        //	        		addSoftConstraint(simplifiedEQC, 1);
        //	            }
        //	            break;
        //    		case GREATER_THAN:
        //    			BoolExpr gt = ctx.mkOr(
        //                        ctx.mkAnd(
        //                        		fst.getIntRange(),
        //                                ctx.mkGt(fst.getIntRangeLower(), snd.getIntRangeLower()),
        //                                ctx.mkGt(
        //                                		fst.getIntRangeUpper(), snd.getIntRangeUpper())),
        //                        ctx.mkNot(fst.getIntRange()));
        //		        if (!gt.isTrue()) {
        //		    		addSoftConstraint(ctx.mkNot(gt), 1);
        //		        }
        //		        break;
        //    		case LESS_THAN:
        //    			BoolExpr lt = ctx.mkOr(
        //                        ctx.mkAnd(
        //                        		fst.getIntRange(),
        //                                ctx.mkLt(fst.getIntRangeLower(), snd.getIntRangeLower()),
        //                                ctx.mkLt(
        //                                		fst.getIntRangeUpper(), snd.getIntRangeUpper())),
        //                        ctx.mkNot(fst.getIntRange()));
        //		        if (!lt.isTrue()) {
        //		    		addSoftConstraint(ctx.mkNot(lt), 1);
        //		        }
        //		        break;
        //    		case GREATER_THAN_EQUAL:
        //    			BoolExpr gteq = ctx.mkOr(
        //                        ctx.mkAnd(
        //                        		fst.getIntRange(),
        //                                ctx.mkGe(fst.getIntRangeLower(), snd.getIntRangeLower()),
        //                                ctx.mkGe(
        //                                		fst.getIntRangeUpper(), snd.getIntRangeUpper())),
        //                        ctx.mkNot(fst.getIntRange()));
        //		        if (!gteq.isTrue()) {
        //		    		addSoftConstraint(ctx.mkNot(gteq), 1);
        //		        }
        //		        break;
        //    		case LESS_THAN_EQUAL:
        //    			BoolExpr lteq = ctx.mkOr(
        //                        ctx.mkAnd(
        //                        		fst.getIntRange(),
        //                                ctx.mkLe(fst.getIntRangeLower(), snd.getIntRangeLower()),
        //                                ctx.mkLe(
        //                                		fst.getIntRangeUpper(), snd.getIntRangeUpper())),
        //                        ctx.mkNot(fst.getIntRange()));
        //		        if (!lteq.isTrue()) {
        //		    		addSoftConstraint(ctx.mkNot(lteq), 1);
        //		        }
        //		        break;
        //			default:
        //				break;
        //
        //    	}
    }

    @Override
    protected void encodeSoftArithmeticConstraint(ArithmeticConstraint constraint) {}

    @Override
    protected void encodeSoftEqualityConstraint(EqualityConstraint constraint) {}

    @Override
    protected void encodeSoftInequalityConstraint(InequalityConstraint constraint) {}

    @Override
    protected void encodeSoftImplicationConstraint(ImplicationConstraint constraint) {}

    @Override
    protected void encodeSoftExistentialConstraint(ExistentialConstraint constraint) {}

    @Override
    protected void encodeSoftCombineConstraint(CombineConstraint constraint) {}

    @Override
    protected void encodeSoftPreferenceConstraint(PreferenceConstraint constraint) {}
}