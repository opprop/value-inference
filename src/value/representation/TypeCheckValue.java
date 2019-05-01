package value.representation;

import org.checkerframework.javacutil.BugInCF;

/**
 * A data structure class to encapsulate a set of java variables representing values for type
 * checking.
 */
public class TypeCheckValue{
	
	private boolean unknownval;
    private boolean bottomval;
    private boolean boolval;
    private boolean intrange;
    private boolean stringval;
    
    private long intrangelo;
    private long intrangehi;
    
	public TypeCheckValue() {
		this.unknownval = false;
		this.bottomval = false;
		this.boolval = false;
		this.intrange = false;
		this.stringval = false;
		this.intrangelo = Long.MIN_VALUE;
		this.intrangehi = Long.MAX_VALUE;
	}
    
    public void setUnknownVal(boolean val) {
    	if (unknownval && bottomval) {
    		throw new BugInCF("Cannot set top and bottom both to true at the same time");
    	}
        unknownval = val;
    }

    public boolean isUnknownVal() {
        return unknownval;
    }
    
    public void setBottomVal(boolean val) {
    	if (unknownval && bottomval) {
    		throw new BugInCF("Cannot set top and bottom both to true at the same time");
    	}
    	bottomval = val;
    }

    public boolean isBottomVal() {
        return bottomval;
    }
    
    public void setBoolVal(boolean val) {
    	if ((boolval && intrange) || (boolval && stringval) || (intrange && stringval)) {
    		throw new BugInCF("Can only be one of int, bool, or string");
    	}
        boolval = val;
    }

    public boolean isBoolVal() {
        return boolval;
    }
    
    public void setIntRange(boolean val) {
    	if ((boolval && intrange) || (boolval && stringval) || (intrange && stringval)) {
    		throw new BugInCF("Can only be one of int, bool, or string");
    	}
    	intrange = val;
    }

    public boolean isIntRange() {
        return intrange;
    }
    
    public void setStringVal(boolean val) {
    	if ((boolval && intrange) || (boolval && stringval) || (intrange && stringval)) {
    		throw new BugInCF("Can only be one of int, bool, or string");
    	}
        stringval = val;
    }

    public boolean getStringVal() {
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
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.unknownval) {
        	sb.append("@UnknownVal");
        }
        if (this.bottomval) {
        	sb.append("@BottomVal");
        }
        if (this.intrange) {
        	sb.append("@IntRange(from = " + this.intrangelo + ", to = " + this.intrangehi);
        }
        
        return sb.toString();
    }
}