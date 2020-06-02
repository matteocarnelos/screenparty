package it.unipd.dei.es.screenparty.network;

import java.net.Socket;

import it.unipd.dei.es.screenparty.party.PartyParams;

public class ConnectedClient extends PartyParams {

    private Socket socket;

    ConnectedClient(Socket socket, PartyParams.Position position, float width, float height) {
        setPosition(position);
        setScreenWidth(width);
        setScreenHeight(height);
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }
}
