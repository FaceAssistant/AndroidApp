package faceassist.faceassist.Components.Activities.AddFace.Models;

/**
 * Created by QiFeng on 2/13/17.
 */

public class TextEntry extends Entry {

    private String mTitle;
    private String mBody;

    public TextEntry(String title){
        mTitle = title;
    }

    public String getTitle(){
        return mTitle;
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(String body) {
        mBody = body;
    }

    @Override
    public String getContent() {
        return mBody;
    }
}
