package it.unipd.dei.es.screenparty.media;

import android.net.Uri;

public class MediaParams {

    public enum Type { IMAGE, VIDEO }

    private Uri uri;
    private Type type;
    private float aspectRatio;
    private float frameWidth;
    private float frameHeight;

    public MediaParams() { }

    public MediaParams(Uri uri, Type type, float aspectRatio) {
        this.uri = uri;
        this.type = type;
        this.aspectRatio = aspectRatio;
    }

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

    public void setFrameWidth(float frameWidth) {
        this.frameWidth = frameWidth;
    }

    public void setFrameHeight(float frameHeight) {
        this.frameHeight = frameHeight;
    }
}