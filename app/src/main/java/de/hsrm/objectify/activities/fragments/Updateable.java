package de.hsrm.objectify.activities.fragments;

import android.graphics.Bitmap;

import de.hsrm.objectify.rendering.ObjectModel;

public interface Updateable {

    void update(ObjectModel objectModel, Bitmap heightmap, Bitmap normalmap);
}
