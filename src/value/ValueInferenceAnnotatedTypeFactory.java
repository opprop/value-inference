package value;

import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferenceChecker;
import checkers.inference.InferenceQualifierHierarchy;
import checkers.inference.InferenceTreeAnnotator;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.VariableAnnotator;
import checkers.inference.model.ConstraintManager;
import checkers.inference.model.VariableSlot;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.LiteralTree;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.value.ValueCheckerUtils;
import org.checkerframework.common.value.util.Range;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.Pair;
import org.checkerframework.javacutil.TreeUtils;

import value.qual.BoolVal;
import value.qual.BottomVal;
import value.qual.IntRange;
import value.qual.StringVal;
import value.qual.UnknownVal;

public class ValueInferenceAnnotatedTypeFactory extends InferenceAnnotatedTypeFactory {

    /** The top type for this hierarchy. */
    protected final AnnotationMirror UNKNOWNVAL =
            AnnotationBuilder.fromClass(elements, UnknownVal.class);

    /** The bottom type for this hierarchy. */
    protected final AnnotationMirror BOTTOMVAL =
            AnnotationBuilder.fromClass(elements, BottomVal.class);

    public ValueInferenceAnnotatedTypeFactory(
            InferenceChecker inferenceChecker,
            boolean withCombineConstraints,
            BaseAnnotatedTypeFactory realTypeFactory,
            InferrableChecker realChecker,
            SlotManager slotManager,
            ConstraintManager constraintManager) {
        super(
                inferenceChecker,
                withCombineConstraints,
                realTypeFactory,
                realChecker,
                slotManager,
                constraintManager);
        postInit();
    }

    @Override
    public TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(
                new ValueInferencePropagationTreeAnnotator(this),
                new ValueInferenceTreeAnnotator(
                        this, realChecker, realTypeFactory, variableAnnotator, slotManager));
    }

    protected class ValueInferenceTreeAnnotator extends InferenceTreeAnnotator {
        public ValueInferenceTreeAnnotator(
                InferenceAnnotatedTypeFactory atypeFactory,
                InferrableChecker realChecker,
                AnnotatedTypeFactory realAnnotatedTypeFactory,
                VariableAnnotator variableAnnotator,
                SlotManager slotManager) {
            super(
                    atypeFactory,
                    realChecker,
                    realAnnotatedTypeFactory,
                    variableAnnotator,
                    slotManager);
        }
    }

    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new ValueInferenceQualifierHierarchy(factory);
    }

    private final class ValueInferenceQualifierHierarchy extends InferenceQualifierHierarchy {
        public ValueInferenceQualifierHierarchy(MultiGraphFactory multiGraphFactory) {
            super(multiGraphFactory);
        }
    }

    /**
     * The domain of the Constant Value Checker: the types for which it estimates possible values.
     */
    protected static final Set<String> COVERED_CLASS_STRINGS =
            Collections.unmodifiableSet(
                    new HashSet<>(
                            Arrays.asList(
                                    "int",
                                    "java.lang.Integer",
                                    "double",
                                    "java.lang.Double",
                                    "byte",
                                    "java.lang.Byte",
                                    "java.lang.String",
                                    "char",
                                    "java.lang.Character",
                                    "float",
                                    "java.lang.Float",
                                    "boolean",
                                    "java.lang.Boolean",
                                    "long",
                                    "java.lang.Long",
                                    "short",
                                    "java.lang.Short",
                                    "char[]")));

    @Override
    public VariableAnnotator createVariableAnnotator() {
        return new ValueVariableAnnotator(
                this, realTypeFactory, realChecker, slotManager, constraintManager);
    }
    
    private final class ValueVariableAnnotator extends VariableAnnotator {
        public ValueVariableAnnotator(
                InferenceAnnotatedTypeFactory typeFactory,
                AnnotatedTypeFactory realTypeFactory,
                InferrableChecker realChecker,
                SlotManager slotManager,
                ConstraintManager constraintManager) {
            super(typeFactory, realTypeFactory, realChecker, slotManager, constraintManager);
        }

        @Override
        public void handleBinaryTree(AnnotatedTypeMirror atm, BinaryTree binaryTree) {
        	// Super creates an LUB constraint by default, we create an VariableSlot here
            // instead for the result of the binary op and create LUB constraint

            // create varslot for the result of the binary tree computation
            // note: constraints for binary ops are added in Visitor
            if (this.treeToVarAnnoPair.containsKey(binaryTree)) {
                atm.replaceAnnotations((Iterable)((Pair)this.treeToVarAnnoPair.get(binaryTree)).second);
            } else {
                AnnotatedTypeMirror lhsATM = this.inferenceTypeFactory.getAnnotatedType(binaryTree.getLeftOperand());
                AnnotatedTypeMirror rhsATM = this.inferenceTypeFactory.getAnnotatedType(binaryTree.getRightOperand());
                AnnotationMirror lhsAM = lhsATM.getEffectiveAnnotationInHierarchy(UNKNOWNVAL);
                AnnotationMirror rhsAM = rhsATM.getEffectiveAnnotationInHierarchy(UNKNOWNVAL);
                // grab slots for the component (only for lub slot)
                VariableSlot lhs = slotManager.getVariableSlot(lhsATM);
                VariableSlot rhs = slotManager.getVariableSlot(rhsATM);

                VariableSlot result;
                switch (binaryTree.getKind()) {
                    case PLUS:
                    	if (TreeUtils.isStringConcatenation(binaryTree)) {
                			result = slotManager.createConstantSlot(AnnotationBuilder.fromClass(elements, StringVal.class));
                			break;
                        }
                    	if (lhsAM == null || rhsAM == null) {
                    		result = slotManager.createArithmeticVariableSlot(VariableAnnotator.treeToLocation(inferenceTypeFactory, binaryTree));
                            break;
                        }
                    	if (AnnotationUtils.areSameByClass(lhsAM, IntRange.class)
                                && AnnotationUtils.areSameByClass(rhsAM, IntRange.class)) {
                        	Range range = getRange(lhsAM).plus(getRange(rhsAM));
                        	result = slotManager.createConstantSlot(createIntRangeAnnotation(range));
                        } else {
                            result = slotManager.createLubVariableSlot(lhs, rhs);
                        }
                    	break;
                    case MINUS:
                    	if (lhsAM == null || rhsAM == null) {
                        	result = slotManager.createArithmeticVariableSlot(VariableAnnotator.treeToLocation(inferenceTypeFactory, binaryTree));
                            break;
                        }
                    	if (AnnotationUtils.areSameByClass(lhsAM, IntRange.class)
                                && AnnotationUtils.areSameByClass(rhsAM, IntRange.class)) {
                        	Range range = getRange(lhsAM).minus(getRange(rhsAM));
                        	result = slotManager.createConstantSlot(createIntRangeAnnotation(range));
                        } else {
                            result = slotManager.createLubVariableSlot(lhs, rhs);
                        }
                    	break;
                    case MULTIPLY:
                    	if (lhsAM == null || rhsAM == null) {
                        	result = slotManager.createArithmeticVariableSlot(VariableAnnotator.treeToLocation(inferenceTypeFactory, binaryTree));
                            break;
                        }
                    	if (AnnotationUtils.areSameByClass(lhsAM, IntRange.class)
                                && AnnotationUtils.areSameByClass(rhsAM, IntRange.class)) {
                        	Range range = getRange(lhsAM).times(getRange(rhsAM));
                        	result = slotManager.createConstantSlot(createIntRangeAnnotation(range));
                        } else {
                            result = slotManager.createLubVariableSlot(lhs, rhs);
                        }
                    	break;
                    case DIVIDE:
                    	if (lhsAM == null || rhsAM == null) {
                        	result = slotManager.createArithmeticVariableSlot(VariableAnnotator.treeToLocation(inferenceTypeFactory, binaryTree));
                            break;
                        }
                    	if (AnnotationUtils.areSameByClass(lhsAM, IntRange.class)
                                && AnnotationUtils.areSameByClass(rhsAM, IntRange.class)) {
                        	Range range = getRange(lhsAM).divide(getRange(rhsAM));
                        	result = slotManager.createConstantSlot(createIntRangeAnnotation(range));
                        } else {
                            result = slotManager.createLubVariableSlot(lhs, rhs);
                        }
                    	break;
                    case REMAINDER:
                    	if (lhsAM == null || rhsAM == null) {
                        	result = slotManager.createArithmeticVariableSlot(VariableAnnotator.treeToLocation(inferenceTypeFactory, binaryTree));
                            break;
                        }
                    	if (AnnotationUtils.areSameByClass(lhsAM, IntRange.class)
                                && AnnotationUtils.areSameByClass(rhsAM, IntRange.class)) {
                        	Range range = getRange(lhsAM).remainder(getRange(rhsAM));
                        	result = slotManager.createConstantSlot(createIntRangeAnnotation(range));
                        } else {
                            result = slotManager.createLubVariableSlot(lhs, rhs);
                        }
                    	break;
                    case LEFT_SHIFT:
                    	if (lhsAM == null || rhsAM == null) {
                    		result = slotManager.createLubVariableSlot(lhs, rhs);
                            break;
                        }
                    	if (AnnotationUtils.areSameByClass(lhsAM, IntRange.class)
                                && AnnotationUtils.areSameByClass(rhsAM, IntRange.class)) {
                        	Range range = getRange(lhsAM).shiftLeft(getRange(rhsAM));
                        	result = slotManager.createConstantSlot(createIntRangeAnnotation(range));
                        } else {
                            result = slotManager.createLubVariableSlot(lhs, rhs);
                        }
                    	break;
                    case RIGHT_SHIFT:
                    	if (lhsAM == null || rhsAM == null) {
                    		result = slotManager.createLubVariableSlot(lhs, rhs);
                            break;
                        }
                    	if (AnnotationUtils.areSameByClass(lhsAM, IntRange.class)
                                && AnnotationUtils.areSameByClass(rhsAM, IntRange.class)) {
                        	Range range = getRange(lhsAM).signedShiftRight(getRange(rhsAM));
                        	result = slotManager.createConstantSlot(createIntRangeAnnotation(range));
                        } else {
                            result = slotManager.createLubVariableSlot(lhs, rhs);
                        }
                    	break;
                    case UNSIGNED_RIGHT_SHIFT:
                    	if (lhsAM == null || rhsAM == null) {
                    		result = slotManager.createLubVariableSlot(lhs, rhs);
                            break;
                        }
                    	if (AnnotationUtils.areSameByClass(lhsAM, IntRange.class)
                                && AnnotationUtils.areSameByClass(rhsAM, IntRange.class)) {
                        	Range range = getRange(lhsAM).unsignedShiftRight(getRange(rhsAM));
                        	result = slotManager.createConstantSlot(createIntRangeAnnotation(range));
                        } else {
                            result = slotManager.createLubVariableSlot(lhs, rhs);
                        }
                    	break;
                    case AND:
                    	if (lhsAM == null || rhsAM == null) {
                    		result = slotManager.createLubVariableSlot(lhs, rhs);
                            break;
                        }
                    	if (AnnotationUtils.areSameByClass(lhsAM, IntRange.class)
                                && AnnotationUtils.areSameByClass(rhsAM, IntRange.class)) {
                        	Range range = getRange(lhsAM).bitwiseAnd(getRange(rhsAM));
                        	result = slotManager.createConstantSlot(createIntRangeAnnotation(range));
                        } else {
                            result = slotManager.createLubVariableSlot(lhs, rhs);
                        }
                    	break;
                    case OR:
                    	if (lhsAM == null || rhsAM == null) {
                    		result = slotManager.createLubVariableSlot(lhs, rhs);
                            break;
                        }
                    	if (AnnotationUtils.areSameByClass(lhsAM, IntRange.class)
                                && AnnotationUtils.areSameByClass(rhsAM, IntRange.class)) {
                        	Range range = getRange(lhsAM).bitwiseOr(getRange(rhsAM));
                        	result = slotManager.createConstantSlot(createIntRangeAnnotation(range));
                        } else {
                            result = slotManager.createLubVariableSlot(lhs, rhs);
                        }
                    	break;
                    case XOR:
                    	if (lhsAM == null || rhsAM == null) {
                    		result = slotManager.createLubVariableSlot(lhs, rhs);
                            break;
                        }
                    	if (AnnotationUtils.areSameByClass(lhsAM, IntRange.class)
                                && AnnotationUtils.areSameByClass(rhsAM, IntRange.class)) {
                        	Range range = getRange(lhsAM).bitwiseXor(getRange(rhsAM));
                        	result = slotManager.createConstantSlot(createIntRangeAnnotation(range));
                        } else {
                            result = slotManager.createLubVariableSlot(lhs, rhs);
                        }
                    	break;
                    case GREATER_THAN:
                    	if (lhsAM == null || rhsAM == null) {
                    		result = slotManager.createLubVariableSlot(lhs, rhs);
                            break;
                        }
                    	if (AnnotationUtils.areSameByClass(lhsAM, IntRange.class)
                                && AnnotationUtils.areSameByClass(rhsAM, IntRange.class)) {
                        	Range range = getRange(lhsAM).refineGreaterThan(getRange(rhsAM));
                        	result = slotManager.createConstantSlot(createIntRangeAnnotation(range));
                        } else {
                            result = slotManager.createLubVariableSlot(lhs, rhs);
                        }
                    	break;
                    case GREATER_THAN_EQUAL:
                    	if (lhsAM == null || rhsAM == null) {
                    		result = slotManager.createLubVariableSlot(lhs, rhs);
                            break;
                        }
                    	if (AnnotationUtils.areSameByClass(lhsAM, IntRange.class)
                                && AnnotationUtils.areSameByClass(rhsAM, IntRange.class)) {
                        	Range range = getRange(lhsAM).refineGreaterThanEq(getRange(rhsAM));
                        	result = slotManager.createConstantSlot(createIntRangeAnnotation(range));
                        } else {
                            result = slotManager.createLubVariableSlot(lhs, rhs);
                        }
                    	break;
                    case LESS_THAN:
                    	if (lhsAM == null || rhsAM == null) {
                    		result = slotManager.createLubVariableSlot(lhs, rhs);
                            break;
                        }
                    	if (AnnotationUtils.areSameByClass(lhsAM, IntRange.class)
                                && AnnotationUtils.areSameByClass(rhsAM, IntRange.class)) {
                        	Range range = getRange(lhsAM).refineLessThan(getRange(rhsAM));
                        	result = slotManager.createConstantSlot(createIntRangeAnnotation(range));
                        } else {
                            result = slotManager.createLubVariableSlot(lhs, rhs);
                        }
                    	break;
                    case LESS_THAN_EQUAL:
                    	if (lhsAM == null || rhsAM == null) {
                    		result = slotManager.createLubVariableSlot(lhs, rhs);
                            break;
                        }
                    	if (AnnotationUtils.areSameByClass(lhsAM, IntRange.class)
                                && AnnotationUtils.areSameByClass(rhsAM, IntRange.class)) {
                        	Range range = getRange(lhsAM).refineLessThanEq(getRange(rhsAM));
                        	result = slotManager.createConstantSlot(createIntRangeAnnotation(range));
                        } else {
                            result = slotManager.createLubVariableSlot(lhs, rhs);
                        }
                    	break;
                    case EQUAL_TO:
                    	if (lhsAM == null || rhsAM == null) {
                    		result = slotManager.createLubVariableSlot(lhs, rhs);
                            break;
                        }
                    	if (AnnotationUtils.areSameByClass(lhsAM, IntRange.class)
                                && AnnotationUtils.areSameByClass(rhsAM, IntRange.class)) {
                        	Range range = getRange(lhsAM).refineEqualTo(getRange(rhsAM));
                        	result = slotManager.createConstantSlot(createIntRangeAnnotation(range));
                        } else {
                            result = slotManager.createLubVariableSlot(lhs, rhs);
                        }
                    	break;
                    case NOT_EQUAL_TO:
                    	if (lhsAM == null || rhsAM == null) {
                    		result = slotManager.createLubVariableSlot(lhs, rhs);
                            break;
                        }
                    	if (AnnotationUtils.areSameByClass(lhsAM, IntRange.class)
                                && AnnotationUtils.areSameByClass(rhsAM, IntRange.class)) {
                        	Range range = getRange(lhsAM).refineNotEqualTo(getRange(rhsAM));
                        	result = slotManager.createConstantSlot(createIntRangeAnnotation(range));
                        } else {
                            result = slotManager.createLubVariableSlot(lhs, rhs);
                        }
                    	break;
                    default:
                        result = slotManager.createLubVariableSlot(lhs, rhs);
                        break;
                }

                // insert varAnnot of the slot into the ATM
                AnnotationMirror resultAM = slotManager.getAnnotation(result);
                atm.clearAnnotations();
                atm.replaceAnnotation(resultAM);

                Set<AnnotationMirror> resultSet = AnnotationUtils.createAnnotationSet();
                resultSet.add(resultAM);
                final Pair<VariableSlot, Set<? extends AnnotationMirror>> varATMPair = Pair.of(slotManager.getVariableSlot(atm), resultSet);
                treeToVarAnnoPair.put(binaryTree, varATMPair);
            }
        }
    }
   
    private final class ValueInferencePropagationTreeAnnotator extends PropagationTreeAnnotator {
        public ValueInferencePropagationTreeAnnotator(AnnotatedTypeFactory factory) {
            super(factory);
        }

        @Override
        public Void visitLiteral(LiteralTree tree, AnnotatedTypeMirror type) {
            if (!handledByValueChecker(type)) {
                return null;
            }
            Object value = tree.getValue();
            switch (tree.getKind()) {
                case BOOLEAN_LITERAL:
                    AnnotationMirror boolAnno =
                            createBooleanAnnotation(Collections.singletonList((Boolean) value));
                    type.replaceAnnotation(boolAnno);
                    return null;

                case CHAR_LITERAL:
                    AnnotationMirror charAnno =
                            createCharAnnotation(Collections.singletonList((Character) value));
                    type.replaceAnnotation(charAnno);
                    return null;

                case DOUBLE_LITERAL:
                case FLOAT_LITERAL:
                case INT_LITERAL:
                case LONG_LITERAL:
                    AnnotationMirror numberAnno =
                            createNumberAnnotationMirror(Collections.singletonList((Number) value));
                    type.replaceAnnotation(numberAnno);
                    return null;
                case STRING_LITERAL:
                    AnnotationMirror stringAnno =
                            createStringAnnotation(Collections.singletonList((String) value));
                    type.replaceAnnotation(stringAnno);
                    return null;
                default:
                    return null;
            }
        }

        /** Returns true iff the given type is in the domain of the Constant Value Checker. */
        private boolean handledByValueChecker(AnnotatedTypeMirror type) {
            TypeMirror tm = type.getUnderlyingType();
            return COVERED_CLASS_STRINGS.contains(tm.toString());
        }
    }

    /**
     * Returns a {@link BoolVal} annotation using the values. If {@code values} is null, then
     * UnknownVal is returned; if {@code values} is empty, then bottom is returned. The values are
     * sorted and duplicates are removed before the annotation is created.
     *
     * @param values list of booleans; duplicates are allowed and the values may be in any order
     * @return a {@link BoolVal} annotation using the values
     */
    public AnnotationMirror createBooleanAnnotation(List<Boolean> values) {
        if (values == null) {
            return UNKNOWNVAL;
        }
        if (values.isEmpty()) {
            return BOTTOMVAL;
        }

        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, BoolVal.class);
        return builder.build();
    }

    /** @param values must be a homogeneous list: every element of it has the same class. */
    public AnnotationMirror createNumberAnnotationMirror(List<Number> values) {
        if (values == null) {
            return UNKNOWNVAL;
        } else if (values.isEmpty()) {
            return BOTTOMVAL;
        }
        Number first = values.get(0);
        if (first instanceof Integer
                || first instanceof Short
                || first instanceof Long
                || first instanceof Byte) {
            List<Long> intValues = new ArrayList<>();
            for (Number number : values) {
                intValues.add(number.longValue());
            }
            return createIntValAnnotation(intValues);
        }
        throw new UnsupportedOperationException(
                "ValueAnnotatedTypeFactory: unexpected class: " + first.getClass());
    }

    /**
     * Returns a {@link StringVal} annotation using the values. If {@code values} is null, then
     * UnknownVal is returned; if {@code values} is empty, then bottom is returned. The values are
     * sorted and duplicates are removed before the annotation is created. If values is larger than
     * the max number of values allowed (10 by default), then an {@link ArrayLen} or an {@link
     * ArrayLenRange} annotation is returned.
     *
     * @param values list of strings; duplicates are allowed and the values may be in any order
     * @return a {@link StringVal} annotation using the values
     */
    public AnnotationMirror createStringAnnotation(List<String> values) {
        if (values == null) {
            return UNKNOWNVAL;
        }
        if (values.isEmpty()) {
            return BOTTOMVAL;
        }
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, StringVal.class);
        return builder.build();
    }

    /**
     * Returns a {@link IntVal} annotation using the values. If {@code values} is null, then
     * UnknownVal is returned; if {@code values} is empty, then bottom is returned. The values are
     * sorted and duplicates are removed before the annotation is created.
     *
     * @param values list of characters; duplicates are allowed and the values may be in any order
     * @return a {@link IntVal} annotation using the values
     */
    public AnnotationMirror createCharAnnotation(List<Character> values) {
        if (values == null) {
            return UNKNOWNVAL;
        }
        if (values.isEmpty()) {
            return BOTTOMVAL;
        }
        List<Long> longValues = new ArrayList<>();
        for (char value : values) {
            longValues.add((long) value);
        }
        return createIntValAnnotation(longValues);
    }

    /**
     * Returns a {@link IntVal} or {@link IntRange} annotation using the values. If {@code values}
     * is null, then UnknownVal is returned; if {@code values} is empty, then bottom is returned. If
     * the number of {@code values} is greater than MAX_VALUES, return an {@link IntRange}. In other
     * cases, the values are sorted and duplicates are removed before an {@link IntVal} is created.
     *
     * @param values list of longs; duplicates are allowed and the values may be in any order
     * @return an annotation depends on the values
     */
    public AnnotationMirror createIntValAnnotation(List<Long> values) {
        if (values == null) {
            return UNKNOWNVAL;
        }
        if (values.isEmpty()) {
            return BOTTOMVAL;
        }
        long valMin = Collections.min(values);
        long valMax = Collections.max(values);
        return createIntRangeAnnotation(valMin, valMax);
    }

    /**
     * Create an {@code @IntRange} annotation from the two (inclusive) bounds. Does not return
     * BOTTOMVAL or UNKNOWNVAL.
     */
    private AnnotationMirror createIntRangeAnnotation(long from, long to) {
        assert from <= to;
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, IntRange.class);
        builder.setValue("from", from);
        builder.setValue("to", to);
        return builder.build();
    }
    
    /**
     * Create an {@code @IntRange} or {@code @IntVal} annotation from the range. May return
     * BOTTOMVAL or UNKNOWNVAL.
     */
    public AnnotationMirror createIntRangeAnnotation(Range range) {
        if (range.isNothing()) {
            return BOTTOMVAL;
        } else if (range.isLongEverything()) {
            return UNKNOWNVAL;
        } else {
            return createIntRangeAnnotation(range.from, range.to);
        }
    }

    /**
     * If {@code anno} is equalient to UnknownVal, return UnknownVal; otherwise, return {@code
     * anno}.
     */
    private AnnotationMirror convertToUnknown(AnnotationMirror anno) {
        if (AnnotationUtils.areSameByClass(anno, IntRange.class)) {
            long from = AnnotationUtils.getElementValue(anno, "from", Long.class, true);
            long to = AnnotationUtils.getElementValue(anno, "to", Long.class, true);
            if (from == Long.MIN_VALUE && to == Long.MAX_VALUE) {
                return UNKNOWNVAL;
            }
        }
        return anno;
    }
    
    /**
     * Returns a {@code Range} bounded by the values specified in the given {@code @Range}
     * annotation. Also returns an appropriate range if an {@code @IntVal} annotation is passed.
     * Returns {@code null} if the annotation is null or if the annotation is not an {@code
     * IntRange}, {@code IntRangeFromPositive}, {@code IntVal}, or {@code ArrayLenRange}.
     */
    public static Range getRange(AnnotationMirror rangeAnno) {
        if (rangeAnno == null) {
            return null;
        }

        // Assume rangeAnno is well-formed, i.e., 'from' is less than or equal to 'to'.
        if (AnnotationUtils.areSameByClass(rangeAnno, IntRange.class)) {
            return new Range(
                    AnnotationUtils.getElementValue(rangeAnno, "from", Long.class, true),
                    AnnotationUtils.getElementValue(rangeAnno, "to", Long.class, true));
        }

        return null;
    }
}
