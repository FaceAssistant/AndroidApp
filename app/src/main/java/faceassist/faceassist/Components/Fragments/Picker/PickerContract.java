package faceassist.faceassist.Components.Fragments.Picker;

import java.util.List;

import faceassist.faceassist.Components.Fragments.Picker.Models.BucketItem;
import faceassist.faceassist.Components.Fragments.Picker.Models.GalleryItem;

/**
 * Created by QiFeng on 2/20/17.
 */

public class PickerContract {


    public interface View{
        void showProgress(boolean show);
        void updateBucketAndGallery(List<BucketItem> bucketItems, List<GalleryItem> galleryItems);
        void showUpdatedGallery(List<GalleryItem> galleryItems);
        void runClickedItem(GalleryItem item);
        void showErrorToast();
    }

    public interface Presenter{
        void setUnfilteredGalleryItems(List<GalleryItem> items);
        void start();
        void stop();
        void filter(String filter);
        void clickGalleryItem(GalleryItem item);
    }

}
