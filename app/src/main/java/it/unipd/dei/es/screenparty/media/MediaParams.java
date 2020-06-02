package it.unipd.dei.es.screenparty.media;

import android.net.Uri;

public class MediaParams {

    public enum Type { IMAGE, VIDEO }

    private Uri uri;
    private Type type;
    private float aspectRatio;
    private float frameWidth;
    private float frameHeight;

    public Uri getUri() {
        return uri;
    }

    public Type getType() {
        return type;
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

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public void setFrameWidth(float frameWidth) {
        this.frameWidth = frameWidth;
    }

    public void setFrameHeight(float frameHeight) {
        this.frameHeight = frameHeight;
    }
}
