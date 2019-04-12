package value.solver.z3smt.encoder;

import com.microsoft.z3.Context;

import backend.z3smt.encoder.Z3SmtAbstractConstraintEncoder;
import checkers.inference.solver.frontend.Lattice;
import value.representation.TypeCheckValue;
import value.solver.z3smt.ValueZ3SmtFormatTranslator;
import value.solver.z3smt.representation.Z3InferenceValue;

public class ValueZ3SmtAbstractConstraintEncoder 
	extends Z3SmtAbstractConstraintEncoder<Z3InferenceValue, TypeCheckValue> {

    protected final ValueZ3SmtEncoderUtils valueZ3SmtEncoderUtils;

    public ValueZ3SmtAbstractConstraintEncoder(
            Lattice lattice, Context ctx, ValueZ3SmtFormatTranslator valueZ3SmtFormatTranslator) {
        super(lattice, ctx, valueZ3SmtFormatTranslator);
        this.valueZ3SmtEncoderUtils = new ValueZ3SmtEncoderUtils();
    }
}
