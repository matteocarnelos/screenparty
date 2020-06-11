package it.unipd.dei.es.screenparty.party;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Rect;
import android.provider.Settings;
import android.util.DisplayMetrics;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PartyUtils {

    private static final String UNKNOWN_DEVICE_NAME = "Unknown";

    public static void computeFrameDimensions(PartyParams hostParam, @NotNull List<? extends PartyParams> clientsParams) {
        float clientsTotalWidth = 0;
        for (PartyParams clientParams : clientsParams)
            clientsTotalWidth += clientParams.getScreenParams().getWidth();
        List<Float> heights = new ArrayList<>();
        heights.add(hostParam.getScreenParams().getHeight());
        for (PartyParams clientParams : clientsParams) heights.add(clientParams.getScreenParams().getHeight());
        float videoHeight = Collections.min(heights);
        hostParam.getMediaParams().setFrameHeight(videoHeight);

        for(PartyParams clientParams : clientsParams)
            clientParams.getMediaParams().setFrameHeight(videoHeight);

        float videoAspectRatio = PartyManager.getInstance().getPartyParams().getMediaParams().getAspectRatio();

        float videoWidth = videoAspectRatio * videoHeight;

        // Invoked when the calculated width of the video is within the width of the 3 screens, and the host's (central) screen
        if(videoWidth < hostParam.getScreenParams().getWidth() + clientsTotalWidth && videoHeight > clientsTotalWidth) {
            hostParam.getMediaParams().setFrameWidth(hostParam.getScreenParams().getWidth());
            for(PartyParams clientParams : clientsParams)
                clientParams.getMediaParams().setFrameWidth((videoWidth - hostParam.getMediaParams().getFrameWidth()) / 2);
        // Invoked when the calculated width it's lower than the host's (central's) screen
        } else if(videoWidth < hostParam.getScreenParams().getWidth()) {
            hostParam.getMediaParams().setFrameWidth(hostParam.getScreenParams().getWidth());
            hostParam.getMediaParams().setFrameHeight(hostParam.getScreenParams().getWidth()/videoAspectRatio);
            for (PartyParams clientParams : clientsParams)
                clientParams.getMediaParams().setFrameWidth(0);
        }
        // Invoked when the calculated width of the video it's greater than width of the 3 screens
        else if(videoWidth > hostParam.getScreenParams().getWidth() + clientsTotalWidth) {
            videoWidth = hostParam.getScreenParams().getWidth() + clientsTotalWidth;
            videoHeight = videoWidth / videoAspectRatio;
            hostParam.getMediaParams().setFrameHeight(videoHeight);
            hostParam.getMediaParams().setFrameWidth(hostParam.getScreenParams().getWidth());
            for(PartyParams clientParams : clientsParams) {
                clientParams.getMediaParams().setFrameHeight(videoHeight);
                clientParams.getMediaParams().setFrameWidth(clientParams.getScreenParams().getWidth());
            }
        }
    }

    /**
     * Get the name of the device. This method returns the name that the user has given to the device.
     * @param contentResolver The {@link ContentResolver} needed for accessing device information.
     * @return A string containing the name of the device.
     */
    public static String getDeviceName(ContentResolver contentResolver) {
        String deviceName = Settings.Secure.getString(contentResolver, "bluetooth_name");
        if(deviceName == null) return UNKNOWN_DEVICE_NAME;
        return deviceName;
    }

    /**
     * Get the navigation bar height in pixel of the device.
     * @param activity The {@link Activity} used for accessing device information.
     * @return The height in pixels.
     */
    public static int getNavigationBarHeightPixels(@NotNull Activity activity) {
        Rect rectangle = new Rect();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics.heightPixels - (rectangle.top + rectangle.height());
    }
}
