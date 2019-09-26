package value.representation;

import java.util.Collections;
import java.util.List;

import org.checkerframework.javacutil.BugInCF;

import com.microsoft.z3.IntExpr;

/**
 * A data structure class to encapsulate a set of java variables representing values for type
 * checking.
 */
public class TypeCheckValue{
	
	/** The maximum number of values allowed in an annotation's array. */
    protected static final int MAX_VALUES = 10;
    
	private boolean unknownval;
    private boolean bottomval;
    private boolean boolval;
    private boolean stringval;
    
    private boolean intrange;
    private long intrangelo;
    private long intrangehi;
    
    private boolean intval;
    private long intvals;

    public TypeCheckValue() {
		this.unknownval = false;
		this.bottomval = false;
		this.boolval = false;
		this.intrange = false;
		this.stringval = false;
		this.intval = false;
		this.intrangelo = Long.MIN_VALUE;
		this.intrangehi = Long.MAX_VALUE;
	}
    
    public void setUnknownVal(boolean val) {
        unknownval = val;
    }

    public boolean isUnknownVal() {
        return unknownval;
    }
    
    public void setBottomVal(boolean val) {
    	bottomval = val;
    }

    public boolean isBottomVal() {
        return bottomval;
    }
    
    public void setBoolVal(boolean val) {
        boolval = val;
    }

    public boolean isBoolVal() {
        return boolval;
    }
    
    public void setIntRange(boolean val) {
    	intrange = val;
    }

    public boolean isIntRange() {
        return intrange;
    }
    
    public void setStringVal(boolean val) {
        stringval = val;
    }

    public boolean isStringVal() {
        return stringval;
    }
    
    public void setIntRangeLower(long val) {
    	if (!intrange) {
    		throw new BugInCF("Only set when the value is an number");
    	}
    	intrangelo = val;
    }
    
    public long getIntRangeLower() {
    	return intrangelo;
    }

    public void setIntRangeUpper(long val) {
    	if (!intrange) {
    		throw new BugInCF("Only set when the value is an number");
    	}
    	intrangehi = val;
    }
    
    public long getIntRangeUpper() {
    	return intrangehi;
    }
    
    public void setIntVal(boolean val) {
    	intval = val;
    }

    public boolean isIntVal() {
        return intval;
    }
    
    public void setIntVals(long val) {
    	intvals = val;
//    	if (val.size() > MAX_VALUES) {
//    		setIntVal(false);
//    		setIntRange(true);
//    		long valMin = Collections.min(val);
//            long valMax = Collections.max(val);
//    		setIntRangeLower(valMin);
//    		setIntRangeUpper(valMax);
//    	} else {
//	    	intvals = val;
//    	}
    }
    
    public long getIntVals() {
    	return intvals;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.unknownval) {
        	sb.append("@UnknownVal");
        }
        if (this.bottomval) {
        	sb.append("@BottomVal");
        }
        if (this.stringval) {
        	sb.append("@StringVal");
        }
        if (this.boolval) {
        	sb.append("@BoolVal");
        }
        if (this.intval) {
        	sb.append("@IntVal(" + this.intvals + ")");
        }
        if (this.intrange) {
        	sb.append("@IntRange(from = " + this.intrangelo + ", to = " + this.intrangehi);
        }
        
        return sb.toString();
    }
}