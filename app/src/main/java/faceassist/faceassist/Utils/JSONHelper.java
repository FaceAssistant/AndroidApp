package faceassist.faceassist.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiFeng on 2/7/17.
 */

public class JSONHelper {


    public static String getString(String key, JSONObject jsonObject){
        try {
            return jsonObject.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getInt(String key, JSONObject jsonObject){
        try {
            return jsonObject.getInt(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static double getDouble(String key, JSONObject jsonObject){
        try {
            return jsonObject.getDouble(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
