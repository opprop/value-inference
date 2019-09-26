package value;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.LiteralTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.NewArrayTree;

import value.qual.BoolVal;
import value.qual.BottomVal;
import value.qual.IntRange;
import value.qual.StringVal;
import value.qual.UnknownVal;
import value.representation.ValueRepresentationUtils;

public class ValueAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {
	
	/** The maximum number of values allowed in an annotation's array. */
    protected static final int MAX_VALUES = 10;
    
    /** The top type for this hierarchy. */
    protected final AnnotationMirror UNKNOWNVAL =
            AnnotationBuilder.fromClass(elements, UnknownVal.class);

    /** The bottom type for this hierarchy. */
    protected final AnnotationMirror BOTTOMVAL =
            AnnotationBuilder.fromClass(elements, BottomVal.class);
	
    public ValueAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        ValueRepresentationUtils.getInstance(processingEnv, elements);
        postInit();
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
    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
        // Because the Value Checker includes its own alias annotations,
        // the qualifiers have to be explicitly defined.
        return new LinkedHashSet<>(
                Arrays.asList(
                        IntRange.class,
                        BoolVal.class,
                        StringVal.class,
                        BottomVal.class,
                        UnknownVal.class));
    }
    
    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new ValueQualifierHierarchy(factory);
    }
    
    /** The qualifier hierarchy for the Value type system. */
    private final class ValueQualifierHierarchy extends MultiGraphQualifierHierarchy {

        /** @param factory MultiGraphFactory to use to construct this */
        public ValueQualifierHierarchy(MultiGraphQualifierHierarchy.MultiGraphFactory factory) {
            super(factory);
        }

        @Override
        public AnnotationMirror greatestLowerBound(AnnotationMirror a1, AnnotationMirror a2) {
            if (isSubtype(a1, a2)) {
                return a1;
            } else if (isSubtype(a2, a1)) {
                return a2;
            }
            
            return BOTTOMVAL;
        }

        /**
         * Determines the least upper bound of a1 and a2, which contains the union of their sets of
         * possible values.
         *
         * @return the least upper bound of a1 and a2
         */
        @Override
        public AnnotationMirror leastUpperBound(AnnotationMirror a1, AnnotationMirror a2) {
            if (!AnnotationUtils.areSameByName(getTopAnnotation(a1), getTopAnnotation(a2))) {
                // The annotations are in different hierarchies
                return null;
            }

            if (isSubtype(a1, a2)) {
                return a2;
            } else if (isSubtype(a2, a1)) {
                return a1;
            }

            if (AnnotationUtils.areSameByName(a1, a2)) {
                // If both are the same type, determine the type and merge
                if (AnnotationUtils.areSameByClass(a1, IntRange.class)) {
                    // special handling for IntRange
                	long from1 = AnnotationUtils.getElementValue(a1, "from", Long.class, true);
                    long to1 = AnnotationUtils.getElementValue(a1, "to", Long.class, true);
                    long from2 = AnnotationUtils.getElementValue(a2, "from", Long.class, true);
                    long to2 = AnnotationUtils.getElementValue(a2, "to", Long.class, true);
                    return createIntRangeAnnotation(Math.min(from1, from2), Math.max(to1, to2));
                } else {
                    AnnotationBuilder builder =
                            new AnnotationBuilder(processingEnv, a1.getAnnotationType().toString());
                    return builder.build();
                }
            }

            // In all other cases, the LUB is UnknownVal.
            return UNKNOWNVAL;
        }

        /**
         * Computes subtyping as per the subtyping in the qualifier hierarchy structure unless both
         * annotations are Value. In this case, subAnno is a subtype of superAnno iff superAnno
         * contains at least every element of subAnno.
         *
         * @return true if subAnno is a subtype of superAnno, false otherwise
         */
        @Override
        public boolean isSubtype(AnnotationMirror subAnno, AnnotationMirror superAnno) {
            if (AnnotationUtils.areSameByClass(subAnno, UnknownVal.class)) {
                superAnno = convertToUnknown(superAnno);
            }

            if (AnnotationUtils.areSameByClass(superAnno, UnknownVal.class)
                    || AnnotationUtils.areSameByClass(subAnno, BottomVal.class)) {
                return true;
            } else if (AnnotationUtils.areSameByClass(subAnno, UnknownVal.class)
                    || AnnotationUtils.areSameByClass(superAnno, BottomVal.class)) {
                return false;
            } else if (AnnotationUtils.areSameByName(superAnno, subAnno)) {
                // Same type, so might be subtype
                if (AnnotationUtils.areSameByClass(subAnno, IntRange.class)) {
                    // Special case for range-based annotations
                	long fromsub = AnnotationUtils.getElementValue(subAnno, "from", Long.class, true);
                    long tosub = AnnotationUtils.getElementValue(subAnno, "to", Long.class, true);
                    long fromsuper = AnnotationUtils.getElementValue(superAnno, "from", Long.class, true);
                    long tosuper = AnnotationUtils.getElementValue(superAnno, "to", Long.class, true);
                    return fromsub >= fromsuper && tosub <= tosuper;
                }
                return true;
            } else {
                return false;
            }
        }
    }
    
    /**
     * Finds the appropriate value for the {@code from} value of an annotated type mirror containing
     * an {@code IntRange} annotation.
     *
     * @param atm an annotated type mirror that contains an {@code IntRange} annotation.
     * @return either the from value from the passed int range annotation, or the minimum value of
     *     the domain of the underlying type (i.e. Integer.MIN_VALUE if the underlying type is int)
     */
    public long getFromValueFromIntRange(AnnotatedTypeMirror atm) {
        AnnotationMirror anno = atm.getAnnotation(IntRange.class);

        if (AnnotationUtils.hasElementValue(anno, "from")) {
            return AnnotationUtils.getElementValue(anno, "from", Long.class, false);
        }

        long from;
        switch (atm.getUnderlyingType().getKind()) {
            case INT:
                from = Integer.MIN_VALUE;
                break;
            case SHORT:
                from = Short.MIN_VALUE;
                break;
            case BYTE:
                from = Byte.MIN_VALUE;
                break;
            case CHAR:
                from = Character.MIN_VALUE;
                break;
            default:
                from = Long.MIN_VALUE;
        }
        return from;
    }
    
    /**
     * Finds the appropriate value for the {@code to} value of an annotated type mirror containing
     * an {@code IntRange} annotation.
     *
     * @param atm an annotated type mirror that contains an {@code IntRange} annotation.
     * @return either the to value from the passed int range annotation, or the maximum value of the
     *     domain of the underlying type (i.e. Integer.MAX_VALUE if the underlying type is int)
     */
    public long getToValueFromIntRange(AnnotatedTypeMirror atm) {
        AnnotationMirror anno = atm.getAnnotation(IntRange.class);

        if (AnnotationUtils.hasElementValue(anno, "to")) {
            return AnnotationUtils.getElementValue(anno, "to", Long.class, false);
        }

        long to;
        switch (atm.getUnderlyingType().getKind()) {
            case INT:
                to = Integer.MAX_VALUE;
                break;
            case SHORT:
                to = Short.MAX_VALUE;
                break;
            case BYTE:
                to = Byte.MAX_VALUE;
                break;
            case CHAR:
                to = Character.MAX_VALUE;
                break;
            default:
                to = Long.MAX_VALUE;
        }
        return to;
    }
    
    @Override
    protected TreeAnnotator createTreeAnnotator() {
        // Don't call super.createTreeAnnotator because it includes the PropagationTreeAnnotator.
        // Only use the PropagationTreeAnnotator for typing new arrays.  The Value Checker
        // computes types differently for all other trees normally typed by the
        // PropagationTreeAnnotator.
        TreeAnnotator arrayCreation =
                new TreeAnnotator(this) {
                    PropagationTreeAnnotator propagationTreeAnnotator =
                            new PropagationTreeAnnotator(atypeFactory);

                    @Override
                    public Void visitNewArray(NewArrayTree node, AnnotatedTypeMirror mirror) {
                        return propagationTreeAnnotator.visitNewArray(node, mirror);
                    }
                };
        return new ListTreeAnnotator(
                new ValueTreeAnnotator(this),
                new LiteralTreeAnnotator(this).addStandardLiteralQualifiers(),
                arrayCreation);
    }
    
    /** The TreeAnnotator for this AnnotatedTypeFactory. It adds/replaces annotations. */
    protected class ValueTreeAnnotator extends TreeAnnotator {

        public ValueTreeAnnotator(ValueAnnotatedTypeFactory factory) {
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
}
