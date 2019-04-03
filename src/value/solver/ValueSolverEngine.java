package value.solver;

import checkers.inference.solver.SolverEngine;
import checkers.inference.solver.backend.SolverFactory;
import value.solver.z3smt.ValueZ3SmtSolverFactory;

public class ValueSolverEngine extends SolverEngine {
    @Override
    protected SolverFactory createSolverFactory() {
        return new ValueZ3SmtSolverFactory();
    }
}