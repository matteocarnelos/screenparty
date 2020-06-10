package it.unipd.dei.es.screenparty.media;

import android.graphics.Matrix;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import it.unipd.dei.es.screenparty.party.PartyParams;

/**
 * Class used to modify a media.
 * It uses the matrix's transformation to modify the media.
 */
public class MediaModifier {

    private static final String MEDIA_MODIFIER_TAG = "MEDIA_MODIFIER";

    /**
     * Sets the matrix transformation given the party parameters.
     *
     * @param partyParams Params of the device.
     * @param aspectRatio Aspect ratio of the video.
     * @return Matrix containing the transformation.
     */
    public Matrix prepareScreen(@NotNull PartyParams partyParams, float aspectRatio,float notchBar) {
        Matrix matrix = new Matrix();
        PartyParams.Position position = partyParams.getPosition();
        float screenWidth = partyParams.getScreenParams().getWidth();
        float screenHeight = partyParams.getScreenParams().getHeight();
        float frameHeight = partyParams.getMediaParams().getFrameHeight();
        float frameWidth = partyParams.getMediaParams().getFrameWidth();
        float xDpi = partyParams.getScreenParams().getXdpi();
        float yDpi = partyParams.getScreenParams().getYdpi();

        Log.d(MEDIA_MODIFIER_TAG, "Role: " + partyParams.getPosition());
        Log.d(MEDIA_MODIFIER_TAG, "Screen Height: " + screenHeight);
        Log.d(MEDIA_MODIFIER_TAG, "Screen width: " + screenWidth);
        Log.d(MEDIA_MODIFIER_TAG, "Video height: " + frameHeight);
        Log.d(MEDIA_MODIFIER_TAG, "Video width: " + frameWidth);
        Log.d(MEDIA_MODIFIER_TAG, "Dpi x: " + xDpi);
        Log.d(MEDIA_MODIFIER_TAG, "Dpi y: " + yDpi);
        Log.d(MEDIA_MODIFIER_TAG, "Video aspect ratio: " + aspectRatio);

        switch (position) {
            case LEFT:
                Matrix leftMatrix = new Matrix();
                centerMatrix(frameHeight, yDpi, screenHeight,notchBar, leftMatrix);
                scaleMatrixHorizontally((frameHeight * aspectRatio) / screenWidth, leftMatrix);
                return horizontalMatrixTranslation(((-frameHeight * aspectRatio) + frameWidth) * xDpi, leftMatrix);
            case CENTER:
                Matrix centralMatrix = new Matrix();
                centerMatrix(frameHeight, yDpi, screenHeight,notchBar, centralMatrix);
                scaleMatrixHorizontally((frameHeight * aspectRatio) / screenWidth, centralMatrix);
                return horizontalMatrixTranslation(-xDpi * (frameHeight * aspectRatio - screenWidth) / 2, centralMatrix);
            case RIGHT:
                Matrix rightMatrix = new Matrix();
                centerMatrix(frameHeight, yDpi, screenHeight,notchBar, rightMatrix);
                scaleMatrixHorizontally((frameHeight * aspectRatio) / screenWidth, rightMatrix);
                return horizontalMatrixTranslation((screenWidth - frameWidth) * xDpi, rightMatrix);
        }
        return matrix;
    }

    /**
     * Scales and translates the matrix keeping the media centered to the screen.
     *
     * @param frameHeight  Number of inches to be shown.
     * @param screenYDpi   The y density pixel of the device's screen.
     * @param screenHeight Number of inches of the screen's height.
     * @return Matrix containing the transformation.
     */
    public Matrix centerMatrix(float frameHeight, float screenYDpi, float screenHeight,float notchBar, Matrix matrix) {
        float yScalingRate = frameHeight / screenHeight;
        float yTranslation = (screenYDpi * (screenHeight - frameHeight-notchBar) /2);
        scaleMatrixVertically(yScalingRate, matrix);
        verticalMatrixTranslation(yTranslation, matrix);
        Log.d(MEDIA_MODIFIER_TAG, "Scaling height: " + frameHeight / screenHeight);
        Log.d(MEDIA_MODIFIER_TAG, "Translation Height: " + ((screenYDpi * (screenHeight - frameHeight) /2)-notchBar));
        return matrix;
    }


    /**
     * Scales the matrix horizontally scaleX times.
     *
     * @param scaleX The time to multiply the width of the video.
     * @return Matrix containing the transformation.
     */
    public Matrix scaleMatrixHorizontally(float scaleX, @NotNull Matrix matrix) {
        matrix.preScale(scaleX, 1);
        return matrix;
    }

    /**
     * Scales the matrix vertically scaleY times.
     *
     * @param scaleY Number of times to scale the matrix.
     * @return Matrix containing the transformation.
     */
    public Matrix scaleMatrixVertically(float scaleY, @NotNull Matrix matrix) {
        matrix.preScale(1, scaleY);
        return matrix;
    }

    /**
     * Translates the matrix vertically of pyTranslation pixels.
     *
     * @param pyTranslation Number of pixel for the translation.
     * @param matrix        Matrix to be translated.
     * @return Matrix containing the transformation.
     */
    public Matrix verticalMatrixTranslation(float pyTranslation, @NotNull Matrix matrix) {
        // Use "-" to make the translation from bottom to top.
        matrix.postTranslate(0, pyTranslation);
        return matrix;
    }

    /**
     * Translates the matrix horizontally of pxTranslation pixels.
     *
     * @param pxTranslation Number of pixel for the translation
     * @param matrix        Matrix to be translated.
     * @return Matrix containing the transformation.
     */
    public Matrix horizontalMatrixTranslation(float pxTranslation, @NotNull Matrix matrix) {
        // Use -pxTranslation to translate from left to right
        matrix.postTranslate(pxTranslation, 0);
        return matrix;
    }
}
