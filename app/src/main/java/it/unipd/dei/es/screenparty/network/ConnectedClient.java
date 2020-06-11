package it.unipd.dei.es.screenparty.network;

import java.net.Socket;

import it.unipd.dei.es.screenparty.party.PartyParams;
import it.unipd.dei.es.screenparty.party.ScreenParams;

/**
 * Class that represent a connected client seen from the host. Every connected client is basically
 * an entity with {@link PartyParams}, a {@link Socket} and a ready state.
 */
public class ConnectedClient extends PartyParams {

    private Socket socket;
    private boolean ready = false;

    /**
     * Create a new {@link ConnectedClient}.
     * @param socket The socket on which the client is connected.
     * @param position The {@link PartyParams} position associated with the client.
     * @param width The width of the screen in inches.
     * @param height The height of the screen in inches.
     */
    ConnectedClient(Socket socket, PartyParams.Position position, float width, float height) {
        super(new ScreenParams(width, height));
        setPosition(position);
        this.socket = socket;
    }

    /**
     * Get the socket on which the client is connected to.
     * @return The Socket object.
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Tell if a client is ready for playing the media.
     * @return True if the client is ready, false otherwise.
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * Set the ready state of the client.
     * @param ready A boolean that indicates the state.
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
