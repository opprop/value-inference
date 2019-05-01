package value;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;

import org.checkerframework.common.value.ValueAnnotatedTypeFactory;
import org.checkerframework.common.value.qual.ArrayLen;
import org.checkerframework.common.value.qual.ArrayLenRange;
import org.checkerframework.common.value.qual.BoolVal;
import org.checkerframework.common.value.qual.DoubleVal;
import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.common.value.qual.IntRangeFromGTENegativeOne;
import org.checkerframework.common.value.qual.IntRangeFromNonNegative;
import org.checkerframework.common.value.qual.IntRangeFromPositive;
import org.checkerframework.common.value.qual.IntVal;
import org.checkerframework.common.value.qual.StringVal;
import org.checkerframework.common.value.qual.UnknownVal;
import org.checkerframework.common.value.util.NumberUtils;
import org.checkerframework.common.value.util.Range;
import org.checkerframework.dataflow.cfg.node.BinaryOperationNode;
import org.checkerframework.dataflow.cfg.node.BitwiseAndNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.WideningConversionNode;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.visitor.AnnotatedTypeScanner;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.Tree.Kind;

import checkers.inference.InferenceChecker;
import checkers.inference.InferenceVisitor;

import org.checkerframework.checker.compilermsgs.qual.CompilerMessageKey;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;

public class ValueVisitor extends InferenceVisitor<ValueChecker, BaseAnnotatedTypeFactory> {

	/** The top type for this hierarchy. */
	protected final AnnotationMirror UNKNOWNVAL;
	
	/** The maximum number of values allowed in an annotation's array. */
    protected static final int MAX_VALUES = 10;
    
    protected final ValueAnnotatedTypeFactory valueATF;
    
	public ValueVisitor(
            ValueChecker checker,
            InferenceChecker ichecker,
            BaseAnnotatedTypeFactory factory,
            boolean infer) {
        super(checker, ichecker, factory, infer);
        UNKNOWNVAL = AnnotationBuilder.fromClass(elements, UnknownVal.class);
        valueATF = (ValueAnnotatedTypeFactory) factory;
    }

	@Override
	protected ValueAnnotatedTypeFactory createTypeFactory() {
		return new ValueAnnotatedTypeFactory(checker);
	}

	/**
     * ValueVisitor overrides this method so that it does not have to check variables annotated with
     * the {@link IntRangeFromPositive} annotation, the {@link IntRangeFromNonNegative} annotation,
     * or the {@link IntRangeFromGTENegativeOne} annotation. This annotation is only introduced by
     * the Index Checker's lower bound annotations. It is safe to defer checking of these values to
     * the Index Checker because this is only introduced for explicitly-written {@code
     * org.checkerframework.checker.index.qual.Positive}, explicitly-written {@code
     * org.checkerframework.checker.index.qual.NonNegative}, and explicitly-written {@code
     * org.checkerframework.checker.index.qual.GTENegativeOne} annotations, which must be checked by
     * the Lower Bound Checker.
     *
     * @param varType the annotated type of the lvalue (usually a variable)
     * @param valueExp the AST node for the rvalue (the new value)
     * @param errorKey the error message to use if the check fails (must be a compiler message key,
     */
    @Override
    protected void commonAssignmentCheck(
            AnnotatedTypeMirror varType,
            ExpressionTree valueExp,
            @CompilerMessageKey String errorKey) {

        replaceSpecialIntRangeAnnotations(varType);
        super.commonAssignmentCheck(varType, valueExp, errorKey);
    }

    @Override
    protected void commonAssignmentCheck(
            AnnotatedTypeMirror varType,
            AnnotatedTypeMirror valueType,
            Tree valueTree,
            @CompilerMessageKey String errorKey) {

        replaceSpecialIntRangeAnnotations(varType);
        super.commonAssignmentCheck(varType, valueType, valueTree, errorKey);
    }

    /**
     * Return types for methods that are annotated with {@code @IntRangeFromX} annotations need to
     * be replaced with {@code @UnknownVal}. See the documentation on {@link
     * #commonAssignmentCheck(AnnotatedTypeMirror, ExpressionTree, String) commonAssignmentCheck}.
     *
     * <p>A separate override is necessary because checkOverride doesn't actually use the
     * commonAssignmentCheck.
     */
    @Override
    protected boolean checkOverride(
            MethodTree overriderTree,
            AnnotatedTypeMirror.AnnotatedExecutableType overrider,
            AnnotatedTypeMirror.AnnotatedDeclaredType overridingType,
            AnnotatedTypeMirror.AnnotatedExecutableType overridden,
            AnnotatedTypeMirror.AnnotatedDeclaredType overriddenType) {

        replaceSpecialIntRangeAnnotations(overrider);
        replaceSpecialIntRangeAnnotations(overridden);

        return super.checkOverride(
                overriderTree, overrider, overridingType, overridden, overriddenType);
    }

    /**
     * Replaces any {@code IntRangeFromX} annotations with {@code @UnknownVal}. This is used to
     * prevent these annotations from being required on the left hand side of assignments.
     *
     * @param varType an annotated type mirror that may contain IntRangeFromX annotations, which
     *     will be used on the lhs of an assignment or pseudo-assignment
     */
    private void replaceSpecialIntRangeAnnotations(AnnotatedTypeMirror varType) {
        AnnotatedTypeScanner<Void, Void> replaceSpecialIntRangeAnnotations =
                new AnnotatedTypeScanner<Void, Void>() {
                    @Override
                    protected Void scan(AnnotatedTypeMirror type, Void p) {
                        if (type.hasAnnotation(IntRangeFromPositive.class)
                                || type.hasAnnotation(IntRangeFromNonNegative.class)
                                || type.hasAnnotation(IntRangeFromGTENegativeOne.class)) {
                            type.replaceAnnotation(UNKNOWNVAL);
                        }
                        return super.scan(type, p);
                    }

                    @Override
                    public Void visitDeclared(AnnotatedDeclaredType type, Void p) {
                        // Skip type arguments.
                        if (type.getEnclosingType() != null) {
                            scan(type.getEnclosingType(), p);
                        }

                        return null;
                    }
                };
        replaceSpecialIntRangeAnnotations.visit(varType);
    }

    /**
     * Warns about malformed constant-value annotations.
     *
     * <p>Issues an error if any @IntRange annotation has its 'from' value greater than 'to' value.
     *
     * <p>Issues a warning if any constant-value annotation has &gt; MAX_VALUES arguments.
     */
    @Override
    public Void visitAnnotation(AnnotationTree node, Void p) {
        List<? extends ExpressionTree> args = node.getArguments();

        if (args.isEmpty()) {
            // Nothing to do if there are no annotation arguments.
            return super.visitAnnotation(node, p);
        }

        AnnotationMirror anno = TreeUtils.annotationFromAnnotationTree(node);

        if (AnnotationUtils.areSameByClass(anno, IntRange.class)) {
            // If there are 2 arguments, issue an error if from.greater.than.to.
            // If there are fewer than 2 arguments, we needn't worry about this problem because the
            // other argument will be defaulted to Long.MIN_VALUE or Long.MAX_VALUE accordingly.
            if (args.size() == 2) {
                long from = AnnotationUtils.getElementValue(anno, "from", Long.class, true);
                long to = AnnotationUtils.getElementValue(anno, "to", Long.class, true);
                if (from > to) {
                    checker.report(Result.failure("from.greater.than.to"), node);
                    return null;
                }
            }
        } else if (AnnotationUtils.areSameByClass(anno, ArrayLen.class)
                || AnnotationUtils.areSameByClass(anno, BoolVal.class)
                || AnnotationUtils.areSameByClass(anno, DoubleVal.class)
                || AnnotationUtils.areSameByClass(anno, IntVal.class)
                || AnnotationUtils.areSameByClass(anno, StringVal.class)) {
            List<Object> values =
                    AnnotationUtils.getElementValueArray(anno, "value", Object.class, true);

            if (values.isEmpty()) {
                checker.report(Result.warning("no.values.given"), node);
                return null;
            } else if (values.size() > MAX_VALUES) {
                checker.report(
                        Result.warning(
                                (AnnotationUtils.areSameByClass(anno, IntVal.class)
                                        ? "too.many.values.given.int"
                                        : "too.many.values.given"), MAX_VALUES),
                        node);
                return null;
            } else if (AnnotationUtils.areSameByClass(anno, ArrayLen.class)) {
                List<Integer> arrayLens = ValueAnnotatedTypeFactory.getArrayLength(anno);
                if (Collections.min(arrayLens) < 0) {
                    checker.report(
                            Result.warning("negative.arraylen", Collections.min(arrayLens)), node);
                    return null;
                }
            }
        } else if (AnnotationUtils.areSameByClass(anno, ArrayLenRange.class)) {
            int from = AnnotationUtils.getElementValue(anno, "from", Integer.class, true);
            int to = AnnotationUtils.getElementValue(anno, "to", Integer.class, true);
            if (from > to) {
                checker.report(Result.failure("from.greater.than.to"), node);
                return null;
            } else if (from < 0) {
                checker.report(Result.warning("negative.arraylen", from), node);
                return null;
            }
        }

        return super.visitAnnotation(node, p);
    }

    @Override
    public Void visitTypeCast(TypeCastTree node, Void p) {
        if (node.getExpression().getKind() == Kind.NULL_LITERAL) {
            return null;
        }

        AnnotatedTypeMirror castType = valueATF.getAnnotatedType(node);
        AnnotationMirror castAnno = castType.getAnnotationInHierarchy(UNKNOWNVAL);
        AnnotationMirror exprAnno =
        		valueATF
                        .getAnnotatedType(node.getExpression())
                        .getAnnotationInHierarchy(UNKNOWNVAL);

        // It is always legal to cast to an IntRange type that includes all values
        // of the underlying type. Do not warn about such casts.
        // I.e. do not warn if an @IntRange(...) int is casted
        // to a @IntRange(from = Byte.MIN_VALUE, to = Byte.MAX_VALUE byte).
        if (castAnno != null
                && exprAnno != null
                && valueATF.isIntRange(castAnno)
                && valueATF.isIntRange(exprAnno)) {
            Range castRange = ValueAnnotatedTypeFactory.getRange(castAnno);
            if (castType.getKind() == TypeKind.BYTE && castRange.isByteEverything()) {
                return p;
            }
            if (castType.getKind() == TypeKind.CHAR && castRange.isCharEverything()) {
                return p;
            }
            if (castType.getKind() == TypeKind.SHORT && castRange.isShortEverything()) {
                return p;
            }
            if (castType.getKind() == TypeKind.INT && castRange.isIntEverything()) {
                return p;
            }
            if (castType.getKind() == TypeKind.LONG && castRange.isLongEverything()) {
                return p;
            }
            if (Range.ignoreOverflow) {
                // Range.ignoreOverflow is only set if this checker is ignoring overflow.
                // In that case, do not warn if the range of the expression encompasses
                // the whole type being casted to (i.e. the warning is actually about overflow).
                Range exprRange = ValueAnnotatedTypeFactory.getRange(exprAnno);
                TypeKind casttypekind = castType.getKind();
                if (casttypekind == TypeKind.BYTE
                        || casttypekind == TypeKind.CHAR
                        || casttypekind == TypeKind.SHORT
                        || casttypekind == TypeKind.INT) {
                    exprRange = NumberUtils.castRange(castType.getUnderlyingType(), exprRange);
                }
                if (castRange.equals(exprRange)) {
                    return p;
                }
            }
        }
        return super.visitTypeCast(node, p);
    }

    /**
     * Overridden to issue errors at the appropriate place if an {@code IntRange} or {@code
     * ArrayLenRange} annotation has {@code from > to}. {@code from > to} either indicates a user
     * error when writing an annotation or an error in the checker's implementation, as {@code from}
     * should always be {@code <= to}.
     */
    @Override
    public boolean validateType(Tree tree, AnnotatedTypeMirror type) {
        boolean result = super.validateType(tree, type);
        if (!result) {
            AnnotationMirror anno = type.getAnnotationInHierarchy(UNKNOWNVAL);
            if (AnnotationUtils.areSameByClass(anno, IntRange.class)) {
                long to = valueATF.getToValueFromIntRange(type);
                long from = valueATF.getFromValueFromIntRange(type);
                if (from > to) {
                    checker.report(Result.failure("from.greater.than.to"), tree);
                    return false;
                }
            } else if (AnnotationUtils.areSameByClass(anno, ArrayLenRange.class)) {
                int from = AnnotationUtils.getElementValue(anno, "from", Integer.class, true);
                int to = AnnotationUtils.getElementValue(anno, "to", Integer.class, true);
                if (from > to) {
                    checker.report(Result.failure("from.greater.than.to"), tree);
                    return false;
                }
            }
        }
        return result;
    }
}
