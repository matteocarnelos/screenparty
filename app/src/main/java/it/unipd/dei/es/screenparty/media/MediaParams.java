package it.unipd.dei.es.screenparty.media;

import android.net.Uri;

import java.io.File;

public class MediaParams {

    private Uri uri;
    private File file;
    private float aspectRatio;
    private float frameWidth;
    private float frameHeight;

    public MediaParams() { }

    public MediaParams(Uri uri, File file, float aspectRatio) {
        this.uri = uri;
        this.file = file;
        this.aspectRatio = aspectRatio;
    }

    public Uri getUri() {
        return uri;
    }

    public File getFile() {
        return file;
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

    public void setFile(File file) {
        this.file = file;
        this.uri = Uri.fromFile(file);
    }

    public void setFrameWidth(float frameWidth) {
        this.frameWidth = frameWidth;
    }

    public void setFrameHeight(float frameHeight) {
        this.frameHeight = frameHeight;
    }
}
