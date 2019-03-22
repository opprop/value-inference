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
import org.checkerframework.dataflow.cfg.node.NumericalAdditionNode;
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
		UNKNOWNVAL = AnnotationBuilder.fromClass(analysis.getTypeFactory().getElementUtils(), UnknownVal.class);
	}

}
