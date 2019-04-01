package cast.solver.z3smt;

import checkers.inference.solver.frontend.Lattice;
import backend.z3smt.Z3SmtFormatTranslator;
import backend.z3smt.Z3SmtSolverFactory;
import cast.representation.TypeCheckCast;
import cast.solver.z3smt.representation.Z3InferenceCast;

public class CastZ3SmtSolverFactory extends Z3SmtSolverFactory<Z3InferenceCast, TypeCheckCast> {

    @Override
    protected Z3SmtFormatTranslator<Z3InferenceCast, TypeCheckCast> createFormatTranslator(
            Lattice lattice) {
        return new CastZ3SmtFormatTranslator(lattice);
    }
}