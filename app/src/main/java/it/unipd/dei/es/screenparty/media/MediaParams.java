package it.unipd.dei.es.screenparty.media;

import android.net.Uri;

public class MediaParams {

    private Uri uri;
    private float aspectRatio;
    private float frameWidth;
    private float frameHeight;

    public MediaParams() { }

    public MediaParams(Uri uri, float aspectRatio) {
        this.uri = uri;
        this.aspectRatio = aspectRatio;
    }

    public Uri getUri() {
        return uri;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public float getFrameWidth() {
        return frameWidth;
    }

    public float getFrameHeight() {
        return frameHeight;
    }

    public void setFrameWidth(float frameWidth) {
        this.frameWidth = frameWidth;
    }

    public void setFrameHeight(float frameHeight) {
        this.frameHeight = frameHeight;
    }
}
