package faceassist.faceassist.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by QiFeng on 1/31/17.
 */

public class ImageUtils {

    public static String encodeImageBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream); //NOTE: Change Compression as needed
        byte[] byteFormat = stream.toByteArray();
        // get the base 64 string
        return Base64.encodeToString(byteFormat, Base64.NO_WRAP);
    }

    public static File savePicture(Context context, Bitmap bitmap) {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "FaceAssist"
        );

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = SimpleDateFormat.getDateTimeInstance().format(new Date());
        File mediaFile = new File(
                mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg"
        );
        if(!mediaFile.exists()){
            try {
                mediaFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Saving the bitmap
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            FileOutputStream stream = new FileOutputStream(mediaFile);
            stream.write(out.toByteArray());
            stream.close();

        } catch (IOException exception) {
            exception.printStackTrace();
        }

        // Mediascanner need to scan for the image saved
        Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri fileContentUri = Uri.fromFile(mediaFile);
        mediaScannerIntent.setData(fileContentUri);
        context.sendBroadcast(mediaScannerIntent);

        return mediaFile;
    }


    public static File savePictureToCache(Context context, Bitmap bitmap) {
        if (context == null) return  null;

        String timeStamp = SimpleDateFormat.getDateTimeInstance().format(new Date());
        File image = new File(
                context.getCacheDir() +  File.separator + "IMG_" + timeStamp + ".jpg");
        // Saving the bitmap
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            FileOutputStream stream = new FileOutputStream(image);
            stream.write(out.toByteArray());
            stream.close();

        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return image;
    }
}
