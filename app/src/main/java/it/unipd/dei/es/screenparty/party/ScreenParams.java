package it.unipd.dei.es.screenparty.party;

public class ScreenParams {

    private float width;
    private float height;
    private float xdpi;
    private float ydpi;

    public ScreenParams(float width, float height) {
        this(width, height, 0, 0);
    }

    public ScreenParams(float width, float height, float xdpi, float ydpi) {
        this.width = width;
        this.height = height;
        this.xdpi = xdpi;
        this.ydpi = ydpi;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getXdpi() {
        return xdpi;
    }

    public float getYdpi() {
        return ydpi;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}
