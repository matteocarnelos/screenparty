package it.unipd.dei.es.screenparty.party;

import java.net.Socket;

public class PartyPeer {

    private Socket socket;

    public PartyPeer(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }
}
