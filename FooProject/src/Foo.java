import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.Reader;

import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.common.value.qual.IntVal;

public class Foo {
    public byte returnUnsighByteError() throws IOException {
        InputStream in = new FileInputStream("afile");
        @SuppressWarnings("unused")
        int data;
        while ((data = in.read()) != -1) {
            //:: error: (return.type.incompatible)
            return (byte) data;
        }

        return (byte) 0;
    }

    public @IntRange(from=0, to=255) byte returnUnsighByte() throws IOException {
        InputStream in = new FileInputStream("afile");
        @SuppressWarnings("unused")
        int data;
        while ((data = in.read()) != -1) {
            return (byte) data;    // OK
        }

        return (byte) 0;
    }

    public void assignRefinementCheck() throws IOException {
        InputStream in = new FileInputStream("afile");
        int inbuff = 0;
        @SuppressWarnings("unused")
        byte data;

        if ((inbuff = in.read()) != -1) {
            data = (byte) inbuff;    // OK
            @IntRange(from=0, to=255) byte value = data; //OK
        }

        in.close();
    }

    public byte[] returnByteArray() throws IOException { return null; }
    public byte returnSignedByte() throws IOException { return 0; }

    public void returnRefinementCheck() throws IOException {
        @IntRange(from=0, to=255) byte data1 = (byte) returnUnsighByte();    //OK
        //:: error: (assignment.type.incompatible)
        @IntRange(from=0, to=255) byte data2 = (byte) returnUnsighByteError();
        
        @IntRange(from=-128, to=127) byte data3 = returnByteArray()[0];  //OK

    }

    public int wideningCheck(@IntRange(from=0, to=255) byte unsigned, byte signed) throws IOException {
        //:: warning: (assignment.type.incompatible)
        int value1 = unsigned;
        value1 = unsigned;
        
        int value2 = signed;	// OK
        value2 = signed;		// OK
        
        int value3 = unsigned & 0xFF;	// OK
        value3 = unsigned & 0xFF; 		// OK
        int value4 = unsigned & 0xff;	// OK
        value4 = unsigned & 0xff; 		// OK
        
		//:: warning: (assignment.type.incompatible)
		int add = unsigned + 1;
		//:: warning: (assignment.type.incompatible)
		add = unsigned + 1;
        add = (unsigned & 0xff) + 1; // OK
        
		int add2 = signed + 1;	// OK
		add2 = signed + 1;		// OK
		
		return unsigned;
    }
    
    public void intValBoundTest() throws IOException {
    	byte value1 = (byte) 255;	// OK
    	short value2 = (short) 65535;	// OK
    	char value3 = (char) 65535;	// OK
    	
    	//:: warning: (cast.unsafe)
    	byte value4 = (byte) 256;
    	//:: warning: (cast.unsafe)
    	short value5 = (short) 65536;
    	//:: warning: (cast.unsafe)
    	char value6 = (char) 65536;
    	
    	byte value7 = (byte) -128;	// OK
    	short value8 = (short) -32768;	// OK
    	char value9 = (char) 0;	// OK
    	
    	//:: warning: (cast.unsafe)
    	byte value10 = (byte) -129;
    	//:: warning: (cast.unsafe)
    	short value11 = (short) -32769;
    	//:: warning: (cast.unsafe)
    	char value12 = (char) -1;
    }

	public void acceptByteArray(byte[] value) throws IOException {}
    public void acceptSignedByte(byte value) throws IOException {}
    public void acceptUnsignedByte(@IntRange(from=0, to=255) byte value) throws IOException {}
    public void acceptSignedByteArray(byte[] value) throws IOException {}
    public void acceptUnsignedByteArray(@IntRange(from=0, to=255) byte[] value) throws IOException {}
    public void acceptChar(char value) throws IOException {}
    public void acceptCharArray(char[] value) throws IOException {}
    public void acceptSignedByteDoubleArray(byte[][] value) throws IOException {}
    public void acceptUnsignedByteDoubleArray(@IntRange(from=0, to=255) byte[][] value) throws IOException {}
    public void acceptCharDoubleArray(char[][] value) throws IOException {}

    public void testPassingArgument(@IntRange(from=0, to=255) int value, byte byte_val, char char_val, byte[] byte_arr, char[] char_arr, byte[][] byte_double_arr, char[][] char_double_arr) throws IOException {
		  byte data = (byte) value;
		
		  //:: error: (argument.type.incompatible)
		  acceptSignedByte(data);
		
		  acceptUnsignedByte(data);   // OK
		
		  byte signed = returnSignedByte();
		  byte[] data_array = {signed};
		  acceptByteArray(data_array);    // OK
		
		  //:: error: (argument.type.incompatible)
		  byte unsigned = returnUnsighByte();
		  byte[] unsigned_array = {unsigned};
		  //:: error: (argument.type.incompatible)
		  acceptByteArray(unsigned_array);
		  acceptByteArray(returnByteArray());    	// OK
	
        //:: error: (argument.type.incompatible)
        acceptUnsignedByte(byte_val);
        acceptSignedByte(byte_val);   // OK
    	acceptChar(char_val);   // OK
        
        //:: error: (argument.type.incompatible)
    	acceptUnsignedByteArray(byte_arr);
        acceptSignedByteArray(byte_arr);	// OK
        acceptCharArray(char_arr);	// OK
        
        //:: error: (argument.type.incompatible)
        acceptUnsignedByteDoubleArray(byte_double_arr);
    	acceptSignedByteDoubleArray(byte_double_arr);	// OK
    	acceptCharDoubleArray(char_double_arr);	// OK
    }

    public void testParameter(byte value, char value2) throws IOException {
        //:: error: (assignment.type.incompatible)
        @IntRange(from=0, to=255) byte data1 = value;

        @IntRange(from=-128, to=127) byte data2 = value;    // OK
        @IntRange(from=0, to=65535) char data3 = value2;   //OK
    }

    public void testDigitExcute(String orig) throws IOException {
        char octal_char = 0;
        int this_esc = orig.indexOf('\\');
        int ii = this_esc + 1;
        while (ii < orig.length()) {
            char ch = orig.charAt(ii++);
            if ((ch < '0') || (ch > '8')) {
             break;
            }
            char octal_char_cast = (char) ((octal_char * 8) + Character.digit(ch, 8));
        }
    }
	
	byte byte_field;
	char char_field;
	public void testField() {
		//:: error: (assignment.type.incompatible)
		@IntRange(from=0, to=255) byte value1 = byte_field;
		
		@IntRange(from=-128, to=127) byte value3 = byte_field;	// OK
		@IntRange(from=0, to=65535) char value2 = char_field;	// OK
	}
    
	byte[] byte_field_array;
	char[] char_field_array;
	byte[][] byte_double_array;
	char[][] char_double_array;
	public void testArray(byte[] byte_array, char[] char_array) {
		//:: error: (assignment.type.incompatible)
		@IntRange(from=0, to=255) byte value = byte_array[0];
		
		//:: error: (assignment.type.incompatible)
		@IntRange(from=0, to=255) byte value1 = byte_field_array[0];
		
		@IntRange(from=-128, to=127) byte value2 = byte_array[0];			// OK
		@IntRange(from=-128, to=127) byte value3 = byte_field_array[0];		// OK
		@IntRange(from=0, to=65535) char value4 = char_array[0];			// OK
		@IntRange(from=0, to=65535) char value5 = char_field_array[0];		// OK
		
		//:: error: (assignment.type.incompatible)
		@IntRange(from=0, to=255) byte value6 = byte_double_array[0][0];
		
		@IntRange(from=-128, to=127) byte value7 = byte_double_array[0][0];	// OK
		@IntRange(from=0, to=65535) char value8 = char_double_array[0][0];	// OK
		
	}
	
	public static long readSwappedUnsignedInteger(final @IntRange(from=0, to=255) byte[] data, int offset) {
        final long low = ( ( ( data[ offset + 0 ] & 0xff ) << 0 ) +
                     ( ( data[ offset + 1 ] & 0xff ) << 8 ) +
                     ( ( data[ offset + 2 ] & 0xff ) << 16 ) );

        final long high = data[ offset + 3 ] & 0xff;
        
        return (high << 24) + (0xffffffffL & low); 
    }
	
	private static void toBytes(final long bits, final @IntRange(from=0, to=255) byte[] result) {
		result[0] = (byte) (0xff & bits);			// OK
		result[1] = (byte) (bits & 0xff);			// OK
        result[2] = (byte) ((bits >> 0) & 0xff);	// OK
        result[3] = (byte) (0xff & (bits >> 8));	// OK
    }
}