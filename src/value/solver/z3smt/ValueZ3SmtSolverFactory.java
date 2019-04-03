package value.solver.z3smt;

import checkers.inference.solver.frontend.Lattice;
import value.representation.TypeCheckValue;
import value.solver.z3smt.representation.Z3InferenceValue;
import backend.z3smt.Z3SmtFormatTranslator;
import backend.z3smt.Z3SmtSolverFactory;

public class ValueZ3SmtSolverFactory extends Z3SmtSolverFactory<Z3InferenceValue, TypeCheckValue> {

    @Override
    protected Z3SmtFormatTranslator<Z3InferenceValue, TypeCheckValue> createFormatTranslator(
            Lattice lattice) {
        return new ValueZ3SmtFormatTranslator(lattice);
    }
}