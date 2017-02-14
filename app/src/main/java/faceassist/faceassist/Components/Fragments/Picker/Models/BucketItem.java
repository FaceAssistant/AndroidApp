package faceassist.faceassist.Components.Fragments.Picker.Models;

/**
 * Created by QiFeng on 2/13/17.
 */

public class BucketItem {
    public final String id;
    public final String name;


    public BucketItem(String id, String name){
        this.id = id;
        this.name = name;
    }


    @Override
    public boolean equals(Object o) {
        return o instanceof BucketItem && ((BucketItem)o).id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return this.name;
    }
}