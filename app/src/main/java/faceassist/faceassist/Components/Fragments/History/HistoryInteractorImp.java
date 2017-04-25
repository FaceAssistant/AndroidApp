package faceassist.faceassist.Components.Fragments.History;

import java.util.ArrayList;
import java.util.List;

import faceassist.faceassist.Components.Activities.Profile.BaseProfile;
import faceassist.faceassist.Utils.OnInteractorResult;

/**
 * Created by QiFeng on 4/24/17.
 */

public class HistoryInteractorImp implements HistoryContract.Interactor {

    @Override
    public void getAllFaces(OnInteractorResult<BaseProfile> onInteractorResult) {
        List<BaseProfile> profiles = new ArrayList<>();

        profiles.add(new BaseProfile("12345", "Qi Feng Huang"));
        profiles.add(new BaseProfile("123445", "Scarlett Johanson"));
        profiles.add(new BaseProfile("qwqw", "Barack Obama"));

        onInteractorResult.onGetAllResultResponse(profiles);
    }
}
