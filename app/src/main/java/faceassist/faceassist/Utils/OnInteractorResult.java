package faceassist.faceassist.Utils;

import java.util.List;

/**
 * Created by QiFeng on 4/2/17.
 */

public interface OnInteractorResult <T> {
    void onGetAllResultResponse(List<T> results);
    void onDeleteResponse(boolean deleted, int pos, T profile);
    void onFailed();
    void onError();
}
