package it.unipd.dei.es.screenparty.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import it.unipd.dei.es.screenparty.R;
import it.unipd.dei.es.screenparty.media.MediaParams;
import it.unipd.dei.es.screenparty.media.MediaUtils;
import it.unipd.dei.es.screenparty.network.NetworkEvents;
import it.unipd.dei.es.screenparty.network.NetworkUtils;
import it.unipd.dei.es.screenparty.party.PartyManager;
import it.unipd.dei.es.screenparty.party.PartyUtils;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Manages the ClientFragment fragment.
 */
public class ClientFragment extends Fragment {

    private static final String IP_PATTERN_STRING = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    private TextInputLayout hostIpField;
    private TextView clientConnectedLabel;
    private ProgressBar clientSpinner;
    private ImageView clientConnectedIcon;
    private Snackbar invalidIpSnackbar;
    private TextView waitHostLabel;
    private Button connectButton;
    private Dialogs dialogs = new Dialogs();

    private NavController navController;
    private PartyManager partyManager = PartyManager.getInstance();

    OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            goBack();
        }
    };

    /**
     * Used to navigate back to the previous fragment (StartFragment) upon pressing the back button
     * on the navigation bar or on the top left corner of the screen.
     */
    private void goBack() {
        partyManager.stop();
        navController.popBackStack();
    }

    View.OnClickListener nextButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View view = requireActivity().getCurrentFocus();
            if(view != null) {
                InputMethodManager imm = (InputMethodManager)requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            String ip = hostIpField.getEditText().getText().toString();
            if(!ip.matches(IP_PATTERN_STRING)) invalidIpSnackbar.show();
            else {
                connectButton.setEnabled(false);
                hostIpField.getEditText().setInputType(InputType.TYPE_NULL);
                setStateConnecting();
                partyManager.startAsClient(ip);
            }
        }
    };

    View.OnFocusChangeListener hostIpFieldListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus && hostIpField.getEditText().getText().toString().isEmpty()) {
                String ip = NetworkUtils.getIPAddress(true);
                hostIpField.getEditText().setText(ip.substring(0, ip.lastIndexOf('.') + 1));
            }
        }
    };

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NotNull Message msg) {
            switch(msg.what) {
                case NetworkEvents.JOIN_FAILED:
                    resetState();
                    dialogs.showJoinFailedDialog((String)msg.obj);
                    break;
                case NetworkEvents.Client.PARTY_JOINED:
                    setStateConnected();
                    break;
                case NetworkEvents.Client.HOST_NEXT:
                    navController.navigate(R.id.actionToPrepare);
                    break;
                case NetworkEvents.Client.PARTY_FULL:
                    resetState();
                    dialogs.showPartyFullDialog();
                    break;
                case NetworkEvents.Client.HOST_LEFT:
                    resetState();
                    dialogs.showHostLeftDialog();
                    break;
                case NetworkEvents.COMMUNICATION_FAILED:
                    resetState();
                    dialogs.showCommunicationFailedDialog((String)msg.obj);
                    break;
                default: super.handleMessage(msg);
            }
        }
    };

    /**
     * Manages the Dialogs windows.
     */
    private class Dialogs {

        /**
         * Shows the "Media Error" dialog window.
         */
        private void showInvalidUriDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_media_error)
                    .setMessage(R.string.dialog_message_invalid_uri)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            goBack();
                        }
                    })
                    .setPositiveButton(R.string.dialog_button_retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            openMediaPicker();
                        }
                    })
                    .setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goBack();
                        }
                    }).show();
        }

        /**
         * Shows the "Join Failed" dialog window.
         * @param message: The message to be displayed in the dialog window.
         */
        private void showJoinFailedDialog(String message) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_join_failed)
                    .setMessage(message)
                    .setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            partyManager.stop();
                        }
                    })
                    .setPositiveButton(R.string.dialog_button_retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            connectButton.performClick();
                        }
                    }).show();
        }

        /**
         * Shows the "Party Full" dialog window.
         */
        private void showPartyFullDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_party_full)
                    .setMessage(R.string.dialog_message_party_full)
                    .setPositiveButton(R.string.dialog_button_ok, null)
                    .show();
        }

        /**
         * Shows the "Host Left" dialog window.
         */
        private void showHostLeftDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_party_closed)
                    .setMessage(R.string.dialog_message_party_closed)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            resetState();
                        }
                    })
                    .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resetState();
                        }
                    }).show();
        }

        /**
         * Shows the "Communication Failed" dialog window.
         * @param message: The message to be displayed in the dialog window.
         */
        private void showCommunicationFailedDialog(String message) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_communication_failed)
                    .setMessage(message)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            goBack();
                        }
                    })
                    .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goBack();
                        }
                    }).show();
        }
    }

    /**
     * Reset the state of the ClientFragment fragment, that is:
     *  - The hostIpField TextInputLayout is cleared.
     *  - The connectButton Button is enabled.
     *  - No label, spinner or icon are shown.
     */
    private void resetState() {
        connectButton.setEnabled(true);
        hostIpField.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
        clientConnectedLabel.setText("");
        clientSpinner.setVisibility(View.INVISIBLE);
        clientConnectedIcon.setVisibility(View.INVISIBLE);
        waitHostLabel.setVisibility(View.INVISIBLE);
    }

    /**
     * Set the state of the ClientFragment fragment as Connecting, that is:
     *  - The clientConnectedLabel TextView shows "Connecting...".
     *  - The clientSpinner ProgressBar is visible.
     *  - The clientConnectedIcon ImageView is not visible
     *  - The connectButton Button is disabled.
     */
    private void setStateConnecting() {
        clientConnectedLabel.setText(R.string.client_connected_label_connecting);
        clientSpinner.setVisibility(View.VISIBLE);
        clientConnectedIcon.setVisibility(View.INVISIBLE);
        waitHostLabel.setVisibility(View.INVISIBLE);
    }

    /**
     * Set the state of the ClientFragment fragment as Connected, that is:
     *  - The clientConnectedLabel TextView shows "Connected!".
     *  - The clientSpinner ProgressBar is not visible.
     *  - The clientConnectedIcon ImageView is visible
     *  - The connectButton Button is disabled.
     */
    private void setStateConnected() {
        clientConnectedLabel.setText(R.string.client_connected_label_connected);
        clientSpinner.setVisibility(View.INVISIBLE);
        clientConnectedIcon.setVisibility(View.VISIBLE);
        waitHostLabel.setVisibility(View.VISIBLE);
    }

    /**
     * Open the MediaPicker to select a video
     */
    private void openMediaPicker() {
        MediaUtils.openMediaPicker(this);
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
        View view = inflater.inflate(R.layout.fragment_client, container, false);

        connectButton = view.findViewById(R.id.connect_button);
        hostIpField = view.findViewById(R.id.host_ip_field);
        clientConnectedLabel = view.findViewById(R.id.client_connected_label);
        clientConnectedIcon = view.findViewById(R.id.client_connected_icon);
        clientSpinner = view.findViewById(R.id.client_spinner);
        invalidIpSnackbar = Snackbar.make(view, R.string.snackbar_text_invalid_ip, Snackbar.LENGTH_SHORT);
        waitHostLabel = view.findViewById(R.id.wait_host_label);

        View snackbarView = invalidIpSnackbar.getView();
        int snackbarTextId = com.google.android.material.R.id.snackbar_text ;
        TextView textView = snackbarView.findViewById(snackbarTextId);
        textView.setTextColor(getResources().getColor(R.color.white_50));
        snackbarView.setBackground(new ColorDrawable(getResources().getColor(R.color.black_800)));

        connectButton.setOnClickListener(nextButtonListener);
        hostIpField.getEditText().setOnFocusChangeListener(hostIpFieldListener);

        String deviceName = PartyUtils.getDeviceName(requireActivity().getContentResolver());
        partyManager.getPartyParams().setDeviceName(deviceName);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
        openMediaPicker();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MediaUtils.SELECT_MEDIA_REQUEST_CODE) {
            if(resultCode == RESULT_CANCELED) goBack();
            else if(resultCode == RESULT_OK) {
                Uri selectedUri = data.getData();
                if(selectedUri == null) dialogs.showInvalidUriDialog();

                MediaParams mediaParams = MediaUtils.analyzeMedia(requireContext(), selectedUri);

                partyManager.getPartyParams().setMediaParams(mediaParams);
            }
        }
    }
}
