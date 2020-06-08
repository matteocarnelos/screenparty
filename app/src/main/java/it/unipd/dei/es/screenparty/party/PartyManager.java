package it.unipd.dei.es.screenparty.party;

import android.content.ContentResolver;
import android.os.Handler;

import java.io.File;

import it.unipd.dei.es.screenparty.network.NetworkClient;
import it.unipd.dei.es.screenparty.network.NetworkHost;
import it.unipd.dei.es.screenparty.network.NetworkMessage;

// TODO: Add streaming server

public class PartyManager {

    public static final String LOG_TAG = "SCREENPARTY";

    private static PartyManager instance = null;

    private Handler handler;
    private ContentResolver contentResolver;
    private File filesDir;

    private NetworkHost host;
    private NetworkClient client;

    private PartyParams partyParams;

    private PartyManager() { }

    public static PartyManager getInstance() {
        if(instance == null) instance = new PartyManager();
        return instance;
    }

    public void setEventsHandler(Handler handler) {
        this.handler = handler;
    }

    public PartyParams getPartyParams() {
        return partyParams;
    }

    public void init(ScreenParams screenParams, ContentResolver contentResolver, File filesDir) {
        partyParams = new PartyParams(screenParams);
        this.contentResolver = contentResolver;
        this.filesDir = filesDir;
    }

    public void startAsHost() {
        if(client != null && client.isAlive()) client.interrupt();
        if(host != null && host.isAlive()) return;
        partyParams.setRole(PartyParams.Role.HOST);
        host = new NetworkHost(handler, contentResolver);
        host.start();
    }

    public void startAsClient(String hostIp) {
        if(host != null && host.isAlive()) host.interrupt();
        if(client != null && client.isAlive()) return;
        partyParams.setRole(PartyParams.Role.CLIENT);
        client = new NetworkClient(handler, filesDir);
        client.start(hostIp);
    }

    public void restart() {
        if(partyParams.getRole() == PartyParams.Role.HOST) {
            host.interrupt();
            startAsHost();
        } else {
            client.interrupt();
            startAsClient(client.getHostIp());
        }
    }

    public void stop() {
        if(partyParams.getRole() == PartyParams.Role.HOST && host != null)
            host.interrupt();
        else if(partyParams.getRole() == PartyParams.Role.CLIENT && client != null)
            client.interrupt();
    }

    public void sendMessage(NetworkMessage message) {
        if(partyParams.getRole() == PartyParams.Role.HOST) host.broadcast(message);
        else client.send(message);
    }
}
