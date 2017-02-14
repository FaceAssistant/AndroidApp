package faceassist.faceassist.Utils;

import android.util.Base64;

import java.io.File;
import java.io.IOException;

/**
 * Created by QiFeng on 2/14/17.
 */

public class FileUtils {

    public static String encodeFileBase64(File file) throws IOException {
        byte[] filebyte = org.apache.commons.io.FileUtils.readFileToByteArray(file);
        return Base64.encodeToString(filebyte, Base64.NO_WRAP);
        //return Base64.encodeToString(byteFormat, Base64.URL_SAFE);
    }
}
