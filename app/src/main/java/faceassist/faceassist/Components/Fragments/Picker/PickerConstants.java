package faceassist.faceassist.Components.Fragments.Picker;

import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by QiFeng on 3/19/17.
 */

public class PickerConstants {

    public static final String[] PROJECTION = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.BUCKET_ID
    };

    public static final String SELECTION = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
            + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

    public static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");

    public static final String SORT_BY = MediaStore.Files.FileColumns.DATE_ADDED + " ASC"; // Sort order.

}
