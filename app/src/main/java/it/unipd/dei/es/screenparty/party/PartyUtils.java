package it.unipd.dei.es.screenparty.party;

import android.os.Handler;

import java.io.IOException;
import java.net.Socket;

public class PartyUtils {

    public static void send(PartyMessage message, PartyPeer peer, Handler handler) {
        send(message, peer.getSocket(), handler);
    }

    public static void send(PartyMessage message, Socket socket, Handler handler) {
        try { socket.getOutputStream().write(message.toString().getBytes()); }
        catch(IOException e) { handler.obtainMessage(PartyEvents.COMMUNICATION_FAILED, e); }
    }
}
