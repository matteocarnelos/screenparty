package it.unipd.dei.es.screenparty.party.client;

import java.net.Socket;

import it.unipd.dei.es.screenparty.party.PartyPeer;

public class ConnectedHost extends PartyPeer {

    public ConnectedHost(Socket socket) {
        super(socket);
    }
}
