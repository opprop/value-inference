import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;

import org.checkerframework.common.value.qual.IntRange;

public class Foo {
    public void unsafeWayOfCastingByte1() throws IOException {
        InputStream in = new FileInputStream("afile");
        @SuppressWarnings("unused")
        byte data;
        //:: warning: (cast.unsafe)
        while ((data = (byte) in.read()) != -1) {
            //...
        }
        in.close();
    }
    
    public void compliantSolutionByte() throws IOException {
        InputStream in = new FileInputStream("afile");
        int inbuff = 0;
        @SuppressWarnings("unused")
        byte data;
        while ((inbuff = in.read()) != -1) {
            data = (byte) inbuff; // OK
        }
        in.close();
    }

    public void unsafeWayOfCastingChar() throws IOException {
        Reader in = new FileReader("afile");
        @SuppressWarnings("unused")
        char data;
        //:: warning: (cast.unsafe)
        while ((data = (char) in.read()) != -1) {
            //...
        }
        in.close();
    }

    public void compliantSolutionChar() throws IOException {
        Reader in = new FileReader("afile");
        int inbuff;
        @SuppressWarnings("unused")
        char data;
        while ((inbuff = in.read()) != -1) {
            data = (char) inbuff; // OK
        }
        in.close();
    }

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
        byte data = (@IntRange(from=0, to=255) byte) value;

        //:: error: (argument.type.incompatible)
        acceptSignedByte(data);

        acceptUnsignedByte(data);   // OK
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
}