package it.unipd.dei.es.screenparty.media;


import android.media.MediaPlayer;
import android.widget.MediaController;

import it.unipd.dei.es.screenparty.network.NetworkCommands;
import it.unipd.dei.es.screenparty.network.NetworkMessage;
import it.unipd.dei.es.screenparty.party.PartyManager;

/**
 * Modified version of the MediaController class.
 */
public class MyMediaController implements MediaController.MediaPlayerControl {

    private PartyManager partyManager = PartyManager.getInstance();
    private MediaPlayer mMediaPlayer;

    public MyMediaController(MediaPlayer mMediaPlayer) {
        this.mMediaPlayer = mMediaPlayer;
    }

    //For every overridden methods check the superclass documentation
    @Override
    public void start() {
        partyManager.sendMessage(new NetworkMessage(NetworkCommands.Host.PLAY));
        mMediaPlayer.start();
    }

    @Override
    public void pause() {
        mMediaPlayer.pause();
        NetworkMessage message = new NetworkMessage.Builder()
                .setCommand(NetworkCommands.Host.PAUSE)
                .addArgument(String.valueOf(getCurrentPosition()))
                .build();
        partyManager.sendMessage(message);
    }

    @Override
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        NetworkMessage message = new NetworkMessage.Builder()
                .setCommand(NetworkCommands.Host.SEEK)
                .addArgument(String.valueOf(pos))
                .build();
        partyManager.sendMessage(message);
        mMediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
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

    @Override
    public int getAudioSessionId() {
        return 0;
    }

}

