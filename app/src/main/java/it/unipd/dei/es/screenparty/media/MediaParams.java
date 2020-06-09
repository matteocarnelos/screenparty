package it.unipd.dei.es.screenparty.media;

import android.net.Uri;

public class MediaParams {

    private Uri uri;
    private int mediaWidth;
    private int mediaHeight;
    private float frameWidth;
    private float frameHeight;

    public MediaParams() { }

    public MediaParams(Uri uri, int mediaWidth, int mediaHeight) {
        this.uri = uri;
        this.mediaWidth = mediaWidth;
        this.mediaHeight = mediaHeight;
    }

    public Uri getUri() {
        return uri;
    }

    public float getAspectRatio() {
        return (float)mediaWidth / (float)mediaHeight;
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
