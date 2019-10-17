package value.solver.encoder;

import checkers.inference.solver.backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.solver.backend.z3smt.encoder.Z3SmtAbstractConstraintEncoder;
import checkers.inference.solver.frontend.Lattice;
import com.microsoft.z3.Context;
import value.representation.TypeCheckValue;
import value.solver.representation.Z3InferenceValue;

public class ValueAbstractConstraintEncoder
        extends Z3SmtAbstractConstraintEncoder<Z3InferenceValue, TypeCheckValue> {

    protected final ValueEncoderUtils valueZ3SmtEncoderUtils;

    public ValueAbstractConstraintEncoder(
            Lattice lattice,
            Context ctx,
            Z3SmtFormatTranslator<Z3InferenceValue, TypeCheckValue> z3SmtFormatTranslator) {
        super(lattice, ctx, z3SmtFormatTranslator);
        this.valueZ3SmtEncoderUtils = new ValueEncoderUtils();
    }
}
