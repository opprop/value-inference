package value.solver.z3smt.representation;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;

public class Z3InferenceValue {
	
	private final Context ctx;
	
	private String slotID;
	
	private BoolExpr unknownval;
    private BoolExpr bottomval;
    private BoolExpr boolval;
    private BoolExpr numval;
    private BoolExpr stringval;
    
    private IntExpr intrangelo;
    private IntExpr intrangehi;
    
    public static Z3InferenceValue makeConstantSlot(Context ctx) {
    	Z3InferenceValue slot = new Z3InferenceValue(ctx);

        // default UnknownVal value is false
        slot.unknownval = ctx.mkBool(false);
        // default BottomVal value is false
        slot.bottomval = ctx.mkBool(false);
        // default BottomVal value is false
        slot.boolval = ctx.mkBool(false);
        // default BottomVal value is false
        slot.numval = ctx.mkBool(true);
        // default BottomVal value is false
        slot.stringval = ctx.mkBool(false);
        
        // default value is range everything
        slot.intrangelo = ctx.mkInt(Long.MIN_VALUE);
        slot.intrangehi = ctx.mkInt(Long.MAX_VALUE);

        return slot;
    }

    public static Z3InferenceValue makeVariableSlot(Context ctx) {
    	Z3InferenceValue slot = new Z3InferenceValue(ctx);

        slot.unknownval = ctx.mkBoolConst(String.valueOf("slotID") + "TOP");
        slot.bottomval = ctx.mkBoolConst(String.valueOf("slotID") + "BOTTOM");
        slot.boolval = ctx.mkBoolConst(String.valueOf("slotID") + "BOOLVAL");
        slot.numval = ctx.mkBoolConst(String.valueOf("slotID") + "INTRANGE");
        slot.stringval = ctx.mkBoolConst(String.valueOf("slotID") + "STRINGVAL");
        slot.intrangelo = ctx.mkIntConst(String.valueOf("slotID") + "from");
        slot.intrangehi = ctx.mkIntConst(String.valueOf("slotID") + "to");

        return slot;
    }
    
    private Z3InferenceValue(Context ctx) {
        this.ctx = ctx;
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
    
    public void setNumVal(boolean val) {
        numval = ctx.mkBool(val);
    }

    public BoolExpr getNumVal() {
        return numval;
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
