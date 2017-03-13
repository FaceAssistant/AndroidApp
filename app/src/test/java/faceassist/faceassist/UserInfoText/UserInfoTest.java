package faceassist.faceassist.UserInfoText;

import android.content.Context;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import faceassist.faceassist.UserInfo;

/**
 * Created by QiFeng on 3/12/17.
 */

public class UserInfoTest {


    @Test
    public void assertNull(){
        UserInfo info = UserInfo.getInstance();
        Assert.assertTrue(info == null);
    }

    @Test
    public void assertEmpty(){

    }

}
