import value.qual.*;

public class Casting {
	
	void narrowingCast(long val) {
		@IntRange(from=-128, to=127) byte bval= (byte) val;
		@IntRange(from=-32768, to=32767) short sval= (short) val;
		@IntRange(from=0, to=65536) char cval= (char) val;
		@IntRange(from=-2147483648, to=2147483647) int ival= (int) val;
	}
	
	void widdeningCast(@IntRange(from=-128, to=127) byte val) {
		@IntRange(from=-128, to=127) short sval= val;
		@IntRange(from=0, to=65536) char cval= (char) val;
		@IntRange(from=0, to=255) char ucval= (char) (val & 0xff);
		@IntRange(from=-128, to=127) int ival= val;
		@IntRange(from=-128, to=127) long lval= val;
	}
}
