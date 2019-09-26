package value.solver.representation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.microsoft.z3.ArrayExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;

public class Z3InferenceValue {
	
	/** The maximum number of values allowed in an annotation's array. */
    protected static final int MAX_VALUES = 10;
	
	private final Context ctx;
	
	private int slotID;
	
	private BoolExpr unknownval;
    private BoolExpr bottomval;
    private BoolExpr boolval;
    private BoolExpr stringval;
    
    private BoolExpr intrange;
    private IntExpr intrangelo;
    private IntExpr intrangehi;
    
    private BoolExpr intval;
    private IntExpr intvalue;
    
    public static Z3InferenceValue makeConstantSlot(Context ctx, int slotID) {
    	Z3InferenceValue slot = new Z3InferenceValue(ctx, slotID);

        // default UnknownVal value is false
        slot.unknownval = ctx.mkBool(false);
        // default BottomVal value is false
        slot.bottomval = ctx.mkBool(false);
        // default BottomVal value is false
        slot.boolval = ctx.mkBool(false);
        // default BottomVal value is false
        slot.intrange = ctx.mkBool(false);
        // default BottomVal value is false
        slot.stringval = ctx.mkBool(false);
        // default BottomVal value is false
        slot.intval = ctx.mkBool(false);
        
        // default value is range everything
        slot.intrangelo = ctx.mkInt(Long.MIN_VALUE);
        slot.intrangehi = ctx.mkInt(Long.MAX_VALUE);
        
        slot.intvalue = ctx.mkIntConst(String.valueOf(slotID) + "-intvalue");

        return slot;
    }

    public static Z3InferenceValue makeVariableSlot(Context ctx, int slotID) {
    	Z3InferenceValue slot = new Z3InferenceValue(ctx, slotID);

        slot.unknownval = ctx.mkBoolConst(String.valueOf(slotID) + "-TOP");
        slot.bottomval = ctx.mkBoolConst(String.valueOf(slotID) + "-BOTTOM");
        slot.boolval = ctx.mkBoolConst(String.valueOf(slotID) + "-BOOLVAL");
        slot.intrange = ctx.mkBoolConst(String.valueOf(slotID) + "-INTRANGE");
        slot.intval = ctx.mkBoolConst(String.valueOf(slotID) + "-INTVAL");
        slot.stringval = ctx.mkBoolConst(String.valueOf(slotID) + "-STRINGVAL");
        slot.intrangelo = ctx.mkIntConst(String.valueOf(slotID) + "-from");
        slot.intrangehi = ctx.mkIntConst(String.valueOf(slotID) + "-to");
        slot.intvalue = ctx.mkIntConst(String.valueOf(slotID) + "-intvalue");

        return slot;
    }
    
    private Z3InferenceValue(Context ctx, int slotID) {
        this.ctx = ctx;
        this.slotID = slotID;
    }
    
    public void setUnknownVal(boolean val) {
        unknownval = ctx.mkBool(val);
    }

    public BoolExpr getUnknownVal() {
        return unknownval;
    }
    
    public void setBottomVal(boolean val) {
    	bottomval = ctx.mkBool(val);
    }

    public BoolExpr getBottomVal() {
        return bottomval;
    }
    
    public void setBoolVal(boolean val) {
        boolval = ctx.mkBool(val);
    }

    public BoolExpr getBoolVal() {
        return boolval;
    }
    
    public void setIntRange(boolean val) {
    	intrange = ctx.mkBool(val);
    }

    public BoolExpr getIntRange() {
        return intrange;
    }
    
    public void setStringVal(boolean val) {
        stringval = ctx.mkBool(val);
    }

    public BoolExpr getStringVal() {
        return stringval;
    }
    
    public void setIntRangeLower(long val) {
    	intrangelo = ctx.mkInt(val);
    }
    
    public IntExpr getIntRangeLower() {
    	return intrangelo;
    }

    public void setIntRangeUpper(long val) {
    	intrangehi = ctx.mkInt(val);
    }
    
    public IntExpr getIntRangeUpper() {
    	return intrangehi;
    }
    
    public void setIntVal(boolean val) {
        intval = ctx.mkBool(val);
    }

    public BoolExpr getIntVal() {
        return intval;
    }
    
    public void setIntVals(long val) {
    	intvalue = ctx.mkInt(val);
//    	val = removeDuplicates(val);
//    	if (val.size() > MAX_VALUES) {
//    		setIntVal(false);
//    		setIntRange(true);
//    		long valMin = Collections.min(val);
//            long valMax = Collections.max(val);
//    		setIntRangeLower(valMin);
//    		setIntRangeUpper(valMax);
//    	} else {
//	    	for(Long l : val) {
//	    		IntExpr i = ctx.mkInt(l);
//	    		
//	    	}
//    	}
    }
    
    public IntExpr getIntVals() {
    	return intvalue;
    }
    
    private static <T extends Comparable<T>> List<T> removeDuplicates(List<T> values) {
        Set<T> set = new TreeSet<>(values);
        return new ArrayList<>(set);
    }
}
