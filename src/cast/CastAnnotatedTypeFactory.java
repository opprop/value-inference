package cast;

import java.util.Collections;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.value.ValueAnnotatedTypeFactory;
import org.checkerframework.common.value.ValueCheckerUtils;
import org.checkerframework.common.value.qual.ArrayLen;
import org.checkerframework.common.value.qual.ArrayLenRange;
import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.common.value.qual.IntVal;
import org.checkerframework.common.value.qual.StringVal;
import org.checkerframework.common.value.util.NumberUtils;
import org.checkerframework.common.value.util.Range;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.qual.TypeKind;
import org.checkerframework.framework.qual.TypeUseLocation;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.type.typeannotator.ListTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.TypeAnnotator;
import org.checkerframework.framework.util.defaults.QualifierDefaults;
import org.checkerframework.javacutil.AnnotationUtils;

import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.TypeCastTree;

public class CastAnnotatedTypeFactory extends ValueAnnotatedTypeFactory {
	
	public CastAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        if (this.getClass().equals(CastAnnotatedTypeFactory.class)) {
            this.postInit();
        }
    }
	
	@Override
    public CFTransfer createFlowTransferFunction(
            CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        return new CastTransfer(analysis);
    }
	
	@Override
    protected TypeAnnotator createTypeAnnotator() {
        return new ListTypeAnnotator(new CastTypeAnnotator(this), super.createTypeAnnotator());
    }
	
    @Override
    protected void addCheckedCodeDefaults(QualifierDefaults defs) {
		TypeUseLocation[] useLocation = {TypeUseLocation.PARAMETER, TypeUseLocation.FIELD};
    	AnnotationMirror anno;
    	
    	anno = createIntRangeAnnotation(Range.BYTE_EVERYTHING);
    	TypeKind[] byte_type = {TypeKind.BYTE, TypeKind.BYTE_ARRAY};
    	defs.addCheckedCodeDefaults(anno, useLocation, byte_type);
    	
    	anno = createIntRangeAnnotation(Range.CHAR_EVERYTHING);
    	TypeKind[] char_type = {TypeKind.CHAR, TypeKind.CHAR_ARRAY};
    	defs.addCheckedCodeDefaults(anno, useLocation, char_type);
    	
    	anno = createIntRangeAnnotation(Range.SHORT_EVERYTHING);
    	TypeKind[] short_type = {TypeKind.SHORT, TypeKind.SHORT_ARRAY};
    	defs.addCheckedCodeDefaults(anno, useLocation, short_type);
    	
    	anno = createIntRangeAnnotation(Range.INT_EVERYTHING);
    	TypeKind[] int_type = {TypeKind.INT, TypeKind.INT_ARRAY};
    	defs.addCheckedCodeDefaults(anno, useLocation, int_type);
    	
        super.addCheckedCodeDefaults(defs);
    }
	
    /**
     * Performs pre-processing on annotations written by users, replacing illegal annotations by
     * legal ones.
     */
    private class CastTypeAnnotator extends TypeAnnotator {

        private CastTypeAnnotator(AnnotatedTypeFactory atypeFactory) {
            super(atypeFactory);
        }

        @Override
        protected Void scan(AnnotatedTypeMirror type, Void aVoid) {
            replaceWithNewAnnoInSpecialCases(type);
            return super.scan(type, aVoid);
        }
        
        @Override
        public Void visitExecutable(AnnotatedExecutableType t, Void p) {
			List<AnnotatedTypeMirror> paramTypes = t.getParameterTypes();
			for (AnnotatedTypeMirror paramType : paramTypes) {
				if (paramType.getKind() == TypeKind.mapTypeKind(TypeKind.ARRAY)) {
					AnnotatedTypeMirror compType = ((AnnotatedArrayType)paramType).getComponentType();
					AnnotationMirror anno = createIntRangeAnnotations(compType);
		        	if (anno != null) {
		        		compType.addMissingAnnotations(Collections.singleton(anno));
					}
		        	scan(((AnnotatedArrayType)paramType).getComponentType(), p);
		        	continue;
				}
				
				AnnotationMirror anno = createIntRangeAnnotations(paramType);
				if (anno != null) {
					paramType.addMissingAnnotations(Collections.singleton(anno));
				}
			}
			
			AnnotatedTypeMirror retType = t.getReturnType();
			if (retType.getKind() == TypeKind.mapTypeKind(TypeKind.ARRAY)) {
				AnnotatedTypeMirror compType = ((AnnotatedArrayType)retType).getComponentType();
				AnnotationMirror anno = createIntRangeAnnotations(compType);
	        	if (anno != null) {
	        		compType.addMissingAnnotations(Collections.singleton(anno));
				}
	        	scan(((AnnotatedArrayType)retType).getComponentType(), p);
	        	return super.visitExecutable(t, p);
			}
			
			AnnotationMirror anno = createIntRangeAnnotations(retType);
			if (anno != null) {
				retType.addMissingAnnotations(Collections.singleton(anno));
			}
        	
            return super.visitExecutable(t, p);
        }
        
        private AnnotationMirror createIntRangeAnnotations(AnnotatedTypeMirror atm) {
			AnnotationMirror newAnno;
			switch (atm.getKind()) {
	            case SHORT:
	            	newAnno = createIntRangeAnnotation(Range.SHORT_EVERYTHING);
	            	break;
	            case BYTE:
	            	newAnno = createIntRangeAnnotation(Range.BYTE_EVERYTHING);
	            	break;
	            case CHAR:
	            	newAnno = createIntRangeAnnotation(Range.CHAR_EVERYTHING);
	            	break;
	            case ARRAY:
	            	newAnno = createIntRangeAnnotation(Range.EVERYTHING);
	            	break;
	            case INT:
	            case DOUBLE:
	            case FLOAT:
	            case LONG:
	            	newAnno = createIntRangeAnnotation(Range.EVERYTHING);
	            	break;
	            default:
	            	newAnno = null;
			}
			return newAnno;
        }
        
        /**
         * This method performs pre-processing on annotations written by users.
         *
         * <p>If any *Val annotation has &gt; MAX_VALUES number of values provided, replaces the
         * annotation by @IntRange for integral types, @ArrayLenRange for arrays, @ArrayLen
         * or @ArrayLenRange for strings, and @UnknownVal for all other types. Works together with
         * {@link
         * org.checkerframework.common.value.ValueVisitor#visitAnnotation(com.sun.source.tree.AnnotationTree,
         * Void)} which issues warnings to users in these cases.
         *
         * <p>If any @IntRange or @ArrayLenRange annotation has incorrect parameters, e.g. the value
         * "from" is greater than the value "to", replaces the annotation by {@code @BottomVal}. The
         * {@link
         * org.checkerframework.common.value.ValueVisitor#visitAnnotation(com.sun.source.tree.AnnotationTree,
         * Void)} raises an error to users if the annotation was user-written.
         *
         * <p>If any @ArrayLen annotation has a negative number, replaces the annotation by {@code
         * BottomVal}. The {@link
         * org.checkerframework.common.value.ValueVisitor#visitAnnotation(com.sun.source.tree.AnnotationTree,
         * Void)} raises an error to users if the annotation was user-written.
         *
         * <p>If a user only writes one side of an {@code IntRange} annotation, this method also
         * computes an appropriate default based on the underlying type for the other side of the
         * range. For instance, if the user write {@code @IntRange(from = 1) short x;} then this
         * method will translate the annotation to {@code @IntRange(from = 1, to = Short.MAX_VALUE}.
         */
        private void replaceWithNewAnnoInSpecialCases(AnnotatedTypeMirror atm) {
            AnnotationMirror anno = atm.getAnnotationInHierarchy(UNKNOWNVAL);
            if (anno == null) {
                return;
            }

            if (anno != null && anno.getElementValues().size() > 0) {
                if (AnnotationUtils.areSameByClass(anno, IntVal.class)) {
                    List<Long> values = getIntValues(anno);
                    if (values.size() > MAX_VALUES) {
                        long annoMinVal = Collections.min(values);
                        long annoMaxVal = Collections.max(values);
                        atm.replaceAnnotation(
                                createIntRangeAnnotation(new Range(annoMinVal, annoMaxVal)));
                    }
                } else if (AnnotationUtils.areSameByClass(anno, ArrayLen.class)) {
                    List<Integer> values = getArrayLength(anno);
                    if (values.isEmpty()) {
                        atm.replaceAnnotation(BOTTOMVAL);
                    } else if (Collections.min(values) < 0) {
                        atm.replaceAnnotation(BOTTOMVAL);
                    } else if (values.size() > MAX_VALUES) {
                        long annoMinVal = Collections.min(values);
                        long annoMaxVal = Collections.max(values);
                        atm.replaceAnnotation(
                                createArrayLenRangeAnnotation(new Range(annoMinVal, annoMaxVal)));
                    }
                } else if (AnnotationUtils.areSameByClass(anno, IntRange.class)) {
                    // Compute appropriate defaults for integral ranges.
                    long from = getFromValueFromIntRange(atm);
                    long to = getToValueFromIntRange(atm);

                    if (from > to) {
                        // from > to either indicates a user error when writing an
                        // annotation or an error in the checker's implementation -
                        // from should always be <= to. ValueVisitor#validateType will
                        // issue an error.
                        atm.replaceAnnotation(BOTTOMVAL);
                    } else {
                        // Always do a replacement of the annotation here so that
                        // the defaults calculated above are correctly added to the
                        // annotation (assuming the annotation is well-formed).
                        atm.replaceAnnotation(createIntRangeAnnotation(new Range(from, to)));
                    }
                } else if (AnnotationUtils.areSameByClass(anno, ArrayLenRange.class)) {
                    int from = AnnotationUtils.getElementValue(anno, "from", Integer.class, true);
                    int to = AnnotationUtils.getElementValue(anno, "to", Integer.class, true);
                    if (from > to) {
                        // from > to either indicates a user error when writing an
                        // annotation or an error in the checker's implementation -
                        // from should always be <= to. ValueVisitor#validateType will
                        // issue an error.
                        atm.replaceAnnotation(BOTTOMVAL);
                    } else if (from < 0) {
                        // No array can have a length less than 0. Any time the type includes a from
                        // less than zero, it must indicate imprecision in the checker.
                        atm.replaceAnnotation(createArrayLenRangeAnnotation(0, to));
                    }
                } else if (AnnotationUtils.areSameByClass(anno, StringVal.class)) {
                    // The annotation is StringVal. If there are too many elements,
                    // ArrayLen or ArrayLenRange is used.
                    List<String> values = getStringValues(anno);

                    if (values.size() > MAX_VALUES) {
                        List<Integer> lengths = ValueCheckerUtils.getLengthsForStringValues(values);
                        atm.replaceAnnotation(createArrayLenAnnotation(lengths));
                    }

                } else {
                    // In here the annotation is @*Val where (*) is not Int, String but other types
                    // (Double, etc).
                    // Therefore we extract its values in a generic way to check its size.
                    List<Object> values =
                            AnnotationUtils.getElementValueArray(
                                    anno, "value", Object.class, false);
                    if (values.size() > MAX_VALUES) {
                        atm.replaceAnnotation(UNKNOWNVAL);
                    }
                }
            }
        }
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
                new CastTreeAnnotator(this), new ImplicitsTreeAnnotator(this), arrayCreation);
    }
	
	protected class CastTreeAnnotator extends ValueTreeAnnotator {
		public CastTreeAnnotator(CastAnnotatedTypeFactory factory) {
            super(factory);
        }
		
		@Override
	    public Void visitTypeCast(TypeCastTree tree, AnnotatedTypeMirror atm) {
            if (handledByValueChecker(atm)) {
                AnnotationMirror oldAnno =
                        getAnnotatedType(tree.getExpression()).getAnnotationInHierarchy(UNKNOWNVAL);
                if (oldAnno != null) {
                    TypeMirror newType = atm.getUnderlyingType();
                    AnnotationMirror newAnno;
                    Range range;

                    if (isIntRange(oldAnno) && (range = getRange(oldAnno)).isWiderThan(MAX_VALUES)) {
                        Class<?> newClass = ValueCheckerUtils.getClassFromType(newType);
                        Range castRange = getRange(oldAnno);
                        if (newClass == byte.class && isUnsignedByte(castRange)) {
                        	newAnno = createIntRangeAnnotation(unsignedByteRange());
                        }
                        else if (newClass == short.class && isUnsignedShort(castRange)) {
                            newAnno = createIntRangeAnnotation(unsignedShortRange());
                        }
                        else if (newClass == String.class) {
                            newAnno = UNKNOWNVAL;
                        } else if (newClass == Boolean.class || newClass == boolean.class) {
                            throw new UnsupportedOperationException(
                                    "ValueAnnotatedTypeFactory: can't convert int to boolean");
                        } else {
                            newAnno =
                                    createIntRangeAnnotation(NumberUtils.castRange(newType, range));
                        }
                        atm.addMissingAnnotations(Collections.singleton(newAnno));
                        return null;
                    }
                }
            }
            
            return super.visitTypeCast(tree, atm);
	    }
		
		/** Returns true iff the given type is in the domain of the Constant Value Checker. */
        private boolean handledByValueChecker(AnnotatedTypeMirror type) {
            return COVERED_CLASS_STRINGS.contains(type.getUnderlyingType().toString());
        }

        /** Return true if this range contains unsigned part of {@code short} value. */
        private boolean isUnsignedShort(Range range) {
        	return !range.intersect(new Range(Short.MAX_VALUE + 1, Short.MAX_VALUE * 2 + 1)).isNothing()
        			&& !range.contains(Range.SHORT_EVERYTHING);
        }

        /** Return true if this range contains unsigned part of {@code byte} value. */
        private boolean isUnsignedByte(Range range) {
            return !range.intersect(new Range(Byte.MAX_VALUE + 1, Byte.MAX_VALUE * 2 + 1)).isNothing()
            		&& !range.contains(Range.BYTE_EVERYTHING);
        }
        
        /** A range containing all possible unsigned byte values. */      
        private Range unsignedByteRange() {
            return new Range(0, Byte.MAX_VALUE * 2 + 1);
        }
        
        /** A range containing all possible unsigned short values. */      
        private Range unsignedShortRange() {
            return new Range(0, Short.MAX_VALUE * 2 + 1);
        }
	}
}
