package it.unipd.dei.es.screenparty.media;

import android.net.Uri;

import java.io.InputStream;

public class MediaParams {

    public enum Type { IMAGE, VIDEO }

    private Uri uri;
    private Type type;
    private InputStream inputStream;
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

    public InputStream getInputStream() {
        return inputStream;
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

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void setFrameWidth(float frameWidth) {
        this.frameWidth = frameWidth;
    }

    public void setFrameHeight(float frameHeight) {
        this.frameHeight = frameHeight;
    }
}
