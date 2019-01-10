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

    public void testPassingArgument(@IntRange(from=0, to=255) int value) throws IOException {
        byte data = (byte) value;

        //:: error: (argument.type.incompatible)
        acceptSignedByte(data);

        acceptUnsignedByte(data);   // OK
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
	public void testField() {
		//:: error: (assignment.type.incompatible)
		@IntRange(from=0, to=255) byte value1 = byte_field;
		
		@IntRange(from=-128, to=127) byte value2 = byte_field;	// OK
		
	}
    
	byte[] field_array;
	@IntRange(from=-128, to=127) byte[] field_annoArray;
	
	public void testArray(byte[] array, @IntRange(from=-128, to=127) byte[] annoArray) {
		
		@IntRange(from=-128, to=127) byte value1 = array[0];
		@IntRange(from=-128, to=127) byte value2 = annoArray[0];
		
		@IntRange(from=-128, to=127) byte value3 = field_array[0];
		@IntRange(from=-128, to=127) byte value4 = field_annoArray[0];
		
	}
}