package value;

import org.checkerframework.common.value.util.NumberUtils;
import org.checkerframework.common.value.util.Range;
import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.NumericalAdditionNode;
import org.checkerframework.dataflow.cfg.node.NumericalMultiplicationNode;
import org.checkerframework.dataflow.cfg.node.NumericalSubtractionNode;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.BugInCF;
import org.checkerframework.javacutil.TypesUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;

public class ValueTransfer extends CFTransfer {

    private final ValueAnnotatedTypeFactory typeFactory;

    public ValueTransfer(
        CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        super(analysis);
        typeFactory = (ValueAnnotatedTypeFactory) analysis.getTypeFactory();
    }

    @Override
    public TransferResult<CFValue, CFStore> visitNumericalAddition(NumericalAdditionNode n,
        TransferInput<CFValue, CFStore> cfValueCFStoreTransferInput) {
        TransferResult<CFValue, CFStore> transferResult = super
            .visitNumericalAddition(n, cfValueCFStoreTransferInput);
        AnnotationMirror resultAnno = calculateNumericalBinaryOp(n.getLeftOperand(),
            n.getRightOperand(),
            NumericalBinaryOps.ADDITION, cfValueCFStoreTransferInput);
        return createNewResult(transferResult, resultAnno);
    }

    @Override
    public TransferResult<CFValue, CFStore> visitNumericalSubtraction(
        NumericalSubtractionNode n, TransferInput<CFValue, CFStore> cfValueCFStoreTransferInput) {
        TransferResult<CFValue, CFStore> transferResult = super
            .visitNumericalSubtraction(n, cfValueCFStoreTransferInput);
        AnnotationMirror resultAnno =
            calculateNumericalBinaryOp(
                n.getLeftOperand(), n.getRightOperand(), NumericalBinaryOps.SUBTRACTION,
                cfValueCFStoreTransferInput);
        return createNewResult(transferResult, resultAnno);
    }

    @Override
    public TransferResult<CFValue, CFStore> visitNumericalMultiplication(
        NumericalMultiplicationNode n,
        TransferInput<CFValue, CFStore> cfValueCFStoreTransferInput) {
        TransferResult<CFValue, CFStore> transferResult = super
            .visitNumericalMultiplication(n, cfValueCFStoreTransferInput);
        AnnotationMirror resultAnno =
            calculateNumericalBinaryOp(
                n.getLeftOperand(),
                n.getRightOperand(),
                NumericalBinaryOps.MULTIPLICATION,
                cfValueCFStoreTransferInput);
        return createNewResult(transferResult, resultAnno);
    }

    /**
     * Create a new transfer result based on the original result and the new annotation.
     *
     * @param result the original result
     * @param resultAnno the new annotation
     * @return the new transfer result
     */
    private TransferResult<CFValue, CFStore> createNewResult(
        TransferResult<CFValue, CFStore> result, AnnotationMirror resultAnno) {
        CFValue newResultValue =
            analysis.createSingleAnnotationValue(
                resultAnno, result.getResultValue().getUnderlyingType());
        return new RegularTransferResult<>(newResultValue, result.getRegularStore());
    }

    /** Binary operations that are analyzed by the value checker. */
    enum NumericalBinaryOps {
        ADDITION,
        SUBTRACTION,
        DIVISION,
        REMAINDER,
        MULTIPLICATION,
        SHIFT_LEFT,
        SIGNED_SHIFT_RIGHT,
        UNSIGNED_SHIFT_RIGHT,
        BITWISE_AND,
        BITWISE_OR,
        BITWISE_XOR;
    }

    /**
     * Get the refined annotation after a numerical binary operation.
     *
     * @param leftNode the node that represents the left operand
     * @param rightNode the node that represents the right operand
     * @param op the operator type
     * @param p the transfer input
     * @return the result annotation mirror
     */
    private AnnotationMirror calculateNumericalBinaryOp(
        Node leftNode,
        Node rightNode,
        NumericalBinaryOps op,
        TransferInput<CFValue, CFStore> p) {
        Range resultRange = calculateRangeBinaryOp(leftNode, rightNode, op, p);
        return typeFactory.createIntRangeAnnotation(resultRange);
    }

    /** Calculate the result range after a binary operation between two numerical type nodes. */
    private Range calculateRangeBinaryOp(
        Node leftNode,
        Node rightNode,
        NumericalBinaryOps op,
        TransferInput<CFValue, CFStore> p) {
        if (TypesUtils.isIntegral(leftNode.getType())
            && TypesUtils.isIntegral(rightNode.getType())) {
            Range leftRange = getIntRange(leftNode, p);
            Range rightRange = getIntRange(rightNode, p);
            Range resultRange;
            switch (op) {
                case ADDITION:
                    resultRange = leftRange.plus(rightRange);
                    break;
                case SUBTRACTION:
                    resultRange = leftRange.minus(rightRange);
                    break;
                case MULTIPLICATION:
                    resultRange = leftRange.times(rightRange);
                    break;
                case DIVISION:
                    resultRange = leftRange.divide(rightRange);
                    break;
                case REMAINDER:
                    resultRange = leftRange.remainder(rightRange);
                    break;
                case SHIFT_LEFT:
                    resultRange = leftRange.shiftLeft(rightRange);
                    break;
                case SIGNED_SHIFT_RIGHT:
                    resultRange = leftRange.signedShiftRight(rightRange);
                    break;
                case UNSIGNED_SHIFT_RIGHT:
                    resultRange = leftRange.unsignedShiftRight(rightRange);
                    break;
                case BITWISE_AND:
                    resultRange = leftRange.bitwiseAnd(rightRange);
                    break;
                case BITWISE_OR:
                    resultRange = leftRange.bitwiseOr(rightRange);
                    break;
                case BITWISE_XOR:
                    resultRange = leftRange.bitwiseXor(rightRange);
                    break;
                default:
                    throw new BugInCF("ValueTransfer: unsupported operation: " + op);
            }
            // Any integral type with less than 32 bits would be promoted to 32-bit int type during
            // operations.
            return leftNode.getType().getKind() == TypeKind.LONG
                || rightNode.getType().getKind() == TypeKind.LONG
                ? resultRange
                : resultRange.intRange();
        } else {
            return Range.EVERYTHING;
        }
    }

    /** Get possible integer range from annotation. */
    private Range getIntRange(Node subNode, TransferInput<CFValue, CFStore> p) {
        AnnotationMirror val = getValueAnnotation(subNode, p);
        return getIntRangeFromAnnotation(subNode, val);
    }

    /**
     * Returns the {@link Range} object corresponding to the annotation {@code val} casted to the
     * type of {@code node}.
     *
     * @param node a node
     * @param val annotation mirror
     * @return the {@link Range} object corresponding to the annotation {@code val} casted to the
     *     type of {@code node}.
     */
    private Range getIntRangeFromAnnotation(Node node, AnnotationMirror val) {
        Range range;
        if (val == null
            || AnnotationUtils.areSameByName(val, typeFactory.UNKNOWNVAL)) {
            range = Range.EVERYTHING;
        } else if (typeFactory.isIntRange(val)) {
            range = ValueAnnotatedTypeFactory.getRange(val);
        } else if (AnnotationUtils.areSameByName(val, typeFactory.BOTTOMVAL)) {
            return Range.NOTHING;
        } else {
            range = Range.EVERYTHING;
        }
        return NumberUtils.castRange(node.getType(), range);
    }

    /**
     * Returns true if {@code node} an integral type and is {@code anno} is {@code @UnknownVal}.
     *
     * @param node a node
     * @param anno annotation mirror
     * @return true if node is annotated with {@code @UnknownVal} and it is an integral type.
     */
    private boolean isIntegralUnknownVal(Node node, AnnotationMirror anno) {
        return AnnotationUtils.areSameByName(anno, typeFactory.UNKNOWNVAL)
            && TypesUtils.isIntegral(node.getType());
    }

    private AnnotationMirror getValueAnnotation(Node subNode, TransferInput<CFValue, CFStore> p) {
        CFValue value = p.getValueOfSubNode(subNode);
        return getValueAnnotation(value);
    }

    /**
     * Extract the Value Checker annotation from a CFValue object.
     *
     * @param cfValue a CFValue object
     * @return the Value Checker annotation within cfValue
     */
    private AnnotationMirror getValueAnnotation(CFValue cfValue) {
        return typeFactory.getQualifierHierarchy().findAnnotationInHierarchy(
            cfValue.getAnnotations(), typeFactory.UNKNOWNVAL);
    }
}
