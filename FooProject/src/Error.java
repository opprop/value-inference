import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.Reader;

import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.common.value.qual.IntVal;

public class Error {
	/**
     * Invokes the delegate's <code>read()</code> method.
     * @return the byte read or -1 if the end of stream
     * @throws IOException if an I/O error occurs
     * @throws EOFException if an end of file is reached unexpectedly
     * commons-io/src/main/java/org/apache/commons/io/input/SwappedDataInputStream.java:73
     */
    public byte readByte() throws IOException, EOFException
    {
    	InputStream in = new FileInputStream("afile");
        return (byte)in.read();
    }
    
    /**
     * Writes a "int" value to an OutputStream. The value is
     * converted to the opposed endian system while writing.
     * @param output target OutputStream
     * @param value value to write
     * @throws IOException in case of an I/O problem
     * commons-io/src/main/java/org/apache/commons/io/EndianUtils.java:327
     */
    public static void writeSwappedInteger(final OutputStream output, final int value)
        throws IOException
    {
        output.write( (byte)( ( value >> 0 ) & 0xff ) );
        output.write( (byte)( ( value >> 8 ) & 0xff ) );
        output.write( (byte)( ( value >> 16 ) & 0xff ) );
        output.write( (byte)( ( value >> 24 ) & 0xff ) );
    }
    
    /**
     * Needed in readInstruction and subclasses in this package
     * @since 6.0
     * commons-bcel/src/main/java/org/apache/bcel/generic/Instruction.java:531
     */
    final void setLength( final int length ) {
        short length = (short) length; // TODO check range?
    }

    /**
     * Set the local variable index.
     * also updates opcode and length
     * TODO Why?
     * @see #setIndexOnly(int)
     * commons-bcel/src/main/java/org/apache/bcel/generic/LocalVariableInstruction.java:173
     */
    public void setIndex( final int n , short c_tag, int n) { // TODO could be package-protected?
        if ((n < 0) || (n > Const.MAX_SHORT)) {
            throw new ClassGenException("Illegal value: " + n);
        }
        // Cannot be < 0 as this is checked above
        if (n <= 3) { // Use more compact instruction xLOAD_n
            short value = (short) (c_tag + n);
        }
    }
    
    /**
     * commons-bcel/src/main/java/org/apache/bcel/classfile/Signature.java:185
     */
    private static void matchIdent( final InputStream in, final StringBuilder buf ) {
        int ch = in.read();
        do {
            buf.append((char) ch);
            ch = in.read();
        } while (ch != -1);
    }
    
    /**
     * commons-imaging/src/main/java/org/apache/commons/imaging/util/Debug.java:154
     */
    private static String byteQuadToString(final int bytequad) {
        final byte b1 = (byte) ((bytequad >> 24) & 0xff);
        final byte b2 = (byte) ((bytequad >> 16) & 0xff);
        final byte b3 = (byte) ((bytequad >> 8) & 0xff);
        final byte b4 = (byte) ((bytequad >> 0) & 0xff);

        final char c1 = (char) b1;
        final char c2 = (char) b2;
        final char c3 = (char) b3;
        final char c4 = (char) b4;

        final StringBuilder buffer = new StringBuilder(31);
        buffer.append(new String(new char[]{c1, c2, c3, c4}));
        buffer.append(" bytequad: ");
        buffer.append(bytequad);
        buffer.append(" b1: ");
        buffer.append(b1);
        buffer.append(" b2: ");
        buffer.append(b2);
        buffer.append(" b3: ");
        buffer.append(b3);
        buffer.append(" b4: ");
        buffer.append(b4);

        return buffer.toString();
    }
    
    /**
     * commons-imaging/src/main/java/org/apache/commons/imaging/formats/psd/datareaders/UncompressedDataReader.java:61
     */
    public void readData(final InputStream is, final int[][]][] data) throws ImageReadException, IOException {
	    final int b = in.read();
	    data[0][0][0] = (byte) b;
    }
}
