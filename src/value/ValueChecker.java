package value;

import checkers.inference.BaseInferrableChecker;
import checkers.inference.InferenceChecker;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.dataflow.InferenceAnalysis;
import checkers.inference.model.ConstraintManager;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.flow.CFTransfer;

public class ValueChecker extends BaseInferrableChecker {
    @Override
    public void initChecker() {
        super.initChecker();
    }

    @Override
    public ValueVisitor createVisitor(
            InferenceChecker ichecker, BaseAnnotatedTypeFactory factory, boolean infer) {
        return new ValueVisitor(this, ichecker, factory, infer);
    }

    @Override
    public ValueAnnotatedTypeFactory createRealTypeFactory() {
        return new ValueAnnotatedTypeFactory(this);
    }

    @Override
    public CFTransfer createInferenceTransferFunction(InferenceAnalysis analysis) {
        return new ValueInferenceTransfer(analysis);
    }

    @Override
    public ValueInferenceAnnotatedTypeFactory createInferenceATF(
            InferenceChecker inferenceChecker,
            InferrableChecker realChecker,
            BaseAnnotatedTypeFactory realTypeFactory,
            SlotManager slotManager,
            ConstraintManager constraintManager) {
        ValueInferenceAnnotatedTypeFactory securityInferenceATF =
                new ValueInferenceAnnotatedTypeFactory(
                        inferenceChecker,
                        realChecker.withCombineConstraints(),
                        realTypeFactory,
                        realChecker,
                        slotManager,
                        constraintManager);
        return securityInferenceATF;
    }
}
