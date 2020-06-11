package it.unipd.dei.es.screenparty.media;

import android.net.Uri;

/**
 * Class that describe the main component's of the media.
 */
public class MediaParams {

    private Uri uri;
    private int mediaWidth;
    private int mediaHeight;
    private float frameWidth;
    private float frameHeight;

    /**
     * Creates an empty {@link MediaParams}
     */
    public MediaParams() { }

    /**
     * Creates a new {@link MediaParams} with the given params
     * @param uri The media's uri.
     * @param mediaWidth The media's width in pixel.
     * @param mediaHeight The media's Height in pixel.
     */
    public MediaParams(Uri uri, int mediaWidth, int mediaHeight) {
        this.uri = uri;
        this.mediaWidth = mediaWidth;
        this.mediaHeight = mediaHeight;
    }

    /**
     * Gets the uri of the media.
     * @return The uri of the media.
     */
    public Uri getUri() {
        return uri;
    }

    /**
     * Gets the aspect ratio of the media.
     * @return The aspect ratio of the media.
     */
    public float getAspectRatio() {
        return (float)mediaWidth / (float)mediaHeight;
    }

    /**
     * Gets the frame width of the media.
     * @return The frame width of the media.
     */
    public float getFrameWidth() {
        return frameWidth;
    }

    /**
     *Gets the frame height of the media.
     * @return The frame height of the media.
     */
    public float getFrameHeight() {
        return frameHeight;
    }

    /**
     *Sets the frame width of the media.
     * @param frameWidth The new frame width.
     */
    public void setFrameWidth(float frameWidth) {
        this.frameWidth = frameWidth;
    }

    /**
     *Sets the frame height of the media.
     * @param frameHeight The new frame height.
     */
    public void setFrameHeight(float frameHeight) {
        this.frameHeight = frameHeight;
    }
}
