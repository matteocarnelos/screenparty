package it.unipd.dei.es.screenparty.party;

/**
 * Stores both horizontal and vertical dimensions and pixel density of the screen of a device.
 */
public class ScreenParams {

    private float width;
    private float height;
    private float xdpi;
    private float ydpi;

    /**
     * Builder of ScreenParams. It sets the width and the heigth as indicated by the given
     * parameters.
     * @param width: The width of the screen.
     * @param height: The height of the screen.
     */
    public ScreenParams(float width, float height) {
        this(width, height, 0, 0);
    }

    /**
     * Builder of ScreenParams. it sets the width, the height, the horizontal pixel density and
     * the vertical pixel density as indicated by the given parameters.
     * @param width: The width of the screen.
     * @param height: The height of the screen.
     * @param xdpi: The horizontal pixel density of the screen.
     * @param ydpi: The vertical pixel density of the screen.
     */
    public ScreenParams(float width, float height, float xdpi, float ydpi) {
        this.width = width;
        this.height = height;
        this.xdpi = xdpi;
        this.ydpi = ydpi;
    }

    /**
     * Get the width of the screen.
     * @return The width of the screen.
     */
    public float getWidth() {
        return width;
    }

    /**
     * Get the height of the screen.
     * @return The height of the screen.
     */
    public float getHeight() {
        return height;
    }

    /**
     * Get the horizontal pixel density of the screen.
     * @return The horizontal pixel density of the screen.
     */
    public float getXdpi() {
        return xdpi;
    }

    /**
     * Get the vertical pixel density of the screen.
     * @return The vertical pixel density of the screen.
     */
    public float getYdpi() {
        return ydpi;
    }

    /**
     * Sets the height of the screen.
     * @param height: The new height of the screen.
     */
    public void setHeight(float height) {
        this.height = height;
    }
}
