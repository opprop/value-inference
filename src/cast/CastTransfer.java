package cast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;

import org.checkerframework.common.value.ValueAnnotatedTypeFactory;
import org.checkerframework.common.value.ValueCheckerUtils;
import org.checkerframework.common.value.ValueTransfer;
import org.checkerframework.common.value.qual.BottomVal;
import org.checkerframework.common.value.qual.DoubleVal;
import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.common.value.qual.IntVal;
import org.checkerframework.common.value.qual.UnknownVal;
import org.checkerframework.common.value.util.NumberMath;
import org.checkerframework.common.value.util.NumberUtils;
import org.checkerframework.common.value.util.Range;
import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.BitwiseAndNode;
import org.checkerframework.dataflow.cfg.node.BitwiseOrNode;
import org.checkerframework.dataflow.cfg.node.BitwiseXorNode;
import org.checkerframework.dataflow.cfg.node.FloatingDivisionNode;
import org.checkerframework.dataflow.cfg.node.FloatingRemainderNode;
import org.checkerframework.dataflow.cfg.node.IntegerDivisionNode;
import org.checkerframework.dataflow.cfg.node.IntegerRemainderNode;
import org.checkerframework.dataflow.cfg.node.LeftShiftNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.NumericalAdditionNode;
import org.checkerframework.dataflow.cfg.node.NumericalMultiplicationNode;
import org.checkerframework.dataflow.cfg.node.NumericalSubtractionNode;
import org.checkerframework.dataflow.cfg.node.SignedRightShiftNode;
import org.checkerframework.dataflow.cfg.node.UnsignedRightShiftNode;
import org.checkerframework.dataflow.cfg.node.WideningConversionNode;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.BugInCF;
import org.checkerframework.javacutil.TypesUtils;

public class CastTransfer extends ValueTransfer {

	protected final CastAnnotatedTypeFactory atypefactory;
	protected final AnnotationMirror UNKNOWNVAL;

	public CastTransfer(CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
		super(analysis);
		atypefactory = (CastAnnotatedTypeFactory) analysis.getTypeFactory();
		UNKNOWNVAL = AnnotationBuilder.fromClass(analysis.getTypeFactory().getElementUtils(), UnknownVal.class);
	}

	@Override
	public TransferResult<CFValue, CFStore> visitWideningConversion(WideningConversionNode n,
			TransferInput<CFValue, CFStore> p) {
		TransferResult<CFValue, CFStore> result = super.visitWideningConversion(n, p);

		// Combine annotations from the operand with the wide type
		CFValue operandValue = p.getValueOfSubNode(n.getOperand());
		if (n.getOperand().getType().getKind() == TypeKind.BYTE) {
			Set<AnnotationMirror> annos = operandValue.getAnnotations();
			for (AnnotationMirror anno : annos) {
				if (AnnotationUtils.areSameByClass(anno, IntRange.class)
						|| AnnotationUtils.areSameByClass(anno, IntVal.class)) {
					Range annoRange = ValueAnnotatedTypeFactory.getRange(anno);
					if (isUnsignedByte(annoRange)) {
						result.setResultValue(operandValue);
						return result;
					}
				}
			}
		}

		CFValue widenedValue = getValueWithSameAnnotations(n.getType(), operandValue);
		result.setResultValue(widenedValue);

		return result;
	}
//
//	@Override
//	public TransferResult<CFValue, CFStore> visitNumericalAddition(NumericalAdditionNode n,
//			TransferInput<CFValue, CFStore> p) {
//		
//		TransferResult<CFValue, CFStore> result = super.visitNumericalAddition(n, p);
//		
//		AnnotationMirror leftAnno = atypefactory.getQualifierHierarchy().findAnnotationInHierarchy(p.getValueOfSubNode(n.getLeftOperand()).getAnnotations(), UNKNOWNVAL);
//		AnnotationMirror rightAnno = atypefactory.getQualifierHierarchy().findAnnotationInHierarchy(p.getValueOfSubNode(n.getRightOperand()).getAnnotations(), UNKNOWNVAL);
//		
//		if (AnnotationUtils.areSameByClass(leftAnno, IntRange.class) || AnnotationUtils.areSameByClass(leftAnno, UnknownVal.class)) {
//			Range leftRange = getIntRange(n.getLeftOperand(), p);
//		}
//		else if (AnnotationUtils.areSameByClass(leftAnno, IntVal.class)) {
//			List<? extends Number> lefts = getNumericalValues(n.getLeftOperand(), p);
//		}
//		
//		if (AnnotationUtils.areSameByClass(rightAnno, IntRange.class) || AnnotationUtils.areSameByClass(rightAnno, UnknownVal.class)) {
//	        Range rightRange = getIntRange(n.getRightOperand(), p);
//		}
//		else if (AnnotationUtils.areSameByClass(rightAnno, IntVal.class)) {
//	        List<? extends Number> rights = getNumericalValues(n.getRightOperand(), p);
//			
//		}
//		
//		return result;
//	}
//
//	@Override
//	public TransferResult<CFValue, CFStore> visitNumericalSubtraction(NumericalSubtractionNode n,
//			TransferInput<CFValue, CFStore> p) {
//		TransferResult<CFValue, CFStore> result = super.visitNumericalSubtraction(n, p);
//		return result;
//
//	}
//
//	@Override
//	public TransferResult<CFValue, CFStore> visitNumericalMultiplication(NumericalMultiplicationNode n,
//			TransferInput<CFValue, CFStore> p) {
//		TransferResult<CFValue, CFStore> result = super.visitNumericalMultiplication(n, p);
//		return result;
//
//	}
//
//	@Override
//	public TransferResult<CFValue, CFStore> visitIntegerDivision(IntegerDivisionNode n,
//			TransferInput<CFValue, CFStore> p) {
//		TransferResult<CFValue, CFStore> result = super.visitIntegerDivision(n, p);
//		return result;
//
//	}
//
//	@Override
//	public TransferResult<CFValue, CFStore> visitBitwiseAnd(BitwiseAndNode n, TransferInput<CFValue, CFStore> p) {
//		TransferResult<CFValue, CFStore> result = super.visitBitwiseAnd(n, p);
//		Range leftRange = getIntRange(n.getLeftOperand(), p);
//        Range rightRange = getIntRange(n.getRightOperand(), p);
//        
//        List<? extends Number> lefts = getNumericalValues(n.getLeftOperand(), p);
//        List<? extends Number> rights = getNumericalValues(n.getRightOperand(), p);
//        
//        return result;
//	}
//	
//	/**
//     * Returns a list of possible values, or null if no estimate is available and any value is
//     * possible.
//     */
//    private List<? extends Number> getNumericalValues(
//            Node subNode, TransferInput<CFValue, CFStore> p) {
//    	CFValue cfvalue = p.getValueOfSubNode(subNode);
//    	AnnotationMirror valueAnno = atypefactory
//                .getQualifierHierarchy()
//                .findAnnotationInHierarchy(cfvalue.getAnnotations(), UNKNOWNVAL);
//
//        if (valueAnno == null || AnnotationUtils.areSameByClass(valueAnno, UnknownVal.class)) {
//            return null;
//        } else if (AnnotationUtils.areSameByClass(valueAnno, BottomVal.class)) {
//            return new ArrayList<>();
//        }
//        List<? extends Number> values;
//        if (AnnotationUtils.areSameByClass(valueAnno, IntVal.class)) {
//            values = ValueAnnotatedTypeFactory.getIntValues(valueAnno);
//        } else if (AnnotationUtils.areSameByClass(valueAnno, DoubleVal.class)) {
//            values = ValueAnnotatedTypeFactory.getDoubleValues(valueAnno);
//        } else {
//            return null;
//        }
//        return NumberUtils.castNumbers(subNode.getType(), values);
//    }
//
//	/** Get possible integer range from annotation. */
//    private Range getIntRange(Node subNode, TransferInput<CFValue, CFStore> p) {
//    	CFValue cfvalue = p.getValueOfSubNode(subNode);
//    	AnnotationMirror val = atypefactory
//                .getQualifierHierarchy()
//                .findAnnotationInHierarchy(cfvalue.getAnnotations(), UNKNOWNVAL);
//    	Range range;
//        if (val == null || AnnotationUtils.areSameByClass(val, UnknownVal.class)) {
//            range = Range.EVERYTHING;
//        } else if (atypefactory.isIntRange(val)) {
//            range = ValueAnnotatedTypeFactory.getRange(val);
//        } else if (AnnotationUtils.areSameByClass(val, IntVal.class)) {
//            List<Long> values = ValueAnnotatedTypeFactory.getIntValues(val);
//            range = ValueCheckerUtils.getRangeFromValues(values);
//        } else if (AnnotationUtils.areSameByClass(val, DoubleVal.class)) {
//            List<Double> values = ValueAnnotatedTypeFactory.getDoubleValues(val);
//            range = ValueCheckerUtils.getRangeFromValues(values);
//        } else if (AnnotationUtils.areSameByClass(val, BottomVal.class)) {
//            return Range.NOTHING;
//        } else {
//            range = Range.EVERYTHING;
//        }
//        return NumberUtils.castRange(subNode.getType(), range);
//    }
//    
//	@Override
//	public TransferResult<CFValue, CFStore> visitBitwiseOr(BitwiseOrNode n, TransferInput<CFValue, CFStore> p) {
//		TransferResult<CFValue, CFStore> result = super.visitBitwiseOr(n, p);
//		return result;
//	}
//
//	@Override
//	public TransferResult<CFValue, CFStore> visitBitwiseXor(BitwiseXorNode n, TransferInput<CFValue, CFStore> p) {
//		TransferResult<CFValue, CFStore> result = super.visitBitwiseXor(n, p);
//		return result;
//	}

	/** Return true if this range contains unsigned part of {@code byte} value. */
	private boolean isUnsignedByte(Range range) {
		return !range.intersect(new Range(Byte.MAX_VALUE + 1, Byte.MAX_VALUE * 2 + 1)).isNothing()
				&& !range.contains(Range.BYTE_EVERYTHING);
	}
}
