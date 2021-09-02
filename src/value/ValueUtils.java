package value;

import com.sun.source.tree.Tree;
import org.checkerframework.common.value.JavaExpressionOptimizer;
import org.checkerframework.common.value.ValueChecker;
import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.common.value.qual.IntVal;
import org.checkerframework.common.value.qual.StringVal;
import org.checkerframework.common.value.util.NumberUtils;
import org.checkerframework.common.value.util.Range;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.GenericAnnotatedTypeFactory;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TypeSystemError;
import org.checkerframework.javacutil.TypesUtils;
import org.plumelib.util.CollectionsPlume;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Utility methods for the Value Checker. */
public class ValueUtils {

    /** Do not instantiate. */
    private ValueUtils() {
        throw new TypeSystemError("do not instantiate");
    }

    /**
     * Get a list of values of annotation, and then cast them to a given type.
     *
     * @param anno the annotation that contains values
     * @param castTo the type that is casted to
     * @param atypeFactory the type factory
     * @return a list of values after the casting
     */
    public static List<?> getValuesCastedToType(
            AnnotationMirror anno, TypeMirror castTo, ValueAnnotatedTypeFactory atypeFactory) {
        Class<?> castType = TypesUtils.getClassFromType(castTo);
        List<?> values;
        switch (AnnotationUtils.annotationName(anno)) {
            case ValueAnnotatedTypeFactory.INTVAL_NAME:
                List<Long> longs = atypeFactory.getIntValues(anno);
                values = convertIntVal(longs, castType, castTo);
                break;
            case ValueAnnotatedTypeFactory.INTRANGE_NAME:
                Range range = atypeFactory.getRange(anno);
                List<Long> rangeValues = getValuesFromRange(range, Long.class);
                values = convertIntVal(rangeValues, castType, castTo);
                break;
            case ValueAnnotatedTypeFactory.STRINGVAL_NAME:
                values = convertStringVal(anno, castType, atypeFactory);
                break;
            case ValueAnnotatedTypeFactory.BOOLVAL_NAME:
                values = convertBoolVal(anno, castType, atypeFactory);
                break;
            case ValueAnnotatedTypeFactory.BOTTOMVAL_NAME:
            default:
                values = null;
        }
        return values;
    }

    /**
     * Converts a long value to a boxed numeric type.
     *
     * @param value a long value
     * @param expectedType the boxed numeric type of the result
     * @return {@code value} converted to {@code expectedType} using standard conversion rules
     */
    private static <T> T convertLongToType(long value, Class<T> expectedType) {
        Object convertedValue;
        if (expectedType == Integer.class) {
            convertedValue = (int) value;
        } else if (expectedType == Short.class) {
            convertedValue = (short) value;
        } else if (expectedType == Byte.class) {
            convertedValue = (byte) value;
        } else if (expectedType == Long.class) {
            convertedValue = value;
        } else if (expectedType == Double.class) {
            convertedValue = (double) value;
        } else if (expectedType == Float.class) {
            convertedValue = (float) value;
        } else if (expectedType == Character.class) {
            convertedValue = (char) value;
        } else {
            throw new UnsupportedOperationException(
                    "ValueCheckerUtils: unexpected class: " + expectedType);
        }
        return expectedType.cast(convertedValue);
    }

    /**
     * Get all possible values from the given type and cast them into a boxed primitive type.
     *
     * <p>{@code expectedType} must be a boxed type, not a primitive type, because primitive types
     * cannot be stored in a list.
     *
     * @param range the given range
     * @param expectedType the expected type
     * @return a list of all the values in the range
     */
    public static <T> List<T> getValuesFromRange(Range range, Class<T> expectedType) {
        if (range == null || range.isWiderThan(ValueAnnotatedTypeFactory.MAX_VALUES)) {
            return null;
        }
        if (range.isNothing()) {
            return Collections.emptyList();
        }

        // The subtraction does not overflow, because the width has already been checked, so the
        // bound difference is less than ValueAnnotatedTypeFactory.MAX_VALUES.
        long boundDifference = range.to - range.from;

        // Each value is computed as a sum of the first value and an offset within the range,
        // to avoid having range.to as an upper bound of the loop. range.to can be Long.MAX_VALUE,
        // in which case a comparison value <= range.to would be always true.
        // boundDifference is always much smaller than Long.MAX_VALUE
        List<T> values = new ArrayList<>((int) boundDifference + 1);
        for (long offset = 0; offset <= boundDifference; offset++) {
            long value = range.from + offset;
            values.add(convertLongToType(value, expectedType));
        }
        return values;
    }

    private static List<?> convertToStringVal(List<?> origValues) {
        if (origValues == null) {
            return null;
        }
        return CollectionsPlume.mapList(Object::toString, origValues);
    }

    /**
     * Convert the {@code value} argument/element of a @BoolVal annotation into a list.
     *
     * @param anno a @BoolVal annotation
     * @param newClass if String.class, the returned list is a {@code List<String>}
     * @param atypeFactory the type factory, used for obtaining fields/elements from annotations
     * @return the {@code value} of a @BoolVal annotation, as a {@code List<Boolean>} or a {@code
     *     List<String>}
     */
    private static List<?> convertBoolVal(
            AnnotationMirror anno, Class<?> newClass, ValueAnnotatedTypeFactory atypeFactory) {
        List<Boolean> bools =
                AnnotationUtils.getElementValueArray(
                        anno, atypeFactory.boolValValueElement, Boolean.class);

        if (newClass == String.class) {
            return convertToStringVal(bools);
        }
        return bools;
    }

    /**
     * Convert the {@code value} argument/element of a {@code @StringVal} annotation into a list.
     *
     * @param anno a {@code @StringVal} annotation
     * @param newClass if char[].class, the returned list is a {@code List<char[]>}
     * @param atypeFactory the type factory, used for obtaining fields/elements from annotations
     * @return the {@code value} of a {@code @StringVal} annotation, as a {@code List<String>} or a
     *     {@code List<char[]>}
     */
    private static List<?> convertStringVal(
            AnnotationMirror anno, Class<?> newClass, ValueAnnotatedTypeFactory atypeFactory) {
        List<String> strings = atypeFactory.getStringValues(anno);
        if (newClass == char[].class) {
            return CollectionsPlume.mapList(String::toCharArray, strings);
        }
        return strings;
    }

    private static List<?> convertIntVal(List<Long> longs, Class<?> newClass, TypeMirror newType) {
        if (longs == null) {
            return null;
        }
        if (newClass == String.class) {
            return convertToStringVal(longs);
        } else if (newClass == Character.class || newClass == char.class) {
            return CollectionsPlume.mapList((Long l) -> (char) l.longValue(), longs);
        } else if (newClass == Boolean.class) {
            throw new UnsupportedOperationException(
                    "ValueAnnotatedTypeFactory: can't convert int to boolean");
        }
        return NumberUtils.castNumbers(newType, longs);
    }

}
