package it.unipd.dei.es.screenparty.network;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import it.unipd.dei.es.screenparty.party.PartyManager;
import it.unipd.dei.es.screenparty.party.PartyParams;
import it.unipd.dei.es.screenparty.party.PartyUtils;

public class NetworkHost extends Thread {

    private static final String NETWORK_HOST_TAG = "NETWORK_HOST";

    public static final int SERVER_PORT = 1310;
    private static final int MAX_CLIENTS = 2;

    private PartyManager partyManager = PartyManager.getInstance();
    private Handler handler;

    private ServerSocket serverSocket = null;
    private List<ConnectedClient> clients = new ArrayList<>();
    private List<ClientWorker> workers = new ArrayList<>();

    public NetworkHost(Handler handler) {
        this.handler = handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void broadcast(NetworkMessage message) {
        for(ConnectedClient client : clients)
            NetworkUtils.send(message, client.getSocket(), handler);
    }

    private void closeConnections() {
        try { serverSocket.close(); }
        catch(IOException e) { Log.w(NETWORK_HOST_TAG, e.toString()); }
        for(ClientWorker worker : workers) worker.interrupt();
        workers.clear();
    }

    @Override
    public void interrupt() {
        super.interrupt();
        closeConnections();
    }

    @Override
    public void run() {
        try { serverSocket = new ServerSocket(SERVER_PORT); }
        catch(IOException e) {
            handler.obtainMessage(NetworkEvents.Host.NOT_STARTED, e.getLocalizedMessage()).sendToTarget();
            return;
        }

        while(true) {
            Socket socket;
            InputStream inputStream;
            NetworkMessage request;

            if(clients.size() < MAX_CLIENTS) {
                String ip = NetworkUtils.getIPAddress(true);
                if(ip.equals(NetworkUtils.INVALID_IP)) ip = null;
                handler.obtainMessage(NetworkEvents.Host.WAITING_DEVICES, ip).sendToTarget();
            }

            try {
                socket = serverSocket.accept();
                inputStream = socket.getInputStream();
            } catch(IOException e) {
                if(isInterrupted()) return;
                handler.obtainMessage(NetworkEvents.CONNECTION_FAILED, e.getLocalizedMessage()).sendToTarget();
                continue;
            }

            Scanner scanner = new Scanner(inputStream);

            try { request = NetworkMessage.parseString(scanner.nextLine()); }
            catch(NoSuchElementException e) {
                if(isInterrupted()) return;
                handler.obtainMessage(NetworkEvents.JOIN_FAILED, e.getLocalizedMessage()).sendToTarget();
                continue;
            }

            if(request.getCommand().equals(NetworkCommands.Client.JOIN)) {
                if(clients.size() < MAX_CLIENTS) {
                    float width = Float.parseFloat(request.getArgument(0));
                    float height = Float.parseFloat(request.getArgument(1));
                    String deviceName = NetworkUtils.decodeDeviceName(request.getArgument(2));

                    PartyParams.Position position = PartyParams.Position.values()[2*clients.size()];
                    ConnectedClient connectedClient = new ConnectedClient(socket, position, width, height);
                    connectedClient.setDeviceName(deviceName);

                    clients.add(connectedClient);
                    ClientWorker clientWorker = new ClientWorker(connectedClient);
                    clientWorker.start();
                    workers.add(clientWorker);

                    handler.obtainMessage(NetworkEvents.Host.CLIENT_JOINED, clients).sendToTarget();

                    if(clients.size() == MAX_CLIENTS) {
                        PartyUtils.computeFrameDimensions(partyManager.getPartyParams(), clients);
                        for(ConnectedClient client : clients) {
                            NetworkMessage message = new NetworkMessage.Builder()
                                    .setCommand(NetworkCommands.Host.OK)
                                    .addArgument(String.valueOf(client.getPosition()))
                                    .addArgument(String.valueOf(client.getMediaParams().getFrameWidth()))
                                    .addArgument(String.valueOf(client.getMediaParams().getFrameHeight()))
                                    .build();
                            NetworkUtils.send(message, client.getSocket(), handler);
                        }
                        handler.obtainMessage(NetworkEvents.Host.PARTY_READY, clients).sendToTarget();
                    }
                } else {
                    NetworkMessage response = new NetworkMessage(NetworkCommands.Host.FULL);
                    NetworkUtils.send(response, socket, handler);
                }
            } else {
                NetworkMessage response = new NetworkMessage(NetworkCommands.Host.UNKNOWN);
                NetworkUtils.send(response, socket, handler);
            }
        }
    }

    private class ClientWorker extends Thread {

        ConnectedClient client;

        ClientWorker(ConnectedClient client) {
            this.client = client;
        }

        @Override
        public void interrupt() {
            super.interrupt();
            closeConnection();
        }

        private void closeConnection() {
            try { client.getSocket().close(); }
            catch(IOException e) { Log.w(NETWORK_HOST_TAG, e.toString()); }
            clients.remove(client);
        }

        @Override
        public void run() {
            Socket socket = client.getSocket();
            InputStream inputStream;
            NetworkMessage message;

            while(true) {
                try {
                    inputStream = socket.getInputStream();
                    Scanner scanner = new Scanner(inputStream);
                    message = NetworkMessage.parseString(scanner.nextLine());
                }
                catch(IOException | NoSuchElementException e) {
                    if(!isInterrupted()) {
                        closeConnections();
                        handler.obtainMessage(NetworkEvents.Host.CLIENT_LEFT, clients).sendToTarget();
                    }
                    return;
                }

                if(message.getCommand().equals(NetworkCommands.Client.READY)) {
                    client.setReady(true);
                    boolean allReady = true;
                    for(ClientWorker worker : workers) allReady &= worker.client.isReady();
                    if(allReady)broadcast(new NetworkMessage(NetworkCommands.Host.PLAY));
                    handler.obtainMessage(NetworkEvents.Client.HOST_PLAY).sendToTarget();
                }
                NetworkMessage response = new NetworkMessage(NetworkCommands.Host.UNKNOWN);
                NetworkUtils.send(response, socket, handler);
            }
        }
    }
}
