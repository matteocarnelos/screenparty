package it.unipd.dei.es.screenparty.party.client;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import it.unipd.dei.es.screenparty.media.MediaOptions;
import it.unipd.dei.es.screenparty.party.PartyCommands;
import it.unipd.dei.es.screenparty.party.PartyEvents;
import it.unipd.dei.es.screenparty.party.PartyManager;
import it.unipd.dei.es.screenparty.party.PartyMessage;
import it.unipd.dei.es.screenparty.party.PartyUtils;
import it.unipd.dei.es.screenparty.party.host.PartyHost;

public class PartyClient extends Thread {

    private Handler handler;

    private ConnectedHost host;

    private String hostIp;
    private float width;
    private float height;

    public PartyClient(Handler handler) {
        this.handler = handler;
    }

    public String getHostIp() {
        return hostIp;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void send(PartyMessage message) {
        PartyUtils.send(message, host, handler);
    }

    public void start(String hostIp, float width, float height) {
        this.hostIp = hostIp;
        this.width = width;
        this.height = height;
        super.start();
    }

    @Override
    public void interrupt() {
        super.interrupt();
        closeConnection();
    }

    private void closeConnection() {
        try { if(host != null) host.getSocket().close(); }
        catch(IOException e) { Log.d(PartyManager.LOG_TAG, e.toString()); }
    }

    @Override
    public void run() {
        InputStream inputStream;
        PartyMessage response;

        try {
            host = new ConnectedHost(new Socket(hostIp, PartyHost.SERVER_PORT));
            inputStream = host.getSocket().getInputStream();
        } catch(IOException e) {
            if(!isInterrupted()) {
                handler.obtainMessage(PartyEvents.JOIN_FAILED, e.getLocalizedMessage()).sendToTarget();
                closeConnection();
            }
            return;
        }

        PartyMessage request = new PartyMessage()
                .setCommand(PartyCommands.Client.JOIN)
                .addArgument(String.valueOf(width))
                .addArgument(String.valueOf(height));
        PartyUtils.send(request, host, handler);

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try { response = PartyMessage.parseString(reader.readLine()); }
        catch(IOException e) {
            if(!isInterrupted()) {
                handler.obtainMessage(PartyEvents.JOIN_FAILED, e.getLocalizedMessage()).sendToTarget();
                closeConnection();
            }
            return;
        }

        if(response.getCommand().equals(PartyCommands.Host.OK)) {
            MediaOptions.Position position = MediaOptions.Position.valueOf(request.getArgument(0));
            float videoWidth = Float.parseFloat(response.getArgument(1));
            float videoHeight = Float.parseFloat(response.getArgument(2));

            MediaOptions options = new MediaOptions(position, videoWidth, videoHeight);

            handler.obtainMessage(PartyEvents.Client.PARTY_JOINED, options).sendToTarget();
        } else {
            handler.obtainMessage(PartyEvents.Client.PARTY_FULL).sendToTarget();
            return;
        }

        while(true) {
            PartyMessage message;
            try { message = PartyMessage.parseString(reader.readLine()); }
            catch(IOException e) {
                if(!isInterrupted()) {
                    handler.obtainMessage(PartyEvents.Client.HOST_LEFT).sendToTarget();
                    closeConnection();
                }
                return;
            }
            switch(message.getCommand()) {
                case PartyCommands.Host.PLAY:
                    handler.obtainMessage(PartyEvents.Client.HOST_PLAY).sendToTarget();
                    break;
                case PartyCommands.Host.PAUSE:
                    handler.obtainMessage(PartyEvents.Client.HOST_PAUSE).sendToTarget();
                    break;
                case PartyCommands.Host.RESUME:
                    handler.obtainMessage(PartyEvents.Client.HOST_RESUME).sendToTarget();
                    break;
                case PartyCommands.Host.STOP:
                    handler.obtainMessage(PartyEvents.Client.HOST_STOP).sendToTarget();
                    break;
                case PartyCommands.EXIT:
                    handler.obtainMessage(PartyEvents.Client.HOST_EXIT).sendToTarget();
                    break;
            }
        }
    }
}
