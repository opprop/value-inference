package value.solver;

import checkers.inference.solver.backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.solver.backend.z3smt.Z3SmtSolverFactory;
import checkers.inference.solver.frontend.Lattice;
import value.representation.TypeCheckValue;
import value.solver.representation.Z3InferenceValue;

public class ValueSolverFactory extends Z3SmtSolverFactory<Z3InferenceValue, TypeCheckValue> {

    @Override
    protected Z3SmtFormatTranslator<Z3InferenceValue, TypeCheckValue> createFormatTranslator(
            Lattice lattice) {
        return new ValueFormatTranslator(lattice);
    }
}
