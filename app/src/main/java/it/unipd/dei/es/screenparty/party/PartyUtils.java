package it.unipd.dei.es.screenparty.party;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PartyUtils {

    public static void computeFrameDimensions(PartyParams hostParam, List<? extends PartyParams> clientsParams) {
        float clientsTotalWidth = 0;
        for (PartyParams clientParams : clientsParams)
            clientsTotalWidth += clientParams.getScreenParams().getWidth();
        List<Float> heights = new ArrayList<>();
        heights.add(hostParam.getScreenParams().getHeight());
        for (PartyParams clientParams : clientsParams) heights.add(clientParams.getScreenParams().getHeight());
        float videoHeight = Collections.min(heights);
        hostParam.getMediaParams().setFrameHeight(videoHeight);

        for (PartyParams clientParams : clientsParams)
            clientParams.getMediaParams().setFrameHeight(videoHeight);

        float videoAspectRatio = PartyManager.getInstance().getPartyParams().getMediaParams().getAspectRatio();

        float videoWidth = videoAspectRatio * videoHeight;

        //Invoked when the calculated width of the video is within the width of the 3 screens and the host's (central) screen.
        if (videoWidth < hostParam.getScreenParams().getWidth() + clientsTotalWidth && videoHeight > clientsTotalWidth) {
            hostParam.getMediaParams().setFrameWidth(hostParam.getScreenParams().getWidth());
            for (PartyParams clientParams : clientsParams)
                clientParams.getMediaParams().setFrameWidth((videoWidth - hostParam.getMediaParams().getFrameWidth()) / 2);
        //Invoked when the calculated width it's lower than the host's (central's) screen.
        } else if (videoWidth < hostParam.getScreenParams().getWidth()) {
            hostParam.getMediaParams().setFrameWidth(hostParam.getScreenParams().getWidth());
            hostParam.getMediaParams().setFrameHeight(hostParam.getScreenParams().getWidth()/videoAspectRatio);
            for (PartyParams clientParams : clientsParams)
                clientParams.getMediaParams().setFrameWidth(0);
        }
        //Invoked when the calculated width of the video it's greater than width of the 3 screens.
        else if (videoWidth > hostParam.getScreenParams().getWidth() + clientsTotalWidth) {
            videoWidth = hostParam.getScreenParams().getWidth() + clientsTotalWidth;
            videoHeight = videoWidth / videoAspectRatio;
            hostParam.getMediaParams().setFrameHeight(videoHeight);
            hostParam.getMediaParams().setFrameWidth(hostParam.getScreenParams().getWidth());
            for (PartyParams clientParams : clientsParams) {
                clientParams.getMediaParams().setFrameHeight(videoHeight);
                clientParams.getMediaParams().setFrameWidth(clientParams.getScreenParams().getWidth());
            }
        }
    }
}
