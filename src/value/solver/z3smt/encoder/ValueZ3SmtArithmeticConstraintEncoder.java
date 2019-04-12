package value.solver.z3smt.encoder;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

import checkers.inference.model.ArithmeticConstraint.ArithmeticOperationKind;
import checkers.inference.model.ArithmeticVariableSlot;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
import checkers.inference.solver.backend.encoder.ArithmeticConstraintEncoder;
import checkers.inference.solver.frontend.Lattice;
import value.solver.z3smt.ValueZ3SmtFormatTranslator;
import value.solver.z3smt.representation.Z3InferenceValue;

public class ValueZ3SmtArithmeticConstraintEncoder extends ValueZ3SmtAbstractConstraintEncoder
implements ArithmeticConstraintEncoder<BoolExpr> {

	public ValueZ3SmtArithmeticConstraintEncoder(Lattice lattice, Context ctx,
			ValueZ3SmtFormatTranslator valueZ3SmtFormatTranslator) {
		super(lattice, ctx, valueZ3SmtFormatTranslator);
	}
	
	protected BoolExpr encode(ArithmeticOperationKind operation, Slot leftOperand, 
			Slot rightOperand, ArithmeticVariableSlot result) {
        Z3InferenceValue left = leftOperand.serialize(z3SmtFormatTranslator);
        Z3InferenceValue right = rightOperand.serialize(z3SmtFormatTranslator);
        Z3InferenceValue res = result.serialize(z3SmtFormatTranslator);

        return ctx.mkOr(
        		ctx.mkAnd(left.getUnknownVal(), right.getUnknownVal(), res.getUnknownVal()),
        		ctx.mkAnd(left.getUnknownVal(), right.getNumVal(), res.getUnknownVal()),
        		ctx.mkAnd(left.getNumVal(), right.getUnknownVal(), res.getUnknownVal()),
        		ctx.mkAnd(left.getNumVal(), right.getNumVal(), res.getNumVal())
        		);
	}

	@Override
	public BoolExpr encodeVariable_Variable(ArithmeticOperationKind operation, VariableSlot leftOperand,
			VariableSlot rightOperand, ArithmeticVariableSlot result) {
		return encode(operation, leftOperand, rightOperand, result);
	}

	@Override
	public BoolExpr encodeVariable_Constant(ArithmeticOperationKind operation, VariableSlot leftOperand,
			ConstantSlot rightOperand, ArithmeticVariableSlot result) {
		return encode(operation, leftOperand, rightOperand, result);
	}

	@Override
	public BoolExpr encodeConstant_Variable(ArithmeticOperationKind operation, ConstantSlot leftOperand,
			VariableSlot rightOperand, ArithmeticVariableSlot result) {
		return encode(operation, leftOperand, rightOperand, result);
	}

	@Override
	public BoolExpr encodeConstant_Constant(ArithmeticOperationKind operation, ConstantSlot leftOperand,
			ConstantSlot rightOperand, ArithmeticVariableSlot result) {
		return encode(operation, leftOperand, rightOperand, result);
	}

}
