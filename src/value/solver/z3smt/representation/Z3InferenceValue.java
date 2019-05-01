package value.solver.z3smt.representation;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;

public class Z3InferenceValue {
	
	private final Context ctx;
	
	private int slotID;
	
	private BoolExpr unknownval;
    private BoolExpr bottomval;
    private BoolExpr boolval;
    private BoolExpr intrange;
    private BoolExpr stringval;
    
    private IntExpr intrangelo;
    private IntExpr intrangehi;
    
    public static Z3InferenceValue makeConstantSlot(Context ctx, int slotID) {
    	Z3InferenceValue slot = new Z3InferenceValue(ctx, slotID);

        // default UnknownVal value is false
        slot.unknownval = ctx.mkBool(false);
        // default BottomVal value is false
        slot.bottomval = ctx.mkBool(false);
        // default BottomVal value is false
        slot.boolval = ctx.mkBool(false);
        // default BottomVal value is false
        slot.intrange = ctx.mkBool(true);
        // default BottomVal value is false
        slot.stringval = ctx.mkBool(false);
        
        // default value is range everything
        slot.intrangelo = ctx.mkInt(Long.MIN_VALUE);
        slot.intrangehi = ctx.mkInt(Long.MAX_VALUE);

        return slot;
    }

    public static Z3InferenceValue makeVariableSlot(Context ctx, int slotID) {
    	Z3InferenceValue slot = new Z3InferenceValue(ctx, slotID);

        slot.unknownval = ctx.mkBoolConst(String.valueOf(slotID) + "-TOP");
        slot.bottomval = ctx.mkBoolConst(String.valueOf(slotID) + "-BOTTOM");
        slot.boolval = ctx.mkBoolConst(String.valueOf(slotID) + "-BOOLVAL");
        slot.intrange = ctx.mkBoolConst(String.valueOf(slotID) + "-INTRANGE");
        slot.stringval = ctx.mkBoolConst(String.valueOf(slotID) + "-STRINGVAL");
        slot.intrangelo = ctx.mkIntConst(String.valueOf(slotID) + "-from");
        slot.intrangehi = ctx.mkIntConst(String.valueOf(slotID) + "-to");

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
}
