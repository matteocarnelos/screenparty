package it.unipd.dei.es.screenparty.media;

import android.media.MediaPlayer;
import android.widget.MediaController;

import it.unipd.dei.es.screenparty.network.NetworkCommands;
import it.unipd.dei.es.screenparty.network.NetworkMessage;
import it.unipd.dei.es.screenparty.party.PartyManager;

/**
 * Synchronize the media controller's commands among the device's.
 */
public class MediaSyncController implements MediaController.MediaPlayerControl {

    private static final int COMMAND_DELAY = 10;

    private PartyManager partyManager = PartyManager.getInstance();
    private MediaPlayer mediaPlayer;

    public MediaSyncController(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    /**
     *Plays the video in the current device and send the command to the others.
     * In the current device the video is played with a small delay
     */
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
                COMMAND_DELAY
        );
    }

    /**
     *Pauses the video in the current device and send the command to the others.
     */
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
                COMMAND_DELAY
        );
    }

    /**
     * Seeks to specified time position in the current device and send the command to the others.
     * @param pos
     */
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
                COMMAND_DELAY
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
