package it.unipd.dei.es.screenparty.party.host;

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

import it.unipd.dei.es.screenparty.media.MediaOptions;
import it.unipd.dei.es.screenparty.party.PartyCommands;
import it.unipd.dei.es.screenparty.party.PartyEvents;
import it.unipd.dei.es.screenparty.party.PartyManager;
import it.unipd.dei.es.screenparty.party.PartyMessage;
import it.unipd.dei.es.screenparty.party.PartyUtils;

public class PartyHost extends Thread {

    public static final int SERVER_PORT = 1310;
    private static final int MAX_CLIENTS = 2;

    private Handler handler;

    private ServerSocket serverSocket = null;
    private List<ConnectedClient> clients = new ArrayList<>();
    private List<ClientWorker> workers = new ArrayList<>();

    private MediaOptions mediaOptions;
    private float width;
    private float height;

    public PartyHost(Handler handler) {
        this.handler = handler;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void broadcast(PartyMessage message) {
        for(ConnectedClient client : clients)
            PartyUtils.send(message, client, handler);
    }

    public void start(float width, float height) {
        this.width = width;
        this.height = height;
        mediaOptions = new MediaOptions(MediaOptions.Position.CENTER, width, height);
        super.start();
    }

    @Override
    public void interrupt() {
        super.interrupt();
        closeConnections();
    }

    private void closeConnections() {
        try { serverSocket.close(); }
        catch(IOException e) { Log.d(PartyManager.LOG_TAG, e.toString()); }
        for(ClientWorker worker : workers) worker.interrupt();
    }

    @Override
    public void run() {
        try { serverSocket = new ServerSocket(SERVER_PORT); }
        catch(IOException e) {
            handler.obtainMessage(PartyEvents.Host.NOT_STARTED, e.getLocalizedMessage()).sendToTarget();
            return;
        }

        while(true) {
            Socket socket;
            InputStream inputStream;
            PartyMessage request;

            if(clients.size() < MAX_CLIENTS)
                handler.obtainMessage(PartyEvents.Host.WAITING_DEVICES).sendToTarget();

            try {
                socket = serverSocket.accept();
                inputStream = socket.getInputStream();
            } catch(IOException e) {
                Log.d(PartyManager.LOG_TAG, e.toString());
                if(isInterrupted()) return;
                handler.obtainMessage(PartyEvents.CONNECTION_FAILED, e.getLocalizedMessage()).sendToTarget();
                continue;
            }

            Scanner scanner = new Scanner(inputStream);

            try { request = PartyMessage.parseString(scanner.nextLine()); }
            catch(NoSuchElementException e) {
                if(isInterrupted()) return;
                handler.obtainMessage(PartyEvents.JOIN_FAILED, e.getLocalizedMessage()).sendToTarget();
                continue;
            }

            if(request.getCommand().equals(PartyCommands.Client.JOIN)) {
                if(clients.size() < MAX_CLIENTS) {
                    float width = Float.parseFloat(request.getArgument(0));
                    float height = Float.parseFloat(request.getArgument(1));

                    mediaOptions.update(height);
                    MediaOptions.Position position = MediaOptions.Position.values()[2*clients.size()];
                    // TODO: Replace with correct width
                    float frameWidth = mediaOptions.getFrameWidth();
                    float frameHeight = mediaOptions.getFrameHeight();
                    MediaOptions options = new MediaOptions(position, frameWidth, frameHeight);

                    ConnectedClient connectedClient = new ConnectedClient(socket, options, width, height);
                    clients.add(connectedClient);
                    ClientWorker clientWorker = new ClientWorker(connectedClient);
                    clientWorker.start();
                    workers.add(clientWorker);

                    handler.obtainMessage(PartyEvents.Host.CLIENT_JOINED, connectedClient).sendToTarget();

                    if(clients.size() == MAX_CLIENTS) {
                        handler.obtainMessage(PartyEvents.Host.PARTY_READY, mediaOptions).sendToTarget();
                        for(ConnectedClient client : clients) {
                            PartyMessage message = new PartyMessage()
                                    .setCommand(PartyCommands.Host.OK)
                                    .addArgument(String.valueOf(client.getMediaOptions().getPosition()))
                                    .addArgument(String.valueOf(client.getMediaOptions().getFrameWidth()))
                                    .addArgument(String.valueOf(client.getMediaOptions().getFrameHeight()));
                            PartyUtils.send(message, client.getSocket(), handler);
                        }
                    }
                } else {
                    PartyMessage response = new PartyMessage(PartyCommands.Host.FULL);
                    PartyUtils.send(response, socket, handler);
                }
            } else {
                PartyMessage response = new PartyMessage(PartyCommands.Host.UNKNOWN);
                PartyUtils.send(response, socket, handler);
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
            catch(IOException e) { Log.d(PartyManager.LOG_TAG, e.toString()); }
            clients.remove(client);
            workers.remove(this);
        }

        @Override
        public void run() {
            Socket socket = client.getSocket();
            InputStream inputStream;
            PartyMessage message;

            while(true) {
                try {
                    inputStream = socket.getInputStream();
                    Scanner scanner = new Scanner(inputStream);
                    message = PartyMessage.parseString(scanner.nextLine());
                }
                catch(IOException | NoSuchElementException e) {
                    if(!isInterrupted()) {
                        handler.obtainMessage(PartyEvents.Host.CLIENT_LEFT, client).sendToTarget();
                        closeConnection();
                    }
                    return;
                }

                if(message.getCommand().equals(PartyCommands.EXIT)) {
                    handler.obtainMessage(PartyEvents.Host.CLIENT_LEFT, client).sendToTarget();
                    closeConnection();
                    return;
                } else {
                    PartyMessage response = new PartyMessage(PartyCommands.Host.UNKNOWN);
                    PartyUtils.send(response, socket, handler);
                }
            }
        }
    }
}
