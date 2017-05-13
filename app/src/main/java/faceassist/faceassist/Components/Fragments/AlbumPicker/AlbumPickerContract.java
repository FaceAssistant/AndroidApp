package faceassist.faceassist.Components.Fragments.AlbumPicker;

import java.util.List;
/**
 * Created by QiFeng on 5/12/17.
 */

public class AlbumPickerContract {

    public interface View{
        void showProgress(boolean show);
        void updateAlbums(List<Album> albums);
        void runClickedItem(Album item);
        void showErrorToast();
        void setPresenter(Presenter presenter);
    }

    public interface Presenter{
        void start();
        void stop();
        void clickItem(Album item);
    }
}
