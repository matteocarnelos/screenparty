package it.unipd.dei.es.screenparty.ui;

import android.content.DialogInterface;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.MediaController;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import it.unipd.dei.es.screenparty.R;
import it.unipd.dei.es.screenparty.media.MediaModifier;
import it.unipd.dei.es.screenparty.media.MediaSyncController;
import it.unipd.dei.es.screenparty.media.MediaUtils;
import it.unipd.dei.es.screenparty.network.NetworkCommands;
import it.unipd.dei.es.screenparty.network.NetworkEvents;
import it.unipd.dei.es.screenparty.network.NetworkMessage;
import it.unipd.dei.es.screenparty.party.PartyManager;
import it.unipd.dei.es.screenparty.party.PartyParams;

public class MediaFragment extends Fragment implements TextureView.SurfaceTextureListener {

    private final String MEDIA_FRAGMENT_TAG = "MEDIA_FRAGMENT";
    public final int NOTCH_MINIMUM_HEIGHT = 24;

    private TextureView textureView;
    private MediaPlayer mediaPlayer;
    private MediaController mediaController;
    private MediaSyncController mediaSyncController;
    private MediaModifier mediaModifier;
    private Dialogs dialogs = new Dialogs();

    private PartyManager partyManager = PartyManager.getInstance();
    private NavController navController;

    private float statusBarHeight;
    private AlertDialog temporaryPauseAlertDialog;
    private boolean exitedPlayer = false;
    private boolean partyOpened = true;

    private OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            dialogs.showBackConfirmationDialog();
        }
    };

    private ViewTreeObserver.OnWindowFocusChangeListener windowFocusChangeListener = new ViewTreeObserver.OnWindowFocusChangeListener() {
        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
            hideSystemUI();
        }
    };

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN && mediaController != null) {
                toggleMediaControlsVisibility();
                hideSystemUI();
            }
            return true;
        }
    };

    private void goToStart() {
        partyManager.stop();
        requireView().getViewTreeObserver().removeOnWindowFocusChangeListener(windowFocusChangeListener);
        showSystemUI();
        navController.popBackStack(R.id.startFragment, false);
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what) {
                case NetworkEvents.Client.HOST_PLAY:
                    mediaPlayer.start();
                    break;
                case NetworkEvents.Client.HOST_PAUSE:
                    mediaPlayer.pause();
                    break;
                case NetworkEvents.Client.HOST_SEEK:
                    int pos = (int) msg.obj;
                    mediaPlayer.seekTo(pos);
                    break;
                case NetworkEvents.Host.CLIENT_LEFT:
                    mediaPlayer.pause();
                    dialogs.showClientLeftDialog();
                    break;
                case NetworkEvents.Client.HOST_LEFT:
                    mediaPlayer.pause();
                    partyOpened = false;
                    dialogs.showHostLeftDialog();
                    break;
                case NetworkEvents.COMMUNICATION_FAILED:
                    mediaPlayer.pause();
                    dialogs.showCommunicationFailedDialog((String) msg.obj);
                    break;
                case NetworkEvents.Host.CLIENT_EXIT_PLAYER:
                    mediaPlayer.pause();
                    dialogs.showClientExitPlayerDialog();
                    break;
                case NetworkEvents.Host.CLIENT_ENTER_PLAYER:
                    temporaryPauseAlertDialog.dismiss();
                    mediaPlayer.start();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private class Dialogs {

        private void showMediaPreparationFailedDialog(String message) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_media_preparation_failed)
                    .setMessage(message)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            goToStart();
                        }
                    })
                    .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goToStart();
                        }
                    }).show();
        }

        private void showClientLeftDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_client_left)
                    .setMessage(R.string.dialog_message_client_left)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            goToStart();
                        }
                    })
                    .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goToStart();
                        }
                    }).show();
        }

        private void showHostLeftDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_party_closed)
                    .setMessage(R.string.dialog_message_party_closed)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            goToStart();
                        }
                    })
                    .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goToStart();
                        }
                    }).show();
        }

        private void showClientExitPlayerDialog() {
            temporaryPauseAlertDialog = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_client_exit_player)
                    .setMessage(R.string.dialog_message_client_exit_player)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            goToStart();
                        }
                    })
                    .setPositiveButton(R.string.dialog_button_quit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goToStart();
                        }
                    }).create();
            temporaryPauseAlertDialog.show();
        }

        private void showCommunicationFailedDialog(String message) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_communication_failed)
                    .setMessage(message)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            goToStart();
                        }
                    })
                    .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goToStart();
                        }
                    })
                    .show();
        }

        private void showBackConfirmationDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_back_confirmation)
                    .setMessage(R.string.dialog_message_back_confirmation)
                    .setPositiveButton(R.string.dialog_button_cancel, null)
                    .setNegativeButton(R.string.dialog_button_quit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goToStart();
                        }
                    }).show();
        }
    }

    private void hideSystemUI() {
        ActionBar actionBar = ((AppCompatActivity)requireActivity()).getSupportActionBar();
        if(actionBar != null) actionBar.hide();
        View decorView = requireActivity().getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
        requireActivity().getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void showSystemUI() {
        ActionBar actionBar = ((AppCompatActivity)requireActivity()).getSupportActionBar();
        if(actionBar != null) actionBar.show();
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
    }

    /**
     * Creates the MediaController for the textureView.
     */
    private void enableMediaController() {
        mediaController.setMediaPlayer(mediaSyncController);
        mediaController.setAnchorView(textureView);
        mediaController.setEnabled(true);
        mediaController.show();
    }

    /**
     * Switches the state (visible or not) of the media controller.
     */
    private void toggleMediaControlsVisibility() {
        if(mediaController.isShowing()) mediaController.hide();
        else mediaController.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        partyManager.setEventsHandler(handler);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media, container, false);

        mediaModifier = new MediaModifier();
        textureView = view.findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);
        mediaPlayer = new MediaPlayer();
        mediaController = new MediaController(getContext());
        mediaSyncController = new MediaSyncController(mediaPlayer);

        view.getViewTreeObserver().addOnWindowFocusChangeListener(windowFocusChangeListener);
        view.setOnTouchListener(touchListener);

        int statusBarHeightPixels = MediaUtils.getStatusBarHeightPixels(requireActivity().getWindow());
        statusBarHeight = statusBarHeightPixels / partyManager.getPartyParams().getScreenParams().getYdpi();

        Log.d(MEDIA_FRAGMENT_TAG, "status bar: " + (statusBarHeight * partyManager.getPartyParams().getScreenParams().getYdpi()));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
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
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) throws IllegalArgumentException {
        Surface surfaceTexture = new Surface(surface);
        try {
            mediaPlayer.setDataSource(requireContext(), partyManager.getPartyParams().getMediaParams().getUri());
            mediaPlayer.setSurface(surfaceTexture);
        } catch(IOException e) { dialogs.showMediaPreparationFailedDialog(e.getLocalizedMessage()); }
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if(statusBarHeight * partyManager.getPartyParams().getScreenParams().getYdpi() > NOTCH_MINIMUM_HEIGHT)
                    partyManager.getPartyParams().getScreenParams().setHeight((partyManager.getPartyParams().getScreenParams().getHeight() - statusBarHeight));

                Log.d(MEDIA_FRAGMENT_TAG, "Texture height: " + textureView.getHeight());
                Log.d(MEDIA_FRAGMENT_TAG, "Texture width: " + textureView.getWidth());
                Log.d(MEDIA_FRAGMENT_TAG, String.valueOf(mp.getVideoHeight()));
                Log.d(MEDIA_FRAGMENT_TAG, String.valueOf(mp.getVideoWidth()));

                float aspectRatio = partyManager.getPartyParams().getMediaParams().getAspectRatio();
                textureView.setTransform(mediaModifier.prepareScreen(partyManager.getPartyParams(), aspectRatio));
                if(partyManager.getPartyParams().getRole() == PartyParams.Role.HOST)
                    enableMediaController();
                else partyManager.sendMessage(new NetworkMessage(NetworkCommands.Client.READY));
                if(partyManager.getPartyParams().getPosition() == PartyParams.Position.RIGHT)
                    mediaPlayer.setVolume(0, 1);
                else if(partyManager.getPartyParams().getPosition() == PartyParams.Position.LEFT)
                    mediaPlayer.setVolume(1, 0);
                else mediaPlayer.setVolume(1, 1);
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

    @Override
    public void onPause() {
        if(mediaPlayer != null && mediaPlayer.isPlaying()) {
            if(partyManager.getPartyParams().getRole() == PartyParams.Role.HOST)
                partyManager.sendMessage(new NetworkMessage(NetworkCommands.Host.PAUSE));
            else partyManager.sendMessage(new NetworkMessage(NetworkCommands.Client.EXIT_PLAYER));
            mediaPlayer.pause();
            exitedPlayer = true;
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        hideSystemUI();
        if(mediaPlayer != null && exitedPlayer) {
            if(partyManager.getPartyParams().getRole() == PartyParams.Role.HOST)
                partyManager.sendMessage(new NetworkMessage(NetworkCommands.Host.PLAY));
            else partyManager.sendMessage(new NetworkMessage(NetworkCommands.Client.ENTER_PLAYER));
            if(partyOpened) mediaPlayer.start();
            exitedPlayer = false;
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
    }
}
