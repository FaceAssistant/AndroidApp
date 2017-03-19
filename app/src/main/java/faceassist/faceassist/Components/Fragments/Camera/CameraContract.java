package faceassist.faceassist.Components.Fragments.Camera;

import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.view.SurfaceHolder;
import android.view.TextureView;

/**
 * Created by QiFeng on 2/20/17.
 */

public class CameraContract {


    public interface View{
        void onImageTaken(Uri image);
        void setPresenter(CameraPresenter presenter);
    }


    public interface Presenter{
        void takePicture(CameraTextureView textureView, boolean flash);
        boolean turnOnFlashLight(boolean turnOn);
        boolean hasActiveCamera();
        boolean safeToTakePictures();
        void start(CameraTextureView textureView, SurfaceTexture surfaceHolder);
        void restart(CameraTextureView textureView, SurfaceTexture holder, int cameraId);
        void swapCamera(CameraTextureView textureView, SurfaceTexture holder, int cameraId);
        void stop(CameraTextureView textureView);
        void release(CameraTextureView textureView);
        void stopBackgroundTasks();

    }
}
