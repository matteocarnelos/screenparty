package it.unipd.dei.es.screenparty.network;

import java.net.Socket;

import it.unipd.dei.es.screenparty.party.PartyParams;
import it.unipd.dei.es.screenparty.party.ScreenParams;

public class ConnectedClient extends PartyParams {

    private Socket socket;

    ConnectedClient(Socket socket, PartyParams.Position position, float width, float height) {
        super(new ScreenParams(width, height));
        setPosition(position);
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }
}
