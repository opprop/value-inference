package value;

import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferenceChecker;
import checkers.inference.InferenceQualifierHierarchy;
import checkers.inference.InferenceTreeAnnotator;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.VariableAnnotator;
import checkers.inference.model.ConstraintManager;

import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.value.ValueAnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;

public class ValueInferenceAnnotatedTypeFactory extends InferenceAnnotatedTypeFactory {
	
	protected final ValueAnnotatedTypeFactory valueATF;
	
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
        this.valueATF = (ValueAnnotatedTypeFactory) realTypeFactory;
        postInit();
    }
    
    @Override
    public AnnotationMirror canonicalAnnotation(AnnotationMirror anno) {
        AnnotationMirror result = valueATF.canonicalAnnotation(anno);
        // System.err.println(" === Aliasing: " + anno.toString() + " ==> " + result);

        return result;
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
    
    @Override
    public VariableAnnotator createVariableAnnotator() {
        return new ValueVariableAnnotator(
                this, valueATF, realChecker, slotManager, constraintManager);
    }
    
    private final class ValueVariableAnnotator extends VariableAnnotator {

		public ValueVariableAnnotator(InferenceAnnotatedTypeFactory typeFactory, AnnotatedTypeFactory realTypeFactory,
				InferrableChecker realChecker, SlotManager slotManager, ConstraintManager constraintManager) {
			super(typeFactory, realTypeFactory, realChecker, slotManager, constraintManager);
		}
    
    }
    
    @Override
    public TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(
        		new ImplicitsTreeAnnotator(this),
                new ValueInferenceTreeAnnotator(
                        this, realChecker, realTypeFactory, variableAnnotator, slotManager));
    }
    

    private final class ValueInferenceTreeAnnotator extends InferenceTreeAnnotator {

		public ValueInferenceTreeAnnotator(InferenceAnnotatedTypeFactory atypeFactory, InferrableChecker realChecker,
				AnnotatedTypeFactory realAnnotatedTypeFactory, VariableAnnotator variableAnnotator,
				SlotManager slotManager) {
			super(atypeFactory, realChecker, realAnnotatedTypeFactory, variableAnnotator, slotManager);
		}
    	
    }
}
