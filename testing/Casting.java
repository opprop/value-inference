import value.qual.*;

public class Casting {
	
	void narrowingCast(long val) {
		// :: fixable-error: (assignment.type.incompatible)
		@IntRange(from=-128, to=127) byte bval= (byte) val;
		// :: fixable-error: (assignment.type.incompatible)
		@IntRange(from=-32768, to=32767) short sval= (short) val;
		// :: fixable-error: (assignment.type.incompatible)
		@IntRange(from=0, to=65536) char cval= (char) val;
		// :: fixable-error: (assignment.type.incompatible)
		@IntRange(from=-2147483648, to=2147483647) int ival= (int) val;
	}

// Not work
//	void widdeningCast(@IntRange(from=-128, to=127) byte val) {
//		@IntRange(from=-128, to=127) short sval= val;
//		// :: warning: (cast.unsafe)
//		@IntRange(from=0, to=65536) char cval= (char) val;
//		// :: error: (assignment.type.incompatible) :: warning: (cast.unsafe)
//		@IntRange(from=0, to=255) char ucval= (char) (val & 0xff);
//		@IntRange(from=-128, to=127) int ival= val;
//		@IntRange(from=-128, to=127) long lval= val;
//	}
}
