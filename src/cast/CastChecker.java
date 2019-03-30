package cast;

import checkers.inference.BaseInferrableChecker;
import checkers.inference.InferenceChecker;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.dataflow.InferenceAnalysis;
import checkers.inference.dataflow.InferenceTransfer;
import checkers.inference.model.ConstraintManager;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.flow.CFTransfer;

public class CastChecker extends BaseInferrableChecker {
    @Override
    public void initChecker() {
        super.initChecker();
    }

    @Override
    public CastVisitor createVisitor(
            InferenceChecker ichecker, BaseAnnotatedTypeFactory factory, boolean infer) {
        return new CastVisitor(this, ichecker, factory, infer);
    }

    @Override
    public CastAnnotatedTypeFactory createRealTypeFactory() {
        return new CastAnnotatedTypeFactory(this);
    }

    @Override
    public CFTransfer createInferenceTransferFunction(InferenceAnalysis analysis) {
        return new InferenceTransfer(analysis);
    }

    @Override
    public CastInferenceAnnotatedTypeFactory createInferenceATF(
            InferenceChecker inferenceChecker,
            InferrableChecker realChecker,
            BaseAnnotatedTypeFactory realTypeFactory,
            SlotManager slotManager,
            ConstraintManager constraintManager) {
        CastInferenceAnnotatedTypeFactory securityInferenceATF =
                new CastInferenceAnnotatedTypeFactory(
                        inferenceChecker,
                        realChecker.withCombineConstraints(),
                        realTypeFactory,
                        realChecker,
                        slotManager,
                        constraintManager);
        return securityInferenceATF;
    }

    @Override
    public boolean isInsertMainModOfLocalVar() {
        return false;
    }

    @Override
    public boolean withCombineConstraints() {
        return false;
    }
}