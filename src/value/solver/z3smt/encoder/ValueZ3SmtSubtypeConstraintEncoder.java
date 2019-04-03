package value.solver.z3smt.encoder;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

import backend.z3smt.Z3SmtFormatTranslator;
import backend.z3smt.encoder.Z3SmtAbstractConstraintEncoder;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.VariableSlot;
import checkers.inference.solver.backend.encoder.binary.SubtypeConstraintEncoder;
import checkers.inference.solver.frontend.Lattice;
import value.representation.TypeCheckValue;
import value.solver.z3smt.representation.Z3InferenceValue;

public class ValueZ3SmtSubtypeConstraintEncoder 
	extends Z3SmtAbstractConstraintEncoder<Z3InferenceValue, TypeCheckValue> 
	implements SubtypeConstraintEncoder<BoolExpr> {

	public ValueZ3SmtSubtypeConstraintEncoder(Lattice lattice, Context ctx,
			Z3SmtFormatTranslator<Z3InferenceValue, TypeCheckValue> z3SmtFormatTranslator) {
		super(lattice, ctx, z3SmtFormatTranslator);
		// TODO Auto-generated constructor stub
	}

	@Override
	public BoolExpr encodeVariable_Variable(VariableSlot fst, VariableSlot snd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BoolExpr encodeVariable_Constant(VariableSlot fst, ConstantSlot snd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BoolExpr encodeConstant_Variable(ConstantSlot fst, VariableSlot snd) {
		// TODO Auto-generated method stub
		return null;
	}

}
