package it.unipd.dei.es.screenparty.party;

import android.os.Handler;

import it.unipd.dei.es.screenparty.network.NetworkClient;
import it.unipd.dei.es.screenparty.network.NetworkHost;
import it.unipd.dei.es.screenparty.network.NetworkMessage;

/**
 * Class for the general management of parties. This class follows the Singleton Design Pattern.
 */
public class PartyManager {

    private static PartyManager instance = null;

    private Handler handler;

    private NetworkHost host;
    private NetworkClient client;
    private PartyParams partyParams;

    private boolean partyReady = false;

    /**
     * Empty private constructor needed for the design pattern.
     */
    private PartyManager() { }

    /**
     * Get the unique instance of the class.
     * @return The {@link PartyManager} instance.
     */
    public static PartyManager getInstance() {
        if(instance == null) instance = new PartyManager();
        return instance;
    }

    /**
     * Initialize the manager, this method must be called before any operation.
     * @param screenParams The {@link ScreenParams} object associated with the current device.
     */
    public void init(ScreenParams screenParams) {
        partyParams = new PartyParams(screenParams);
    }

    /**
     * Tell if the party is ready. A party is ready when all the clients are connected.
     * @return True if the party is ready, false otherwise.
     */
    public boolean isPartyReady() {
        return partyReady;
    }

    /**
     * Get the parameters of the party
     * @return The parameters as a {@link PartyParams} object.
     */
    public PartyParams getPartyParams() {
        return partyParams;
    }

    /**
     * Set the event handler for party events.
     * @param handler The {@link Handler} object.
     */
    public void setEventsHandler(Handler handler) {
        this.handler = handler;
        if(host != null) host.setHandler(handler);
        if(client != null) client.setHandler(handler);
    }

    /**
     * Set the ready state of the party.
     * @param partyReady The state as a boolean.
     */
    public void setPartyReady(boolean partyReady) {
        this.partyReady = partyReady;
    }

    /**
     * Start the manager as the host device.
     */
    public void startAsHost() {
        if(client != null && client.isAlive()) client.interrupt();
        if(host != null && host.isAlive()) return;
        partyParams.setRole(PartyParams.Role.HOST);
        host = new NetworkHost(handler);
        host.start();
    }

    /**
     * Start the manager as the client device.
     * @param hostIp The IP address of the host.
     */
    public void startAsClient(String hostIp) {
        if(host != null && host.isAlive()) host.interrupt();
        if(client != null && client.isAlive()) return;
        partyParams.setRole(PartyParams.Role.CLIENT);
        client = new NetworkClient(handler);
        client.start(hostIp);
    }

    /**
     * Restart the party manager.
     */
    public void restart() {
        if(partyParams.getRole() == PartyParams.Role.HOST) {
            host.interrupt();
            try { host.join(); }
            catch(InterruptedException ignored) { return; }
            startAsHost();
        }
        else {
            client.interrupt();
            try { client.join(); }
            catch(InterruptedException ignored) { return; }
            startAsClient(client.getHostIp());
        }
    }

    /**
     * Stop the party manager.
     */
    public void stop() {
        if(partyParams.getRole() == PartyParams.Role.HOST && host != null)
            host.interrupt();
        else if(partyParams.getRole() == PartyParams.Role.CLIENT && client != null)
            client.interrupt();
    }

    /**
     * Send a {@link NetworkMessage} to the host or the clients depending on the role.
     * @param message The {@link NetworkMessage} to send.
     */
    public void sendMessage(NetworkMessage message) {
        if(partyParams.getRole() == PartyParams.Role.HOST) host.broadcast(message);
        else client.send(message);
    }
}
