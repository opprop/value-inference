package value.solver;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.Solver;
import checkers.inference.solver.backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.solver.backend.z3smt.Z3SmtSolver;
import checkers.inference.solver.backend.z3smt.Z3SmtSolverFactory;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverEnvironment;
import java.util.Collection;
import value.representation.TypeCheckValue;
import value.solver.representation.Z3InferenceValue;

public class ValueSolverFactory extends Z3SmtSolverFactory<Z3InferenceValue, TypeCheckValue> {

    @Override
    protected Z3SmtFormatTranslator<Z3InferenceValue, TypeCheckValue> createFormatTranslator(
            Lattice lattice) {
        return new ValueFormatTranslator(lattice);
    }

    @Override
    public Solver<?> createSolver(
            SolverEnvironment solverEnvironment,
            Collection<Slot> slots,
            Collection<Constraint> constraints,
            Lattice lattice) {
        Z3SmtFormatTranslator<Z3InferenceValue, TypeCheckValue> formatTranslator =
                createFormatTranslator(lattice);
        return new Z3SmtSolver<Z3InferenceValue, TypeCheckValue>(solverEnvironment, slots, constraints, formatTranslator, lattice);
    }
}
