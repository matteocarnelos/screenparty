package it.unipd.dei.es.screenparty.party.host;

import java.net.Socket;

import it.unipd.dei.es.screenparty.media.MediaOptions;
import it.unipd.dei.es.screenparty.party.PartyPeer;

public class ConnectedClient extends PartyPeer {

    private MediaOptions mediaOptions;
    private float width;
    private float height;

    public ConnectedClient(Socket socket, MediaOptions mediaOptions, float width, float height) {
        super(socket);
        this.mediaOptions = mediaOptions;
        this.width = width;
        this.height = height;
    }

    public MediaOptions getMediaOptions() {
        return mediaOptions;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
