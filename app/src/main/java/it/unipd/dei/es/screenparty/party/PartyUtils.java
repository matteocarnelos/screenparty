package it.unipd.dei.es.screenparty.party;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PartyUtils {

    public static void computeFrameDimensions(PartyParams host, List<? extends PartyParams> clients) {
        // TODO: Check cases (if sum of widths < computed video width -> reverse calculation)
        List<Float> heights = new ArrayList<>();
        heights.add(host.getScreenHeight());
        for(PartyParams clientParams : clients) heights.add(clientParams.getScreenHeight());
        float videoHeight = Collections.min(heights);
        host.getMediaParams().setFrameHeight(videoHeight);
        for(PartyParams clientParams : clients) clientParams.getMediaParams().setFrameHeight(videoHeight);

        float videoAspectRatio = PartyManager.getInstance().getPartyParams().getMediaParams().getAspectRatio();

        float videoWidth = videoAspectRatio * videoHeight;
        host.getMediaParams().setFrameWidth(host.getScreenWidth());
        for(PartyParams clientParams : clients)
            clientParams.getMediaParams().setFrameWidth((videoWidth - host.getMediaParams().getFrameWidth()) / 2);
    }
}
