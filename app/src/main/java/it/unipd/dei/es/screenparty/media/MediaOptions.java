package it.unipd.dei.es.screenparty.media;

public class MediaOptions {

    public enum Position { LEFT, CENTER, RIGHT }

    private Position position;
    private float frameWidth;
    private float frameHeight;

    public MediaOptions(Position position, float frameWidth, float frameHeight) {
        this.position = position;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }

    // TODO: Update also frameWidth
    public void update(float height) {
        if(height < this.frameHeight)
            this.frameHeight = height;
    }

    public Position getPosition() {
        return position;
    }

    public float getFrameWidth() {
        return frameWidth;
    }

    public float getFrameHeight() {
        return frameHeight;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setFrameWidth(float frameWidth) {
        this.frameWidth = frameWidth;
    }

    public void setFrameHeight(float frameHeight) {
        this.frameHeight = frameHeight;
    }
}
