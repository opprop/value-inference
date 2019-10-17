package value.solver;

import checkers.inference.InferenceMain;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.model.SubtypeConstraint;
import checkers.inference.solver.backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.solver.backend.z3smt.Z3SmtSolver;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverEnvironment;
import com.microsoft.z3.Expr;
import java.util.Collection;
import value.representation.TypeCheckValue;
import value.solver.representation.Z3InferenceValue;

public class ValueSolver extends Z3SmtSolver<Z3InferenceValue, TypeCheckValue> {

    public ValueSolver(
            SolverEnvironment solverEnvironment,
            Collection<Slot> slots,
            Collection<Constraint> constraints,
            Z3SmtFormatTranslator<Z3InferenceValue, TypeCheckValue> z3SmtFormatTranslator,
            Lattice lattice) {
        super(solverEnvironment, slots, constraints, z3SmtFormatTranslator, lattice);
    }

    @Override
    protected void encodeSoftSubtypeConstraint(SubtypeConstraint stc) {
        Constraint eqc =
                InferenceMain.getInstance()
                        .getConstraintManager()
                        .createEqualityConstraint(stc.getSubtype(), stc.getSupertype());

        Expr simplifiedEQC = eqc.serialize(formatTranslator).simplify();

        if (!simplifiedEQC.isTrue()) {
            if (stc.getSubtype().getKind() == Slot.Kind.CONSTANT) {
                addSoftConstraint(simplifiedEQC, 3);
            } else if (stc.getSupertype().getKind() == Slot.Kind.CONSTANT) {
                addSoftConstraint(simplifiedEQC, 2);
            } else {
                addSoftConstraint(simplifiedEQC, 1);
            }
        }
    }
}
