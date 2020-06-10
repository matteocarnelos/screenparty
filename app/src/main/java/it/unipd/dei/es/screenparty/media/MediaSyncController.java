package it.unipd.dei.es.screenparty.media;

import android.media.MediaPlayer;
import android.widget.MediaController;

import it.unipd.dei.es.screenparty.network.NetworkCommands;
import it.unipd.dei.es.screenparty.network.NetworkMessage;
import it.unipd.dei.es.screenparty.party.PartyManager;

/**
 * Modified version of the MediaController class.
 */
public class MediaSyncController implements MediaController.MediaPlayerControl {

    private PartyManager partyManager = PartyManager.getInstance();
    private MediaPlayer mediaPlayer;

    public MediaSyncController(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    @Override
    public void start() {
        partyManager.sendMessage(new NetworkMessage(NetworkCommands.Host.PLAY));
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        mediaPlayer.start();
                    }
                },
                10
        );
    }

    @Override
    public void pause() {
        partyManager.sendMessage(new NetworkMessage(NetworkCommands.Host.PAUSE));
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        mediaPlayer.pause();
                    }
                },
                10
        );
    }

    @Override
    public void seekTo(final int pos) {
        NetworkMessage message = new NetworkMessage.Builder()
                .setCommand(NetworkCommands.Host.SEEK)
                .addArgument(String.valueOf(pos))
                .build();
        partyManager.sendMessage(message);
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        mediaPlayer.seekTo(pos);
                    }
                },
                10
        );
    }

    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }
}
