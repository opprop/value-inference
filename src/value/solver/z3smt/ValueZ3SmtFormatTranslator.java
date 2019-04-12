package value.solver.z3smt;

import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import com.microsoft.z3.BoolExpr;

import backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.VariableSlot;
import checkers.inference.solver.backend.encoder.ConstraintEncoderFactory;
import checkers.inference.solver.frontend.Lattice;
import value.representation.TypeCheckValue;
import value.solver.z3smt.encoder.ValueZ3SmtConstraintEncoderFactory;
import value.solver.z3smt.representation.Z3InferenceValue;

public class ValueZ3SmtFormatTranslator extends Z3SmtFormatTranslator<Z3InferenceValue, TypeCheckValue> {

	public ValueZ3SmtFormatTranslator(Lattice lattice) {
		super(lattice);
		// TODO Auto-generated constructor stub
	}

	@Override
	public AnnotationMirror decodeSolution(TypeCheckValue solution, ProcessingEnvironment processingEnvironment) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Z3InferenceValue serializeVarSlot(VariableSlot slot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Z3InferenceValue serializeConstantSlot(ConstantSlot slot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String generateZ3SlotDeclaration(VariableSlot slot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BoolExpr encodeSlotWellformnessConstraint(VariableSlot slot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BoolExpr encodeSlotPreferenceConstraint(VariableSlot slot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, AnnotationMirror> decodeSolution(List<String> model, ProcessingEnvironment processingEnv) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ConstraintEncoderFactory<BoolExpr> createConstraintEncoderFactory() {
		return new ValueZ3SmtConstraintEncoderFactory(lattice, ctx, this);
	}

}
