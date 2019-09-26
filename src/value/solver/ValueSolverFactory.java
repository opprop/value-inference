package value.solver;

import checkers.inference.InferenceMain;
import checkers.inference.solver.backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.solver.backend.z3smt.Z3SmtSolverFactory;
import checkers.inference.solver.frontend.Lattice;
import value.representation.TypeCheckValue;
import value.solver.representation.Z3InferenceValue;

import org.checkerframework.common.value.ValueAnnotatedTypeFactory;

public class ValueSolverFactory extends Z3SmtSolverFactory<Z3InferenceValue, TypeCheckValue> {

  @Override
  protected Z3SmtFormatTranslator<Z3InferenceValue, TypeCheckValue> createFormatTranslator(
          Lattice lattice) {
      return new ValueFormatTranslator(lattice);
  }
}
