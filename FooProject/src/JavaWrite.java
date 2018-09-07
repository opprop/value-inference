import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.Reader;

public class JavaWrite {
    public void unsafeWayOfCastingByte() throws IOException {
        InputStream in = new FileInputStream("in-file");
        OutputStream out = new FileOutputStream("out-file");
        @SuppressWarnings("unused")
        int inbuff = 0;
        byte data;
        while ((inbuff = in.read()) != -1) {
            data = (byte) inbuff;
            // ::warning: (argument.type.incompatible)
            out.write(data);
        }
        in.close();
    }
    
    public void compliantSolutionByte() throws IOException {
        InputStream in = new FileInputStream("in-file");
        OutputStream out = new FileOutputStream("out-file");
        @SuppressWarnings("unused")
        int inbuff = 0;
        byte data;
        while ((inbuff = in.read()) != -1) {
            data = (byte) inbuff;
            out.write(data & 0xff); // OK
        }
        in.close();
    }
}