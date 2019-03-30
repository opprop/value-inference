package cast;

import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;

import org.checkerframework.common.value.ValueAnnotatedTypeFactory;
import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.common.value.qual.UnknownVal;
import org.checkerframework.common.value.util.Range;
import org.checkerframework.dataflow.cfg.node.BinaryOperationNode;
import org.checkerframework.dataflow.cfg.node.BitwiseAndNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.WideningConversionNode;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;

import checkers.inference.InferenceChecker;
import checkers.inference.InferenceVisitor;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;

public class CastVisitor extends InferenceVisitor<CastChecker, BaseAnnotatedTypeFactory> {

	/** The top type for this hierarchy. */
	protected final AnnotationMirror UNKNOWNVAL;

	public CastVisitor(
            CastChecker checker,
            InferenceChecker ichecker,
            BaseAnnotatedTypeFactory factory,
            boolean infer) {
        super(checker, ichecker, factory, infer);
        UNKNOWNVAL = AnnotationBuilder.fromClass(elements, UnknownVal.class);
    }

	@Override
	protected CastAnnotatedTypeFactory createTypeFactory() {
		return new CastAnnotatedTypeFactory(checker);
	}

	/** Return true if this range contains unsigned part of {@code byte} value. */
	private boolean isUnsignedByte(Range range) {
		return !range.intersect(new Range(Byte.MAX_VALUE + 1, Byte.MAX_VALUE * 2 + 1)).isNothing()
				&& !range.contains(Range.BYTE_EVERYTHING);
	}
	
	@Override
    protected void commonAssignmentCheck(
            AnnotatedTypeMirror varType,
            AnnotatedTypeMirror valueType,
            Tree valueTree,
            String errorKey) {
    	
    	if (varType.getKind() != valueType.getKind()
    			&& valueType.getUnderlyingType().getKind() == TypeKind.BYTE) {
            AnnotationMirror valueAnno = valueType.getAnnotationInHierarchy(UNKNOWNVAL);
            if (AnnotationUtils.areSameByClass(valueAnno, IntRange.class)) {
	            Range range = ValueAnnotatedTypeFactory.getRange(valueAnno);
	        	if (isUnsignedByte(range)) {
	        		checker.report(Result.warning(errorKey, valueType, varType), valueTree);
	        	}
            }
        }
        super.commonAssignmentCheck(varType, valueType, valueTree, errorKey);
    }

	@Override
	public Void visitTypeCast(TypeCastTree node, Void p) {
		// type cast safety for byte casting is already being checked
		AnnotatedTypeMirror castType = atypeFactory.getAnnotatedType(node);
		if (castType.getUnderlyingType().getKind() != TypeKind.BYTE) {
			checkTypecastSafety(node, p);
		}

		return super.visitTypeCast(node, p);
	}

	@Override
	public Void visitBinary(BinaryTree node, Void p) {
        Set<Node> nodes = atypeFactory.getNodesForTree(node);
        for (Node n : nodes) {
        	if (n instanceof BinaryOperationNode)
        		visitBinaryOperation((BinaryOperationNode) n);
        	else if (n instanceof WideningConversionNode) {
        		checkUnsafeWidening((WideningConversionNode)n, node);
        	}
        }
        return super.visitBinary(node, p);
    }

	private void visitBinaryOperation(BinaryOperationNode node) {
		Node leftNode = node.getLeftOperand();
		Node rightNode = node.getRightOperand();

		if (leftNode instanceof WideningConversionNode
				&& (!(node instanceof BitwiseAndNode) || !rightNode.toString().equals("255"))) {
			checkUnsafeWidening((WideningConversionNode) leftNode, node.getTree());
		}
		if (rightNode instanceof WideningConversionNode
				&& (!(node instanceof BitwiseAndNode) || !leftNode.toString().equals("255"))) {
			checkUnsafeWidening((WideningConversionNode) rightNode, node.getTree());
		}
	}

	private void checkUnsafeWidening(WideningConversionNode node, Tree target) {
		if (isUnsighedByteWideningConversion(node)) {
			AnnotatedTypeMirror targetType = atypeFactory.getAnnotatedType(target);
			AnnotatedTypeMirror exprType = atypeFactory.getAnnotatedType(node.getTree());
			checker.report(Result.warning("cast.unsafe", exprType, targetType), target);
		}
	}

	private boolean isUnsighedByteWideningConversion(WideningConversionNode node) {
		if (node.getOperand().getType().getKind() == TypeKind.BYTE) {
			CFValue operandValue = atypeFactory.getInferredValueFor(node.getOperand().getTree());
			Set<AnnotationMirror> annos = operandValue.getAnnotations();
			for (AnnotationMirror anno : annos) {
				if (AnnotationUtils.areSameByClass(anno, IntRange.class)) {
					Range annoRange = ValueAnnotatedTypeFactory.getRange(anno);
					if (isUnsignedByte(annoRange)) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
