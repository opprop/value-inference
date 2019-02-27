package cast;

import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.value.ValueAnnotatedTypeFactory;
import org.checkerframework.common.value.ValueVisitor;
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

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.VariableTree;

public class CastVisitor extends ValueVisitor {

	/** The top type for this hierarchy. */
	protected final AnnotationMirror UNKNOWNVAL;

	public CastVisitor(BaseTypeChecker checker) {
		super(checker);
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
	public Void visitTypeCast(TypeCastTree node, Void p) {
		// type cast safety for byte casting is already being checked
		AnnotatedTypeMirror castType = atypeFactory.getAnnotatedType(node);
		if (castType.getUnderlyingType().getKind() != TypeKind.BYTE) {
			checkTypecastSafety(node, p);
		}

		return super.visitTypeCast(node, p);
	}

	@Override
	public Void visitAssignment(AssignmentTree node, Void p) {
		Set<Node> expr_nodes = atypeFactory.getNodesForTree(node.getExpression());
		if (expr_nodes != null) {
			for (Node n : expr_nodes) {
				if (n instanceof WideningConversionNode) {
					checkUnsafeWidening((WideningConversionNode) n, node);
				} else {
					visitNode(n);
				}
			}
		}
		return super.visitAssignment(node, p);
	}

	@Override
	public Void visitVariable(VariableTree node, Void p) {
		Set<Node> expr_nodes = atypeFactory.getNodesForTree(node.getInitializer());
		if (expr_nodes != null) {
			for (Node n : expr_nodes) {
				if (n instanceof WideningConversionNode) {
					checkUnsafeWidening((WideningConversionNode) n, node);
				} else {
					visitNode(n);
				}
			}
		}
		return super.visitVariable(node, p);
	}

	private void visitNode(Node n) {
		if (n instanceof BitwiseAndNode) {
			visitBitwiseAnd((BitwiseAndNode) n);
		} else if (n instanceof BinaryOperationNode) {
			visitBinaryOperation((BinaryOperationNode) n);
		}
	}

	private void visitBinaryOperation(BinaryOperationNode node) {
		Node leftNode = node.getLeftOperand();
		Node rightNode = node.getRightOperand();

		if (leftNode instanceof WideningConversionNode) {
			checkUnsafeWidening((WideningConversionNode) leftNode, node.getTree());
		}
		if (rightNode instanceof WideningConversionNode) {
			checkUnsafeWidening((WideningConversionNode) rightNode, node.getTree());
		}

		visitNode(leftNode);
		visitNode(rightNode);
	}

	private void visitBitwiseAnd(BitwiseAndNode node) {
		Node leftNode = node.getLeftOperand();
		Node rightNode = node.getRightOperand();

		if (leftNode instanceof WideningConversionNode && !rightNode.toString().equals("255")) {
			checkUnsafeWidening((WideningConversionNode) leftNode, node.getTree());
		}
		if (rightNode instanceof WideningConversionNode && !leftNode.toString().equals("255")) {
			checkUnsafeWidening((WideningConversionNode) rightNode, node.getTree());
		}

		visitNode(leftNode);
		visitNode(rightNode);
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
