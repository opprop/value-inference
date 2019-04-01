package cast.solver;

import cast.solver.z3smt.CastZ3SmtSolverFactory;
import checkers.inference.solver.SolverEngine;
import checkers.inference.solver.backend.SolverFactory;

public class CastSolverEngine extends SolverEngine {
    @Override
    protected SolverFactory createSolverFactory() {
        return new CastZ3SmtSolverFactory();
    }
}