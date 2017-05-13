package faceassist.faceassist.Components.Fragments.Friends;

import java.util.ArrayList;
import java.util.List;

import faceassist.faceassist.Components.Activities.Profile.LovedOneProfile;
import faceassist.faceassist.Utils.OnInteractorResult;

/**
 * Created by QiFeng on 4/24/17.
 */

public class FriendsInteractorImp implements FriendsContract.Interactor {


    @Override
    public void getAllFaces(OnInteractorResult<LovedOneProfile> onInteractorResult) {
        List<LovedOneProfile> profiles = new ArrayList<>();

        profiles.add(new LovedOneProfile("", "Raymond Zhu", "", "Friend","",""));
        profiles.add(new LovedOneProfile("", "Ayaz Shah", "", "Friend","",""));
        profiles.add(new LovedOneProfile("", "Qi Feng Huang", "", "Me","",""));

        onInteractorResult.onGetAllResultResponse(profiles);
    }
}
