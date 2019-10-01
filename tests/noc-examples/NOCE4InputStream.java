package read;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NOCE4InputStream {
    public void unsafeWayOfCasting() throws IOException {
        InputStream in = new FileInputStream("afile");
        @SuppressWarnings("unused")
        byte data;
        // :: error: (cast.unsafe)
        while ((data = (byte) in.read()) != -1) {
            // ...
        }
        in.close();
    }
}
