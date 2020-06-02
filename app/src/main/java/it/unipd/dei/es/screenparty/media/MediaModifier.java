package it.unipd.dei.es.screenparty.media;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.util.DisplayMetrics;

/**
 * Class made to modify the content of a TextureView.
 */
public class MediaModifier {
    private Matrix matrix = new Matrix();
    /**
     * Scales the width of the content of the TextureView.
     *
     * @param scaleX The time to multiply the width of the video.
     * @return The modified Matrix.
     */
    public Matrix scaleTexture(int scaleX) {
        matrix.preScale(scaleX, 1);
        return matrix;
    }

    /**
     * Translate the content on dx  the Texture view of pxTranslation pixels.
     *
     * @param pxTranslation The number of pixel to translate  the content of the texture view of(on dx).
     * @return The modified Matrix.
     */
    public Matrix translateTexture(int pxTranslation){
        //The "-" it's used because it make the translation to the right side and not the left.
        matrix.postTranslate(-pxTranslation,0);
        return matrix;
    }
    /**
     * Returns the width of the device's screen.
     *
     * @return The width of the device's screen in pixel.
     */
    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;

    }

    /**
     * Returns the height in pixel of the device's screen,included the height of navigation bar.
     *
     * @param activity The activity to get the height of the navigation bar calculated.
     * @return The height of the device's screen.
     */
    public static int getScreenHeight(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        return metrics.heightPixels;
    }

    /**
     * Returns the width of the device's screen in inches.
     *
     * @return The width of the device's screen in inches.
     */
    public static float getRealScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels/Resources.getSystem().getDisplayMetrics().xdpi;

    }

    /**
     * Returns the width of the device's screen in inches.
     *
     * @return The width of the device's screen in inches.
     */
    public static float getRealScreenHeight(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        return metrics.heightPixels/Resources.getSystem().getDisplayMetrics().ydpi;

    }

}