import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;

import org.checkerframework.common.value.qual.IntRange;

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

    public void acceptSignedByte(byte value) throws IOException {}
    public void acceptUnsignedByte(@IntRange(from=0, to=255) byte value) throws IOException {}
    public void acceptSignedByteArray(byte[] value) throws IOException {}
    public void acceptUnsignedByteArray(@IntRange(from=0, to=255) byte[] value) throws IOException {}
    public void acceptChar(char value) throws IOException {}
    public void acceptCharArray(char[] value) throws IOException {}
    public void acceptSignedByteDoubleArray(byte[][] value) throws IOException {}
    public void acceptUnsignedByteDoubleArray(@IntRange(from=0, to=255) byte[][] value) throws IOException {}
    public void acceptCharDoubleArray(char[][] value) throws IOException {}

    public void testPassingArgument(byte byte_val, char char_val, byte[] byte_arr, char[] char_arr, byte[][] byte_double_arr, char[][] char_double_arr) throws IOException {
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

    public void returnRefinementCheck() throws IOException {
        byte data = (@IntRange(from=0, to=255) byte) returnUnsighByte();    //OK
    }

    public void wideningCheck(@IntRange(from=0, to=255) byte data) throws IOException {
        //:: warning: (argument.type.incompatible)
        int value1 = data;
        value1 = data;
        
        int value2 = data & 0xFF;
        value2 = data & 0xFF; // OK
        int value3 = data & 0xff;
        value3 = data & 0xff; // OK
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
}