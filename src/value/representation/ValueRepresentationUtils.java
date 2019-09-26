package value.representation;

import checkers.inference.model.ConstantSlot;
import checkers.inference.solver.util.Statistics;
import value.ValueAnnotationMirrorHolder;
import value.qual.BottomVal;
import value.qual.IntRange;
import value.qual.UnknownVal;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.qual.PolyAll;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.BugInCF;

/**
 * Utility class containing logic for creating and converting internal representations of units
 * between its 3 primary forms: {@link UnitsRep} as annotation mirrors and {@link TypecheckUnit}.
 *
 * <p>TODO: {@code @Unit}, and alias forms.
 */
public class ValueRepresentationUtils {
	private static ValueRepresentationUtils singletonInstance;
    private static ProcessingEnvironment processingEnv;
    private static Elements elements;
	
	private boolean serializeRange;
	
	private ValueRepresentationUtils(ProcessingEnvironment processingEnv, Elements elements) {
		ValueRepresentationUtils.processingEnv = processingEnv;
		ValueRepresentationUtils.elements = elements;
    }

    public static ValueRepresentationUtils getInstance(
            ProcessingEnvironment processingEnv, Elements elements) {
        if (singletonInstance == null) {
            singletonInstance = new ValueRepresentationUtils(processingEnv, elements);
        }
        return singletonInstance;
    }
    
    public static ValueRepresentationUtils getInstance() {
        if (singletonInstance == null) {
            throw new BugInCF(
                    "getInstance() called without initializing ValueRepresentationUtils.");
        }
        return singletonInstance;
    }
	
	public boolean serializeRange() {
        return serializeRange;
    }
	
	public void setSerializedValue(Set<ConstantSlot> constantSlots) {
        serializeRange = false;
        for (ConstantSlot slot : constantSlots) {
            TypeCheckValue value = createTypeCheckValue(slot.getValue());
            serializeRange = serializeRange || value.isIntRange();
        }
    }
	
	// 1 to 1 mapping between an annotation mirror and its unique type check value.
    private final Map<AnnotationMirror, TypeCheckValue> typeCheckValueCache =
            AnnotationUtils.createAnnotationMap();
    
	public TypeCheckValue createTypeCheckValue(AnnotationMirror anno) {
        if (typeCheckValueCache.containsKey(anno)) {
            return typeCheckValueCache.get(anno);
        }

        TypeCheckValue value = new TypeCheckValue();
        
        if(AnnotationUtils.areSame(anno, ValueAnnotationMirrorHolder.UNKNOWNVAL)) {
			value.setUnknownVal(true);
		} else if(AnnotationUtils.areSame(anno, ValueAnnotationMirrorHolder.BOTTOMVAL)) {
			value.setBottomVal(true);
		} else if(AnnotationUtils.areSame(anno, ValueAnnotationMirrorHolder.BOOLVAL)) {
			value.setBoolVal(true);
		} else if(AnnotationUtils.areSame(anno, ValueAnnotationMirrorHolder.STRINGVAL)) {
			value.setStringVal(true);
		} else if(AnnotationUtils.areSame(anno, ValueAnnotationMirrorHolder.INTVAL)) {
			value.setIntVal(true);
			value.setIntVals(AnnotationUtils.getElementValue(anno, "value", Long.class, true));
		} else if(AnnotationUtils.areSame(anno, ValueAnnotationMirrorHolder.INTRANGE)) {
			value.setIntRange(true);
			value.setIntRangeLower(AnnotationUtils.getElementValue(anno, "from", Long.class, true));
			value.setIntRangeUpper(AnnotationUtils.getElementValue(anno, "to", Long.class, true));
		} else {
			return null;
		}
        
        typeCheckValueCache.put(anno, value);

        return value;
    }

}
