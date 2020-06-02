package it.unipd.dei.es.screenparty.network;

import android.os.Handler;

import java.io.IOException;
import java.net.Socket;

public class NetworkUtils {

    public static void send(NetworkMessage message, Socket socket, Handler handler) {
        try { socket.getOutputStream().write(message.toString().getBytes()); }
        catch(IOException e) { handler.obtainMessage(NetworkEvents.COMMUNICATION_FAILED, e); }
    }
}
