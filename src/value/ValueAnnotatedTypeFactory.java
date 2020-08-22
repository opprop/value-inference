package value;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeCastTree;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.value.ValueCheckerUtils;
import org.checkerframework.common.value.qual.ArrayLenRange;
import org.checkerframework.common.value.qual.IntVal;
import org.checkerframework.common.value.util.NumberUtils;
import org.checkerframework.common.value.util.Range;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedPrimitiveType;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.LiteralTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.type.typeannotator.ListTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.TypeAnnotator;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TypesUtils;
import value.qual.BoolVal;
import value.qual.BottomVal;
import value.qual.IntRange;
import value.qual.StringVal;
import value.qual.PolyVal;
import value.qual.UnknownVal;

public class ValueAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    /** The maximum number of values allowed in an annotation's array. */
    protected static final int MAX_VALUES = 10;

    /** The top type for this hierarchy. */
    protected final AnnotationMirror UNKNOWNVAL =
            AnnotationBuilder.fromClass(elements, UnknownVal.class);

    /** The bottom type for this hierarchy. */
    protected final AnnotationMirror BOTTOMVAL =
            AnnotationBuilder.fromClass(elements, BottomVal.class);
    
    /** The polymorphic type for this hierarchy. */
    protected final AnnotationMirror POLYVAL =
            AnnotationBuilder.fromClass(elements, PolyVal.class);

    public ValueAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
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
//                                    "java.lang.String",
                                    "char",
                                    "java.lang.Character",
                                    "float",
                                    "java.lang.Float",
//                                    "boolean",
//                                    "java.lang.Boolean",
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
//                        BoolVal.class,
//                        StringVal.class,
                        BottomVal.class,
                        UnknownVal.class,
                        PolyVal.class));
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
            if (AnnotationUtils.areSame(subAnno, UNKNOWNVAL)) {
                superAnno = convertToUnknown(superAnno);
            }

            if (AnnotationUtils.areSame(superAnno, UNKNOWNVAL)
                    || AnnotationUtils.areSame(subAnno, BOTTOMVAL)) {
                return true;
            } else if (AnnotationUtils.areSame(subAnno, UNKNOWNVAL)
                    || AnnotationUtils.areSame(superAnno, BOTTOMVAL)) {
                return false;
            } 
            // Case: @PolyUnit are treated as @UnknownVal
            else if (AnnotationUtils.areSame(subAnno, POLYVAL)) {
                return isSubtype(UNKNOWNVAL, superAnno);
            }
            if (AnnotationUtils.areSame(superAnno, POLYVAL)) {
                return true;
            } else if (AnnotationUtils.areSameByName(superAnno, subAnno)) {
                // Same type, so might be subtype
                if (AnnotationUtils.areSameByClass(subAnno, IntRange.class)) {
                    // Special case for range-based annotations
                    Range sub = getRange(subAnno);
                    Range sup = getRange(superAnno);
                    return sub.from >= sup.from && sub.to <= sup.to;
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        public AnnotationMirror widenedUpperBound(
                AnnotationMirror newQualifier, AnnotationMirror previousQualifier) {
            AnnotationMirror lub = leastUpperBound(newQualifier, previousQualifier);
            if (AnnotationUtils.areSameByClass(lub, IntRange.class)) {
                Range lubRange = getRange(lub);
                Range newRange = getRange(newQualifier);
                Range oldRange = getRange(previousQualifier);
                Range wubRange = widenedRange(newRange, oldRange, lubRange);
                return createIntRangeAnnotation(wubRange);
            } else {
                return lub;
            }
        }

        private Range widenedRange(Range newRange, Range oldRange, Range lubRange) {
            if (newRange == null || oldRange == null || lubRange.equals(oldRange)) {
                return lubRange;
            }
            // If both bounds of the new range are bigger than the old range, then returned range
            // should use the lower bound of the new range and a MAX_VALUE.
            if ((newRange.from >= oldRange.from && newRange.to >= oldRange.to)) {
                if (lubRange.to < Byte.MAX_VALUE) {
                    return Range.create(newRange.from, Byte.MAX_VALUE);
                } else if (lubRange.to < Short.MAX_VALUE) {
                    return Range.create(newRange.from, Short.MAX_VALUE);
                } else if (lubRange.to < Integer.MAX_VALUE) {
                    return Range.create(newRange.from, Integer.MAX_VALUE);
                } else {
                    return Range.create(newRange.from, Long.MAX_VALUE);
                }
            }

            // If both bounds of the old range are bigger than the new range, then returned range
            // should use a MIN_VALUE and the upper bound of the new range.
            if ((newRange.from <= oldRange.from && newRange.to <= oldRange.to)) {
                if (lubRange.from > Byte.MIN_VALUE) {
                    return Range.create(Byte.MIN_VALUE, newRange.to);
                } else if (lubRange.from > Short.MIN_VALUE) {
                    return Range.create(Short.MIN_VALUE, newRange.to);
                } else if (lubRange.from > Integer.MIN_VALUE) {
                    return Range.create(Integer.MIN_VALUE, newRange.to);
                } else {
                    return Range.create(Long.MIN_VALUE, newRange.to);
                }
            }

            if (lubRange.isWithin(Byte.MIN_VALUE + 1, Byte.MAX_VALUE)
                    || lubRange.isWithin(Byte.MIN_VALUE, Byte.MAX_VALUE - 1)) {
                return Range.BYTE_EVERYTHING;
            } else if (lubRange.isWithin(Short.MIN_VALUE + 1, Short.MAX_VALUE)
                    || lubRange.isWithin(Short.MIN_VALUE, Short.MAX_VALUE - 1)) {
                return Range.SHORT_EVERYTHING;
            } else if (lubRange.isWithin(Long.MIN_VALUE + 1, Long.MAX_VALUE)
                    || lubRange.isWithin(Long.MIN_VALUE, Long.MAX_VALUE - 1)) {
                return Range.INT_EVERYTHING;
            } else {
                return Range.EVERYTHING;
            }
        }
    }
    
    @Override
    protected TypeAnnotator createTypeAnnotator() {
        return new ListTypeAnnotator(new ValueTypeAnnotator(this), super.createTypeAnnotator());
    }
    
    /**
     * Performs pre-processing on annotations written by users, replacing illegal annotations by
     * legal ones.
     */
    private class ValueTypeAnnotator extends TypeAnnotator {

        private ValueTypeAnnotator(ValueAnnotatedTypeFactory atypeFactory) {
            super(atypeFactory);
        }

        @Override
        protected Void scan(AnnotatedTypeMirror type, Void aVoid) {
            return super.scan(type, aVoid);
        }

        @Override
        public Void visitExecutable(AnnotatedExecutableType t, Void p) {
            List<AnnotatedTypeMirror> paramTypes = t.getParameterTypes();
            for (AnnotatedTypeMirror paramType : paramTypes) {
                AnnotationMirror anno = createIntRangeAnnotations(paramType);
                if (anno != null) {
                    paramType.addMissingAnnotations(Collections.singleton(anno));
                }
            }

            AnnotatedTypeMirror retType = t.getReturnType();
            AnnotationMirror anno = createIntRangeAnnotations(retType);
            if (anno != null) {
                retType.addMissingAnnotations(Collections.singleton(anno));
            }
            while (retType instanceof AnnotatedArrayType) {
            	retType = ((AnnotatedArrayType) retType).getComponentType();
                anno = createIntRangeAnnotations(retType);
                if (anno != null) {
                	retType.addMissingAnnotations(Collections.singleton(anno));
                }
            }

            return super.visitExecutable(t, p);
        }
        
        @Override
        public Void visitArray(AnnotatedArrayType t, Void p) {
            AnnotatedTypeMirror comp = t.getComponentType();
            AnnotationMirror anno = createIntRangeAnnotations(comp);
            if (anno != null) {
                comp.addMissingAnnotations(Collections.singleton(anno));
            }
            return super.visitArray(t, p);
        }
        
        @Override
        public Void visitPrimitive(AnnotatedPrimitiveType t, Void p) {
            AnnotationMirror anno = createIntRangeAnnotations(t);
            if (anno != null) {
                t.addMissingAnnotations(Collections.singleton(anno));
            }
            return super.visitPrimitive(t, p);
        }
    }
    
    @Override
    public @Nullable AnnotatedTypeMirror getAnnotatedTypeVarargsArray(Tree tree) {
    	AnnotatedTypeMirror atm = super.getAnnotatedTypeVarargsArray(tree);
    	AnnotationMirror anno = createIntRangeAnnotations(atm);
        if (anno != null) {
        	atm.replaceAnnotation(anno);
        }
		return atm;
    }

	private AnnotationMirror createIntRangeAnnotations(AnnotatedTypeMirror atm) {
        AnnotationMirror newAnno;
        switch (atm.getKind()) {
            case NULL:
            	newAnno = BOTTOMVAL;
	            break;
//            case BOOLEAN:
//            	newAnno = AnnotationBuilder.fromClass(elements, BoolVal.class);
//	            break;
  	        case BYTE:
	            newAnno = createIntRangeAnnotation(Range.BYTE_EVERYTHING);
	            break;
            case SHORT:
                newAnno = createIntRangeAnnotation(Range.SHORT_EVERYTHING);
                break;
            case CHAR:
                newAnno = createIntRangeAnnotation(Range.CHAR_EVERYTHING);
                break;
            case INT:
                newAnno = createIntRangeAnnotation(Range.INT_EVERYTHING);
                break;
            case DOUBLE:
            case FLOAT:
            case LONG:
                newAnno = createIntRangeAnnotation(Range.EVERYTHING);
                break;
            default:
//            	if (atm.getUnderlyingType().toString().equals("java.lang.String")) {
//            		newAnno = AnnotationBuilder.fromClass(elements, StringVal.class);
//                    break;
//            	}
                newAnno = null;
                break;
        }
        return newAnno;
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
            return Range.create(
                    AnnotationUtils.getElementValue(rangeAnno, "from", Long.class, true),
                    AnnotationUtils.getElementValue(rangeAnno, "to", Long.class, true));
        }

        return null;
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
            	case NULL_LITERAL:
            		type.replaceAnnotation(BOTTOMVAL);
            		return null;
//                case BOOLEAN_LITERAL:
//                    AnnotationMirror boolAnno =
//                            createBooleanAnnotation(Collections.singletonList((Boolean) value));
//                    type.replaceAnnotation(boolAnno);
//                    return null;

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
//                case STRING_LITERAL:
//                    AnnotationMirror stringAnno =
//                            createStringAnnotation(Collections.singletonList((String) value));
//                    type.replaceAnnotation(stringAnno);
//                    return null;
                default:
                    return null;
            }
        }

        @Override
        public Void visitTypeCast(TypeCastTree tree, AnnotatedTypeMirror atm) {
            if (handledByValueChecker(atm)) {
                AnnotationMirror oldAnno =
                        getAnnotatedType(tree.getExpression()).getAnnotationInHierarchy(UNKNOWNVAL);
                if (oldAnno == null) {
                    return null;
                }
                TypeMirror newType = atm.getUnderlyingType();
                AnnotationMirror newAnno;
                Range range;

                if (TypesUtils.isString(newType) || newType.getKind() == TypeKind.ARRAY) {
                    // Strings and arrays do not allow conversions
                    newAnno = oldAnno;
                } else if (AnnotationUtils.areSameByClass(oldAnno, IntRange.class)
                        && (range = getRange(oldAnno)).isWiderThan(MAX_VALUES)) {
                    Class<?> newClass = ValueCheckerUtils.getClassFromType(newType);
                    if (newClass == String.class) {
                        newAnno = UNKNOWNVAL;
                    } else if (newClass == Boolean.class || newClass == boolean.class) {
                        throw new UnsupportedOperationException(
                                "ValueAnnotatedTypeFactory: can't convert int to boolean");
                    } else {
                        newAnno = createIntRangeAnnotation(NumberUtils.castRange(newType, range));
                    }
                } else {
                    List<?> values = ValueCheckerUtils.getValuesCastedToType(oldAnno, newType);
                    newAnno = createResultingAnnotation(atm.getUnderlyingType(), values);
                }
                atm.addMissingAnnotations(Collections.singleton(newAnno));
            } else if (atm.getKind() == TypeKind.ARRAY) {
                if (tree.getExpression().getKind() == Kind.NULL_LITERAL) {
                    atm.addMissingAnnotations(Collections.singleton(BOTTOMVAL));
                }
            }
            return null;
        }

        /** Returns true iff the given type is in the domain of the Constant Value Checker. */
        private boolean handledByValueChecker(AnnotatedTypeMirror type) {
            TypeMirror tm = type.getUnderlyingType();
            return COVERED_CLASS_STRINGS.contains(tm.toString());
        }
    }

    /**
     * Returns a constant value annotation with the {@code values}. The class of the annotation
     * reflects the {@code resultType} given.
     *
     * @param resultType used to selected which kind of value annotation is returned
     * @param values must be a homogeneous list: every element of it has the same class
     * @return a constant value annotation with the {@code values}
     */
    AnnotationMirror createResultingAnnotation(TypeMirror resultType, List<?> values) {
        if (values == null) {
            return UNKNOWNVAL;
        }
        // For some reason null is included in the list of values,
        // so remove it so that it does not cause a NPE elsewhere.
        values.remove(null);
        if (values.isEmpty()) {
            return BOTTOMVAL;
        }

        if (TypesUtils.isString(resultType)) {
            List<String> stringVals = new ArrayList<>(values.size());
            for (Object o : values) {
                stringVals.add((String) o);
            }
            return createStringAnnotation(stringVals);
        } else if (ValueCheckerUtils.getClassFromType(resultType) == char[].class) {
            List<String> stringVals = new ArrayList<>(values.size());
            for (Object o : values) {
                if (o instanceof char[]) {
                    stringVals.add(new String((char[]) o));
                } else {
                    stringVals.add(o.toString());
                }
            }
            return createStringAnnotation(stringVals);
        }

        TypeKind primitiveKind;
        if (TypesUtils.isPrimitive(resultType)) {
            primitiveKind = resultType.getKind();
        } else if (TypesUtils.isBoxedPrimitive(resultType)) {
            primitiveKind = types.unboxedType(resultType).getKind();
        } else {
            return UNKNOWNVAL;
        }

        switch (primitiveKind) {
            case BOOLEAN:
                List<Boolean> boolVals = new ArrayList<>(values.size());
                for (Object o : values) {
                    boolVals.add((Boolean) o);
                }
                return createBooleanAnnotation(boolVals);
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
            case BYTE:
                List<Number> numberVals = new ArrayList<>(values.size());
                List<Character> characterVals = new ArrayList<>(values.size());
                for (Object o : values) {
                    if (o instanceof Character) {
                        characterVals.add((Character) o);
                    } else {
                        numberVals.add((Number) o);
                    }
                }
                if (numberVals.isEmpty()) {
                    return createCharAnnotation(characterVals);
                }
                return createNumberAnnotationMirror(new ArrayList<>(numberVals));
            case CHAR:
                List<Character> charVals = new ArrayList<>(values.size());
                for (Object o : values) {
                    if (o instanceof Number) {
                        charVals.add((char) ((Number) o).intValue());
                    } else {
                        charVals.add((char) o);
                    }
                }
                return createCharAnnotation(charVals);
            default:
                throw new UnsupportedOperationException("Unexpected kind:" + resultType);
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
     * Create an {@code @IntRange} or {@code @IntVal} annotation from the range. May return
     * BOTTOMVAL or UNKNOWNVAL.
     */
    public AnnotationMirror createIntRangeAnnotation(Range range) {
        if (range.isNothing()) {
            return BOTTOMVAL;
        } else if (range.isLongEverything()) {
            return UNKNOWNVAL;
        } else if (range.isWiderThan(MAX_VALUES)) {
            return createIntRangeAnnotation(range.from, range.to);
        } else {
            List<Long> newValues = ValueCheckerUtils.getValuesFromRange(range, Long.class);
            return createIntValAnnotation(newValues);
        }
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
