package it.unipd.dei.es.screenparty.media;

import android.graphics.Matrix;
import android.util.Log;

import it.unipd.dei.es.screenparty.party.PartyParams;

/**
 * Class made to modify the content of a TextureView.
 */
public class MediaModifier {
    private final String MEDIA_MODIFIER_TAG = "MEDIA_MODIFIER";

    public Matrix prepareScreen(PartyParams partyParams, float aspectRatio) {
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
//        scaleCenteredTextureHeight(frameHeight, yDpi, screenHeight,matrix);
        switch (position) {
            case LEFT:
                Matrix leftMatrix = new Matrix();
                scaleCenteredTextureHeight(frameHeight, yDpi, screenHeight, leftMatrix);
                scaleTextureWidth((frameHeight * aspectRatio) / screenWidth, leftMatrix);
                return xTranslateTexture((screenWidth - frameWidth) * xDpi, leftMatrix);
            case CENTER:
                Matrix centralMatrix = new Matrix();
                scaleCenteredTextureHeight(frameHeight, yDpi, screenHeight, centralMatrix);
                scaleTextureWidth((frameHeight * aspectRatio) / screenWidth, centralMatrix);
                return xTranslateTexture(-xDpi * (frameHeight * aspectRatio - screenWidth) / 2, centralMatrix);
            case RIGHT:
                Matrix rightMatrix = new Matrix();
                scaleCenteredTextureHeight(frameHeight, yDpi, screenHeight, rightMatrix);
                scaleTextureWidth((frameHeight * aspectRatio) / screenWidth, rightMatrix);
                Log.d(MEDIA_MODIFIER_TAG, "translation: " + ((-frameHeight * aspectRatio) + frameWidth));
                return xTranslateTexture(((-frameHeight * aspectRatio) + frameWidth) * xDpi, rightMatrix);
        }
        return matrix;
    }


    /**
     * @param frameHeight  Number of inches to be shown.
     * @param screenYDpi   The yDpi of the device's screen.
     * @param screenHeight Number of inches of the screen's height.
     * @return Matrix.
     */
    public Matrix scaleCenteredTextureHeight(float frameHeight, float screenYDpi, float screenHeight, Matrix matrix) {
        float yScalingRate = frameHeight / screenHeight;
        float yTranslation = screenYDpi * (screenHeight - frameHeight) / 2;
        scaleTextureHeight(yScalingRate, matrix);
        yTranslateTexture(yTranslation, matrix);
        Log.d(MEDIA_MODIFIER_TAG, "Scaling height: " + frameHeight / screenHeight);
        Log.d(MEDIA_MODIFIER_TAG, "Translation Height: " + (screenYDpi * (screenHeight - frameHeight) / 2));
        return matrix;
    }

    /**
     * Scales the width of the content of the TextureView.
     *
     * @param scaleX The time to multiply the width of the video.
     * @return The modified Matrix.
     */
    public Matrix scaleTextureWidth(float scaleX, Matrix matrix) {
        matrix.preScale(scaleX, 1);
        return matrix;
    }

    /**
     * Scales the height of the content of the TextureView.
     *
     * @param scaleY The time to multiply the width of the video.
     * @return The modified Matrix.
     */
    public Matrix scaleTextureHeight(float scaleY, Matrix matrix) {
        matrix.preScale(1, scaleY);
        return matrix;
    }


    public Matrix yTranslateTexture(float pyTranslation, Matrix matrix) {
        //The "-" it's used because it make the translation from bottom to top.
        matrix.postTranslate(0, pyTranslation);
        return matrix;
    }

    public Matrix xTranslateTexture(float pxTranslation, Matrix matrix) {
        //Use -pxTranslation to translate from left to right
        matrix.postTranslate(pxTranslation, 0);
        return matrix;
    }
}