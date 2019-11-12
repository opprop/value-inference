package value.solver.encoder;

import checkers.inference.model.ComparableConstraint.ComparableOperationKind;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.encoder.binary.ComparableConstraintEncoder;
import checkers.inference.solver.backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.solver.frontend.Lattice;

import org.checkerframework.javacutil.BugInCF;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import value.representation.TypeCheckValue;
import value.solver.representation.Z3InferenceValue;

public class ValueComparableConstraintEncoder extends ValueAbstractConstraintEncoder
        implements ComparableConstraintEncoder<BoolExpr> {

    public ValueComparableConstraintEncoder(
            Lattice lattice,
            Context ctx,
            Z3SmtFormatTranslator<Z3InferenceValue, TypeCheckValue> formatTranslator) {
        super(lattice, ctx, formatTranslator);
    }

    protected BoolExpr encode(Slot fst, Slot snd) {
        Z3InferenceValue first = fst.serialize(z3SmtFormatTranslator);
        Z3InferenceValue second = snd.serialize(z3SmtFormatTranslator);

        return ctx.mkAnd(
                ctx.mkEq(first.getBottomVal(), second.getBottomVal()),
                ctx.mkEq(first.getUnknownVal(), second.getUnknownVal()),
                ctx.mkEq(first.getBoolVal(), second.getBoolVal()),
                ctx.mkEq(first.getStringVal(), second.getStringVal()),
                ctx.mkEq(first.getIntRange(), second.getIntRange()));
    }
    
    protected BoolExpr encode(ComparableOperationKind operation,
            Slot first,
            Slot second) {
        Z3InferenceValue fst = first.serialize(z3SmtFormatTranslator);
        Z3InferenceValue snd = second.serialize(z3SmtFormatTranslator);
        
        BoolExpr encoding = ctx.mkAnd(
                ctx.mkEq(fst.getBottomVal(), snd.getBottomVal()),
                ctx.mkEq(fst.getUnknownVal(), snd.getUnknownVal()),
                ctx.mkEq(fst.getBoolVal(), snd.getBoolVal()),
                ctx.mkEq(fst.getStringVal(), snd.getStringVal()),
                ctx.mkEq(fst.getIntRange(), snd.getIntRange()));
        		
        switch (operation) {
		    case EQUAL_TO:
				break;
		    case NOT_EQUAL_TO:
		    	break;
		    case GREATER_THAN:
		    	break;
		    case GREATER_THAN_EQUAL:
		    	break;
		    case LESS_THAN:
		    	break;
		    case LESS_THAN_EQUAL:
		    	break;
		    default:
		    	throw new BugInCF(
                        "Attempting to encode an unsupported comparable operation: "
                                + operation
                                + " first: "
                                + first
                                + " second: "
                                + second);
        }

        return encoding;
    }

    @Override
    public BoolExpr encodeVariable_Variable(Slot fst, Slot snd) {
        return encode(fst, snd);
    }

    @Override
    public BoolExpr encodeVariable_Constant(Slot fst, ConstantSlot snd) {
        return encode(fst, snd);
    }

    @Override
    public BoolExpr encodeConstant_Variable(ConstantSlot fst, Slot snd) {
        return encode(fst, snd);
    }
    
    @Override
    public BoolExpr encodeVariable_Variable(
            ComparableOperationKind operation,
            Slot fst,
            Slot snd) {
        return encode(operation, fst, snd);
    }

    @Override
    public BoolExpr encodeVariable_Constant(
    		ComparableOperationKind operation,
            Slot fst,
            ConstantSlot snd) {
        return encode(operation, fst, snd);
    }

    @Override
    public BoolExpr encodeConstant_Variable(
    		ComparableOperationKind operation,
            ConstantSlot fst,
            Slot snd) {
        return encode(operation, fst, snd);
    }

    @Override
    public BoolExpr encodeConstant_Constant(
    		ComparableOperationKind operation,
            ConstantSlot fst,
            ConstantSlot snd) {
        return encode(operation, fst, snd);
    }
}
