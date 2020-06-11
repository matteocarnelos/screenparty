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
import it.unipd.dei.es.screenparty.network.NetworkCommands;
import it.unipd.dei.es.screenparty.network.NetworkEvents;
import it.unipd.dei.es.screenparty.network.NetworkMessage;
import it.unipd.dei.es.screenparty.party.PartyManager;
import it.unipd.dei.es.screenparty.party.PartyParams;

/**
 * Manages the MediaFragment fragment.
 */
public class MediaFragment extends Fragment implements TextureView.SurfaceTextureListener {

    private final String MEDIA_FRAGMENT_TAG = "MEDIA_FRAGMENT";

    private float notchBar=0;
    private TextureView textureView;
    private MediaPlayer mediaPlayer;
    private MediaController mediaController;
    private MediaSyncController mediaSyncController;
    private MediaModifier mediaModifier;
    private Dialogs dialogs = new Dialogs();

    private PartyManager partyManager = PartyManager.getInstance();
    private NavController navController;

    private AlertDialog temporaryPauseAlertDialog;
    private boolean exitedPlayer = false;

    /**
     * Manages the event of the back button being pressed.
     */
    private OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            dialogs.showBackConfirmationDialog();
        }
    };

    /**
     * Hides the UI on focus changed.
     */
    private ViewTreeObserver.OnWindowFocusChangeListener windowFocusChangeListener = new ViewTreeObserver.OnWindowFocusChangeListener() {
        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
            hideSystemUI();
        }
    };

    /**
     * Displays the UI as the screen is pressed.
     */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN && mediaController != null) {
                toggleMediaControlsVisibility();
                hideSystemUI();
            }
            return true;
        }
    };

    /**
     * Navigates back to the {@link StartFragment}.
     */
    private void goToStart() {
        partyManager.stop();
        requireView().getViewTreeObserver().removeOnWindowFocusChangeListener(windowFocusChangeListener);
        showSystemUI();
        navController.popBackStack(R.id.startFragment, false);
    }

    /**
     * Behaves accordingly to the {@link NetworkEvents} related to the received {@link Message}.
     */
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
                    partyManager.stop();
                    mediaPlayer.pause();
                    dialogs.showClientLeftDialog();
                    break;
                case NetworkEvents.Client.HOST_LEFT:
                    mediaPlayer.pause();
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

    /**
     * Manages the Dialog's windows.
     */
    private class Dialogs {

        /**
         * Shows the "Media preparation failed" dialog window.
         */
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

        /**
         * Shows the "Client left" dialog window.
         */
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

        /**
         * Shows the "Party Closed" dialog window.
         */
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

        /**
         * Shows the "Video temporary paused" dialog window.
         */
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

        /**
         * Shows the "Communication failed" dialog window.
         * @param message: The message to be displayed in the dialog window.
         */
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

        /**
         * Shows the "Are you sure?" dialog window.
         */
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

    /**
     * Hides the system UI.
     */
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

    /**
     * Shows the system UI.
     */
    private void showSystemUI() {
        ActionBar actionBar = ((AppCompatActivity)requireActivity()).getSupportActionBar();
        if(actionBar != null) actionBar.show();
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
    }

    /**
     * Sets and show the MediaController for the textureView.
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

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) throws IllegalArgumentException {
        Surface surfaceTexture = new Surface(surface);
        try {
            mediaPlayer.setDataSource(requireContext(), partyManager.getPartyParams().getMediaParams().getUri());
            mediaPlayer.setSurface(surfaceTexture);
        } catch(IOException e) { dialogs.showMediaPreparationFailedDialog(e.getLocalizedMessage()); }
        mediaPlayer.prepareAsync();

        /**
         * Listen for when the mediaPlayer {@link MediaPlayer} is done preparing.
         */
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if(textureView.getHeight()/partyManager.getPartyParams().getScreenParams().getYdpi()<partyManager.getPartyParams().getScreenParams().getHeight()) {
                    notchBar=(partyManager.getPartyParams().getScreenParams().getHeight()*partyManager.getPartyParams().getScreenParams().getYdpi()-textureView.getHeight())/
                    partyManager.getPartyParams().getScreenParams().getYdpi();
                    partyManager.getPartyParams().getScreenParams().setHeight((textureView.getHeight() / partyManager.getPartyParams().getScreenParams().getYdpi()));
                    Log.d(MEDIA_FRAGMENT_TAG,"Notch Bar: "+notchBar);
                }
                Log.d(MEDIA_FRAGMENT_TAG, "Texture height: " + textureView.getHeight());
                Log.d(MEDIA_FRAGMENT_TAG, "Texture width: " + textureView.getWidth());
                float aspectRatio = partyManager.getPartyParams().getMediaParams().getAspectRatio();
                textureView.setTransform(mediaModifier.prepareScreen(partyManager.getPartyParams(), aspectRatio,notchBar));
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


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }


    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }


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
            if(partyManager.isPartyReady()) mediaPlayer.start();
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
