import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.tesseract.TessBaseAPI;

import static org.bytedeco.leptonica.global.lept.pixDestroy;
import static org.bytedeco.leptonica.global.lept.pixRead;

public class TestTess {
public static void main(String[] args) {
    BytePointer outText;
    TessBaseAPI api = new TessBaseAPI();
    if (api.Init("D:\\code\\", "chi_sim") != 0) {
        System.err.println("Could not initialize tesseract.");
        System.exit(1);
    }

    // Open input image with leptonica library
    PIX image = pixRead("D:\\code\\123.jpg");
    api.SetImage(image);



    // Get OCR result
    outText = api.GetUTF8Text();
    System.out.println("OCR output:\n" + outText.getString());

    // Destroy used object and release memory
    api.End();
    outText.deallocate();
    pixDestroy(image);
}
}
