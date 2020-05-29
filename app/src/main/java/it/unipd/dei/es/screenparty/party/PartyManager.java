package it.unipd.dei.es.screenparty.party;

import android.os.Handler;

import it.unipd.dei.es.screenparty.party.client.PartyClient;
import it.unipd.dei.es.screenparty.party.host.PartyHost;

// TODO: Add streaming server

public class PartyManager {

    public static final String LOG_TAG = "SCREENPARTY_NET";

    private enum Role { HOST, CLIENT }

    private static PartyManager instance = null;
    private Handler handler = null;

    private Role role;
    private PartyHost host = null;
    private PartyClient client = null;

    private PartyManager() { }

    public static PartyManager getInstance() {
        if(instance == null) return new PartyManager();
        else return instance;
    }

    public void setEventsHandler(Handler handler) {
        this.handler = handler;
    }

    public void startAsHost(float width, float height) {
        if(client != null && client.isAlive()) client.interrupt();
        if(host != null && host.isAlive()) return;
        host = new PartyHost(handler);
        host.start(width, height);
        role = Role.HOST;
    }

    public void startAsClient(String serverIp, float width, float height) {
        if(host != null && host.isAlive()) host.interrupt();
        if(client != null && client.isAlive()) return;
        client = new PartyClient(handler);
        client.start(serverIp, width, height);
        role = Role.CLIENT;
    }

    public void restart() {
        if(role == Role.HOST) {
            host.interrupt();
            float width = host.getWidth();
            float height = host.getHeight();
            host = new PartyHost(handler);
            host.start(width, height);
        } else {
            client.interrupt();
            String serverIp = client.getHostIp();
            float width = client.getWidth();
            float height = client.getHeight();
            client = new PartyClient(handler);
            client.start(serverIp, width, height);
        }
    }

    public void stop() {
        if(role == Role.HOST) host.interrupt();
        else client.interrupt();
    }

    public void sendMessage(PartyMessage message) {
        if(role == Role.HOST) host.broadcast(message);
        else client.send(message);
    }

}
