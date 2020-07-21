package value;

import checkers.inference.VariableAnnotator;
import checkers.inference.dataflow.InferenceAnalysis;
import checkers.inference.dataflow.InferenceTransfer;
import checkers.inference.model.AnnotationLocation;
import checkers.inference.model.ComparisonVariableSlot;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.dataflow.analysis.ConditionalTransferResult;
import org.checkerframework.dataflow.analysis.FlowExpressions;
import org.checkerframework.dataflow.analysis.FlowExpressions.Receiver;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.AssignmentNode;
import org.checkerframework.dataflow.cfg.node.EqualToNode;
import org.checkerframework.dataflow.cfg.node.GreaterThanNode;
import org.checkerframework.dataflow.cfg.node.GreaterThanOrEqualNode;
import org.checkerframework.dataflow.cfg.node.LessThanNode;
import org.checkerframework.dataflow.cfg.node.LessThanOrEqualNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.NotEqualNode;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFValue;

public class ValueInferenceTransfer extends InferenceTransfer {

    private ValueInferenceAnnotatedTypeFactory typeFactory;

    public ValueInferenceTransfer(InferenceAnalysis analysis) {
        super(analysis);
        typeFactory = (ValueInferenceAnnotatedTypeFactory) analysis.getTypeFactory();
    }

    private InferenceAnalysis getInferenceAnalysis() {
        return (InferenceAnalysis) analysis;
    }

    private void createComparisonVariableSlot(Node node, CFStore thenStore, CFStore elseStore) {
        Tree tree = node.getTree();
        // Don't need to create variable slot for literal trees
        if (tree instanceof LiteralTree) {
            return;
        }
        AnnotationLocation lcoation =
                VariableAnnotator.treeToLocation(analysis.getTypeFactory(), tree);
        ComparisonVariableSlot thenSlot =
                getInferenceAnalysis()
                        .getSlotManager()
                        .createComparisonVariableSlot(lcoation, true);
        ComparisonVariableSlot elseSlot =
                getInferenceAnalysis()
                        .getSlotManager()
                        .createComparisonVariableSlot(lcoation, false);
        AnnotationMirror thenAm = getInferenceAnalysis().getSlotManager().getAnnotation(thenSlot);
        AnnotationMirror elseAm = getInferenceAnalysis().getSlotManager().getAnnotation(elseSlot);

        // If node is assignment, iterate over lhs; otherwise, just node.
        Receiver rec;
        if (node instanceof AssignmentNode) {
            AssignmentNode a = (AssignmentNode) node;
            rec = FlowExpressions.internalReprOf(analysis.getTypeFactory(), a.getTarget());
        } else {
            rec = FlowExpressions.internalReprOf(analysis.getTypeFactory(), node);
        }
        thenStore.insertValue(rec, thenAm);
        elseStore.insertValue(rec, elseAm);
    }

    @Override
    public TransferResult<CFValue, CFStore> visitEqualTo(
            EqualToNode n, TransferInput<CFValue, CFStore> in) {
        TransferResult<CFValue, CFStore> result = super.visitEqualTo(n, in);
        CFStore thenStore = result.getThenStore();
        CFStore elseStore = result.getElseStore();

        createComparisonVariableSlot(n.getLeftOperand(), thenStore, elseStore);
        createComparisonVariableSlot(n.getRightOperand(), thenStore, elseStore);

        CFValue newResultValue =
                analysis.createAbstractValue(typeFactory.getAnnotatedType(n.getTree()));
        return new ConditionalTransferResult<>(newResultValue, thenStore, elseStore);
    }

    @Override
    public TransferResult<CFValue, CFStore> visitNotEqual(
            NotEqualNode n, TransferInput<CFValue, CFStore> in) {
        TransferResult<CFValue, CFStore> result = super.visitNotEqual(n, in);
        CFStore thenStore = result.getThenStore();
        CFStore elseStore = result.getElseStore();

        createComparisonVariableSlot(n.getLeftOperand(), thenStore, elseStore);
        createComparisonVariableSlot(n.getRightOperand(), thenStore, elseStore);

        CFValue newResultValue =
                analysis.createAbstractValue(typeFactory.getAnnotatedType(n.getTree()));
        return new ConditionalTransferResult<>(newResultValue, thenStore, elseStore);
    }

    @Override
    public TransferResult<CFValue, CFStore> visitGreaterThan(
            GreaterThanNode n, TransferInput<CFValue, CFStore> in) {
        TransferResult<CFValue, CFStore> result = super.visitGreaterThan(n, in);
        CFStore thenStore = result.getThenStore();
        CFStore elseStore = result.getElseStore();

        createComparisonVariableSlot(n.getLeftOperand(), thenStore, elseStore);
        createComparisonVariableSlot(n.getRightOperand(), thenStore, elseStore);

        CFValue newResultValue =
                analysis.createAbstractValue(typeFactory.getAnnotatedType(n.getTree()));
        return new ConditionalTransferResult<>(newResultValue, thenStore, elseStore);
    }

    @Override
    public TransferResult<CFValue, CFStore> visitGreaterThanOrEqual(
            GreaterThanOrEqualNode n, TransferInput<CFValue, CFStore> in) {
        TransferResult<CFValue, CFStore> result = super.visitGreaterThanOrEqual(n, in);
        CFStore thenStore = result.getThenStore();
        CFStore elseStore = result.getElseStore();

        createComparisonVariableSlot(n.getLeftOperand(), thenStore, elseStore);
        createComparisonVariableSlot(n.getRightOperand(), thenStore, elseStore);

        CFValue newResultValue =
                analysis.createAbstractValue(typeFactory.getAnnotatedType(n.getTree()));
        return new ConditionalTransferResult<>(newResultValue, thenStore, elseStore);
    }

    @Override
    public TransferResult<CFValue, CFStore> visitLessThan(
            LessThanNode n, TransferInput<CFValue, CFStore> in) {
        TransferResult<CFValue, CFStore> result = super.visitLessThan(n, in);
        CFStore thenStore = result.getThenStore();
        CFStore elseStore = result.getElseStore();

        createComparisonVariableSlot(n.getLeftOperand(), thenStore, elseStore);
        createComparisonVariableSlot(n.getRightOperand(), thenStore, elseStore);

        CFValue newResultValue =
                analysis.createAbstractValue(typeFactory.getAnnotatedType(n.getTree()));
        return new ConditionalTransferResult<>(newResultValue, thenStore, elseStore);
    }

    @Override
    public TransferResult<CFValue, CFStore> visitLessThanOrEqual(
            LessThanOrEqualNode n, TransferInput<CFValue, CFStore> in) {
        TransferResult<CFValue, CFStore> result = super.visitLessThanOrEqual(n, in);
        CFStore thenStore = result.getThenStore();
        CFStore elseStore = result.getElseStore();

        createComparisonVariableSlot(n.getLeftOperand(), thenStore, elseStore);
        createComparisonVariableSlot(n.getRightOperand(), thenStore, elseStore);

        CFValue newResultValue =
                analysis.createAbstractValue(typeFactory.getAnnotatedType(n.getTree()));
        return new ConditionalTransferResult<>(newResultValue, thenStore, elseStore);
    }
}
