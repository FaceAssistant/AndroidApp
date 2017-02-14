package faceassist.faceassist.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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


    public static Bitmap decodeUri(Context c, Uri uri, final int requiredSize)
            throws IOException, NullPointerException{

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth
                , height_tmp = o.outHeight;
        int scale = 1;

        while(true) {
            if(width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;

        Rect rect = new Rect();
        if (o.outWidth > o.outHeight){
            int half = (o.outWidth - o.outHeight) / 2;
            rect.top = 0;
            rect.bottom = o.outHeight;
            rect.left = half;
            rect.right = half + o.outHeight;
        }else if(o.outHeight > o.outWidth){
            int half = (o.outHeight - o.outWidth) / 2;
            rect.top = half;
            rect.bottom = half + o.outWidth;
            rect.left = 0;
            rect.right = o.outWidth;
        }else {
            rect.left = 0;
            rect.top = 0;
            rect.right = o.outWidth;
            rect.bottom = o.outHeight;
        }

        BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(c.getContentResolver().openInputStream(uri), true);
        Bitmap cropped = decoder.decodeRegion(rect, o2);
        decoder.recycle();
        return cropped;

        //return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }
}
