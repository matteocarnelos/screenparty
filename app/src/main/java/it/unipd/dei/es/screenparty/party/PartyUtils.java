package it.unipd.dei.es.screenparty.party;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PartyUtils {

    public static void computeFrameDimensions(PartyParams hostParam, List<? extends PartyParams> clientParams) {
        // TODO: Check cases (if sum of widths < computed video width -> reverse calculation)
        List<Float> heights = new ArrayList<>();
        heights.add(hostParam.getScreenParams().getHeight());
        for(PartyParams clientParam : clientParams) heights.add(clientParam.getScreenParams().getHeight());
        float videoHeight = Collections.min(heights);
        hostParam.getMediaParams().setFrameHeight(videoHeight);
        for(PartyParams clientParam : clientParams) clientParam.getMediaParams().setFrameHeight(videoHeight);

        float videoAspectRatio = PartyManager.getInstance().getPartyParams().getMediaParams().getAspectRatio();

        float videoWidth = videoAspectRatio * videoHeight;
        hostParam.getMediaParams().setFrameWidth(hostParam.getScreenParams().getWidth());
        for(PartyParams clientParam : clientParams)
            clientParam.getMediaParams().setFrameWidth((videoWidth - hostParam.getMediaParams().getFrameWidth()) / 2);
    }
}
