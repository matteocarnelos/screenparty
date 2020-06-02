package it.unipd.dei.es.screenparty.network;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import it.unipd.dei.es.screenparty.party.PartyManager;
import it.unipd.dei.es.screenparty.party.PartyParams;

public class NetworkClient extends Thread {

    private PartyManager partyManager = PartyManager.getInstance();
    private Handler handler;

    private Socket host;
    private String hostIp;

    public NetworkClient(Handler handler) {
        this.handler = handler;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void send(NetworkMessage message) {
        NetworkUtils.send(message, host, handler);
    }

    public void start(String hostIp) {
        this.hostIp = hostIp;
        super.start();
    }

    @Override
    public void interrupt() {
        super.interrupt();
        closeConnection();
    }

    private void closeConnection() {
        try { if(host != null) host.close(); }
        catch(IOException e) { Log.d(PartyManager.LOG_TAG, e.toString()); }
    }

    @Override
    public void run() {
        InputStream inputStream;
        NetworkMessage response;

        try {
            host = new Socket(hostIp, NetworkHost.SERVER_PORT);
            inputStream = host.getInputStream();
        } catch(IOException e) {
            if(!isInterrupted()) {
                handler.obtainMessage(NetworkEvents.JOIN_FAILED, e.getLocalizedMessage()).sendToTarget();
                closeConnection();
            }
            return;
        }

        NetworkMessage request = new NetworkMessage.Builder()
                .setCommand(NetworkCommands.Client.JOIN)
                .addArgument(String.valueOf(partyManager.getPartyParams().getScreenParams().getWidth()))
                .addArgument(String.valueOf(partyManager.getPartyParams().getScreenParams().getHeight()))
                .build();
        NetworkUtils.send(request, host, handler);

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try { response = NetworkMessage.parseString(reader.readLine()); }
        catch(IOException e) {
            if(!isInterrupted()) {
                handler.obtainMessage(NetworkEvents.JOIN_FAILED, e.getLocalizedMessage()).sendToTarget();
                closeConnection();
            }
            return;
        }

        if(response.getCommand().equals(NetworkCommands.Host.OK)) {
            PartyParams.Position position = PartyParams.Position.valueOf(request.getArgument(0));
            float frameWidth = Float.parseFloat(response.getArgument(1));
            float frameHeight = Float.parseFloat(response.getArgument(2));

            partyManager.getPartyParams().setPosition(position);
            partyManager.getPartyParams().getMediaParams().setFrameWidth(frameWidth);
            partyManager.getPartyParams().getMediaParams().setFrameHeight(frameHeight);

            handler.obtainMessage(NetworkEvents.Client.PARTY_JOINED).sendToTarget();
        } else {
            handler.obtainMessage(NetworkEvents.Client.PARTY_FULL).sendToTarget();
            return;
        }

        while(true) {
            NetworkMessage message;
            try { message = NetworkMessage.parseString(reader.readLine()); }
            catch(IOException e) {
                if(!isInterrupted()) {
                    handler.obtainMessage(NetworkEvents.Client.HOST_LEFT).sendToTarget();
                    closeConnection();
                }
                return;
            }
            switch(message.getCommand()) {
                case NetworkCommands.Host.PLAY:
                    handler.obtainMessage(NetworkEvents.Client.HOST_PLAY).sendToTarget();
                    break;
                case NetworkCommands.Host.PAUSE:
                    handler.obtainMessage(NetworkEvents.Client.HOST_PAUSE).sendToTarget();
                    break;
                case NetworkCommands.Host.RESUME:
                    handler.obtainMessage(NetworkEvents.Client.HOST_RESUME).sendToTarget();
                    break;
                case NetworkCommands.Host.STOP:
                    handler.obtainMessage(NetworkEvents.Client.HOST_STOP).sendToTarget();
                    break;
                case NetworkCommands.EXIT:
                    handler.obtainMessage(NetworkEvents.Client.HOST_EXIT).sendToTarget();
                    break;
            }
        }
    }
}
