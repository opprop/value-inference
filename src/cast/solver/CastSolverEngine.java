package cast.solver;

import checkers.inference.solver.SolverEngine;
import checkers.inference.solver.backend.SolverFactory;
import checkers.inference.solver.backend.maxsat.MaxSatSolverFactory;

public class CastSolverEngine extends SolverEngine {
    @Override
    protected SolverFactory createSolverFactory() {
        return new MaxSatSolverFactory();
    }
}