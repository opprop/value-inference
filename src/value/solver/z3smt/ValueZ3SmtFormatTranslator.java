package value.solver.z3smt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.IntExpr;

import backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.VariableSlot;
import checkers.inference.solver.backend.encoder.ConstraintEncoderFactory;
import checkers.inference.solver.frontend.Lattice;
import value.representation.TypeCheckValue;
import value.solver.z3smt.encoder.ValueZ3SmtConstraintEncoderFactory;
import value.solver.z3smt.encoder.ValueZ3SmtEncoderUtils;
import value.solver.z3smt.representation.Z3InferenceValue;

import org.checkerframework.common.value.qual.BottomVal;
import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.common.value.qual.UnknownVal;

public class ValueZ3SmtFormatTranslator extends Z3SmtFormatTranslator<Z3InferenceValue, TypeCheckValue> {
	
	protected final ValueZ3SmtEncoderUtils valueZ3SmtEncoderUtil;
	
	public ValueZ3SmtFormatTranslator(Lattice lattice) {
		super(lattice);
		this.valueZ3SmtEncoderUtil = new ValueZ3SmtEncoderUtils();
	}

	@Override
	public AnnotationMirror decodeSolution(TypeCheckValue solution, ProcessingEnvironment processingEnv) {
		if (solution.isUnknownVal()) {
			AnnotationBuilder builder = new AnnotationBuilder(processingEnv, UnknownVal.class);
			return builder.build();
		}
		if (solution.isBottomVal()) {
			AnnotationBuilder builder = new AnnotationBuilder(processingEnv, BottomVal.class);
			return builder.build();
		}
		if (solution.isIntRange()) {
			AnnotationBuilder builder = new AnnotationBuilder(processingEnv, IntRange.class);
	        builder.setValue("from", solution.getIntRangeLower());
	        builder.setValue("to", solution.getIntRangeUpper());
	        return builder.build();
		}
		
		return null;
	}

	@Override
	protected Z3InferenceValue serializeVarSlot(VariableSlot slot) {
		int slotID = slot.getId();
		if (serializedSlots.containsKey(slotID)) {
			return serializedSlots.get(slotID);
		}
		Z3InferenceValue encodedSlot = Z3InferenceValue.makeVariableSlot(ctx, slotID);
		serializedSlots.put(slotID, encodedSlot);
		return encodedSlot;
	}

	@Override
	protected Z3InferenceValue serializeConstantSlot(ConstantSlot slot) {
		int slotID = slot.getId();
		if (serializedSlots.containsKey(slotID)) {
			return serializedSlots.get(slotID);
		}
		Z3InferenceValue encodedSlot = Z3InferenceValue.makeConstantSlot(ctx, slotID);
		serializedSlots.put(slotID, encodedSlot);
		return encodedSlot;
	}

	@Override
	public String generateZ3SlotDeclaration(VariableSlot slot) {
		Z3InferenceValue encodedSlot = serializeVarSlot(slot);
		List<String> slotDeclaration = new ArrayList<>();
		
		slotDeclaration.add(addZ3BoolDefinition(encodedSlot.getUnknownVal()));
        slotDeclaration.add(addZ3BoolDefinition(encodedSlot.getBottomVal()));
        slotDeclaration.add(addZ3BoolDefinition(encodedSlot.getBoolVal()));
        slotDeclaration.add(addZ3BoolDefinition(encodedSlot.getIntRange()));
        slotDeclaration.add(addZ3BoolDefinition(encodedSlot.getStringVal()));
        slotDeclaration.add(addZ3IntDefinition(encodedSlot.getIntRangeLower()));
        slotDeclaration.add(addZ3IntDefinition(encodedSlot.getIntRangeUpper()));
        
		return String.join(System.lineSeparator(), slotDeclaration);
	}

	private String addZ3BoolDefinition(BoolExpr z3BoolVariable) {
        return "(declare-fun " + z3BoolVariable.toString() + " () Bool)";
    }

    private String addZ3IntDefinition(IntExpr z3IntVariable) {
        return "(declare-fun " + z3IntVariable.toString() + " () Int)";
    }
    
	@Override
	public BoolExpr encodeSlotWellformnessConstraint(VariableSlot slot) {
        Z3InferenceValue value = slot.serialize(this);
        return ctx.mkAnd (
        		ctx.mkXor(value.getUnknownVal(), value.getBottomVal()),
        		ctx.mkXor(
        				ctx.mkXor(value.getBoolVal(), value.getStringVal()),
        				value.getIntRange()),
        		ctx.mkNot(
        				ctx.mkAnd(value.getBoolVal(), value.getStringVal(), value.getIntRange()))
        		);
	}

	@Override
	public BoolExpr encodeSlotPreferenceConstraint(VariableSlot slot) {
        Z3InferenceValue value = slot.serialize(this);
		return ctx.mkAnd(
				ctx.mkNot(value.getUnknownVal()),
				ctx.mkNot(value.getBottomVal())
				);
	}

	@Override
	public Map<Integer, AnnotationMirror> decodeSolution(List<String> model, ProcessingEnvironment processingEnv) {
		
		Map<Integer, AnnotationMirror> result = new HashMap<>();
        Map<Integer, TypeCheckValue> solutionSlots = new HashMap<>();
        
        for (String line : model) {
        	// each line is "varName value"
            String[] parts = line.split(" ");
            String value = parts[1];
            
            // Get slotID and component name
            int slotID;
            String component;
            int dashIndex = parts[0].indexOf("-");
            if (dashIndex < 0) {
                slotID = Integer.valueOf(parts[0]);
                component = null;
            } else {
                slotID = Integer.valueOf(parts[0].substring(0, dashIndex));
                component = parts[0].substring(dashIndex + 1);
            }
            
            // Create a fresh solution slot if needed in the map
            if (!solutionSlots.containsKey(slotID)) {
                solutionSlots.put(slotID, new TypeCheckValue());
            }
            
            TypeCheckValue z3Slot = solutionSlots.get(slotID);
            if (component.contentEquals("TOP")) {
            	z3Slot.setUnknownVal(Boolean.parseBoolean(value));
            }
            if (component.contentEquals("BOTTOM")) {
            	z3Slot.setBottomVal(Boolean.parseBoolean(value));
            }
            if (component.contentEquals("INTRANGE")) {
            	z3Slot.setIntRange(Boolean.parseBoolean(value));
            }
            if (component.contentEquals("from")) {
            	z3Slot.setIntRangeLower(Long.parseLong(value));
            }
            if (component.contentEquals("too")) {
            	z3Slot.setIntRangeUpper(Long.parseLong(value));
            }
        }
        
        for (Map.Entry<Integer, TypeCheckValue> entry : solutionSlots.entrySet()) {
        	result.put(entry.getKey(), decodeSolution(entry.getValue(), processingEnv));
        }
		
		return result;
	}
	
	

	@Override
	protected ConstraintEncoderFactory<BoolExpr> createConstraintEncoderFactory() {
		return new ValueZ3SmtConstraintEncoderFactory(lattice, ctx, this);
	}

}
