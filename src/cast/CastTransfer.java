package cast;

import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;

import org.checkerframework.common.value.ValueAnnotatedTypeFactory;
import org.checkerframework.common.value.ValueTransfer;
import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.common.value.qual.IntVal;
import org.checkerframework.common.value.qual.UnknownVal;
import org.checkerframework.common.value.util.Range;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.WideningConversionNode;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

public class CastTransfer extends ValueTransfer {

	protected final CastAnnotatedTypeFactory atypefactory;
    protected final AnnotationMirror UNKNOWNVAL;
	
	public CastTransfer(CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
		super(analysis);
		atypefactory = (CastAnnotatedTypeFactory) analysis.getTypeFactory();
		UNKNOWNVAL = AnnotationBuilder.fromClass(analysis.getTypeFactory()
                .getElementUtils(), UnknownVal.class);
	}
	
	@Override
    public TransferResult<CFValue, CFStore> visitWideningConversion(
            WideningConversionNode n, TransferInput<CFValue, CFStore> p) {
		TransferResult<CFValue, CFStore> result = super.visitWideningConversion(n, p);
		
        // Combine annotations from the operand with the wide type
		CFValue operandValue = p.getValueOfSubNode(n.getOperand());
		
		if (n.getOperand().getType().getKind() == TypeKind.BYTE) {
			Set<AnnotationMirror> annos = operandValue.getAnnotations();
			for (AnnotationMirror anno : annos) {
				if (AnnotationUtils.areSameByClass(anno, IntRange.class) || 
						AnnotationUtils.areSameByClass(anno, IntVal.class)) {
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
	
	/** Return true if this range contains unsigned part of {@code byte} value. */
    private boolean isUnsignedByte(Range range) {
        return !range.intersect(new Range(Byte.MAX_VALUE + 1, Byte.MAX_VALUE * 2 + 1)).isNothing()
        		&& !range.contains(Range.BYTE_EVERYTHING);
    }
}
