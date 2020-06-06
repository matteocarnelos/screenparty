package it.unipd.dei.es.screenparty.ui;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.MediaController;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import java.io.IOException;

import it.unipd.dei.es.screenparty.R;
import it.unipd.dei.es.screenparty.media.MediaModifier;
import it.unipd.dei.es.screenparty.media.MyMediaController;
import it.unipd.dei.es.screenparty.party.PartyManager;

public class MediaFragment extends Fragment implements TextureView.SurfaceTextureListener {

    private final String MEDIA_FRAGMENT_TAG = "MEDIA_FRAGMENT";
    private TextureView textureView;
    private MediaPlayer mediaPlayer;
    private MediaController mediaController;
    private MyMediaController mMediaController;
    private MediaModifier mediaModifier;
    private PartyManager partyManager;
    private final int maxHeight=1920;
    private float mediaHeight;
    private float mediaWidth;
    private float statusBarHeight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media, container, false);
        partyManager = PartyManager.getInstance();
        mediaModifier=new MediaModifier();
        textureView = view.findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);
        mediaPlayer = new MediaPlayer();
        mediaController = new MediaController(getContext());
        mMediaController = new MyMediaController(mediaPlayer);
        view.getViewTreeObserver().addOnWindowFocusChangeListener(new ViewTreeObserver.OnWindowFocusChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onWindowFocusChanged(boolean hasFocus) {
                hideSystemUI();
            }
        });
        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN && mediaController != null) {
                    toggleMediaControlsVisibility();

                }
                return true;
            }
        });
        Rect rectangle = new Rect();
        Window window = requireActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        statusBarHeight = rectangle.top/partyManager.getPartyParams().getScreenParams().getYdpi();
        Log.d(MEDIA_FRAGMENT_TAG, "status bar: " + statusBarHeight);
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void hideSystemUI() {
        View decorView = requireActivity().getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    /**
     * Invoked when a {@link TextureView}'s SurfaceTexture is ready for use.
     * Prepare the media player and the media controller ready to be used.
     *
     * @param surface The surface returned by
     *                {@link android.view.TextureView#getSurfaceTexture()}
     * @param width   The width of the surface.
     * @param height  The height of the surface.
     * @throws IllegalArgumentException If the set data source doesn't exist.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) throws IllegalArgumentException {
        Surface surfaceTexture = new Surface(surface);
        try {
            String videoPath = "android.resource://" + requireActivity().getPackageName() + "/" + R.raw.video;
            Uri uri = Uri.parse(videoPath);
            mediaPlayer.setDataSource(requireContext(), uri);
            mediaPlayer.setSurface(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaHeight = mp.getVideoHeight();
                mediaWidth = mp.getVideoWidth();
                if (partyManager.getPartyParams().getScreenParams().getHeight() > maxHeight/partyManager.getPartyParams().getScreenParams().getYdpi())
                    partyManager.getPartyParams().getScreenParams().setHeight((partyManager.getPartyParams().getScreenParams().getHeight() - statusBarHeight));
                Log.d(MEDIA_FRAGMENT_TAG, "Texture height: " + textureView.getHeight());
                Log.d(MEDIA_FRAGMENT_TAG, "Texture width: " + textureView.getWidth());
                Log.d(MEDIA_FRAGMENT_TAG, String.valueOf(mp.getVideoHeight()));
                Log.d(MEDIA_FRAGMENT_TAG, String.valueOf(mp.getVideoWidth()));
                textureView.setTransform(mediaModifier.prepareScreen(partyManager.getPartyParams(), mediaWidth / mediaHeight));
                mediaPlayer.start();
                mMediaControllerEnable();
            }
        });
    }

    /**
     * Invoked when the {@link SurfaceTexture}'s buffers size changed.
     *
     * @param surface The surface returned by
     *                {@link android.view.TextureView#getSurfaceTexture()}.
     * @param width   The new width of the surface.
     * @param height  The new height of the surface.
     */
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    /**
     * Invoked when the specified {@link SurfaceTexture} is about to be destroyed.
     * If returns true, no rendering should happen inside the surface texture after this method
     * is invoked. If returns false, the client needs to call {@link SurfaceTexture#release()}.
     * Most applications should return true.
     *
     * @param surface The surface about to be destroyed
     */
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    /**
     * Invoked when the specified {@link SurfaceTexture} is updated through
     * {@link SurfaceTexture#updateTexImage()}.
     *
     * @param surface The surface just updated
     */
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }


    /**
     * Creates the MediaController for the textureView.
     */
    private void mMediaControllerEnable() {
        mediaController.setMediaPlayer(mMediaController);
        mediaController.setAnchorView(textureView);
        mediaController.setEnabled(true);
        mediaController.show();
    }

    @Override
    public void onPause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        if (mediaPlayer != null)
            mediaPlayer.start();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    /**
     * Switches the state (visible or not) of the media controller.
     */
    private void toggleMediaControlsVisibility() {
        if (mediaController.isShowing()) {
            mediaController.hide();
        } else {
            mediaController.show();
        }
    }
}
