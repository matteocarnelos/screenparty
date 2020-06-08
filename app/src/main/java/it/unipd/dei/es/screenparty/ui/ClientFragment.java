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
import android.provider.Settings;
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

import it.unipd.dei.es.screenparty.R;
import it.unipd.dei.es.screenparty.media.MediaParams;
import it.unipd.dei.es.screenparty.media.MediaUtils;
import it.unipd.dei.es.screenparty.network.NetworkEvents;
import it.unipd.dei.es.screenparty.party.PartyManager;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class ClientFragment extends Fragment {

    private static final String IP_PATTERN_STRING = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    private TextInputLayout hostIpField;
    private TextView clientConnectedLabel;
    private ProgressBar clientSpinner;
    private ImageView clientConnectedIcon;
    private Snackbar invalidIpSnackbar;
    private TextView waitHostLabel;
    Button connectButton;
    private Dialogs dialogs = new Dialogs();

    private NavController navController;
    private PartyManager partyManager = PartyManager.getInstance();

    OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            partyManager.stop();
            navController.popBackStack();
        }
    };

    View.OnClickListener nextButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View view = requireActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            if(!((hostIpField.getEditText().getText().toString()).matches(IP_PATTERN_STRING))) {
                invalidIpSnackbar.show();
            } else {
                connectButton.setEnabled(false);
                setStateConnecting();
                partyManager.startAsClient(hostIpField.getEditText().getText().toString());
            }
        }
    };

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
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

    private void resetState() {
        connectButton.setEnabled(true);
        clientConnectedLabel.setText("");
        clientSpinner.setVisibility(View.INVISIBLE);
        clientConnectedIcon.setVisibility(View.INVISIBLE);
        waitHostLabel.setVisibility(View.INVISIBLE);
    }

    private void setStateConnecting() {
        clientConnectedLabel.setText("Connecting...");
        clientSpinner.setVisibility(View.VISIBLE);
        clientConnectedIcon.setVisibility(View.INVISIBLE);
        waitHostLabel.setVisibility(View.INVISIBLE);
    }

    private void setStateConnected() {
        clientConnectedLabel.setText("Connected!");
        clientSpinner.setVisibility(View.INVISIBLE);
        clientConnectedIcon.setVisibility(View.VISIBLE);
        waitHostLabel.setVisibility(View.VISIBLE);
    }

    private class Dialogs {

        private void showJoinFailedDialog(String message) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Join failed")
                    .setMessage(message)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            partyManager.stop();
                        }
                    })
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            connectButton.performClick();
                        }
                    }).show();
        }

        private void showPartyFullDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Party full")
                    .setMessage("The party you are trying to connect is full")
                    .setPositiveButton("Ok", null)
                    .show();
        }

        private void showHostLeftDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Party no long exists")
                    .setMessage("The host has left the party")
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            resetState();
                        }
                    })
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resetState();
                        }
                    }).show();
        }

        private void showCommunicationFailedDialog(String message) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Communication failed")
                    .setMessage(message)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            backPressedCallback.handleOnBackPressed();
                        }
                    })
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            backPressedCallback.handleOnBackPressed();
                        }
                    }).show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        partyManager.setEventsHandler(handler);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_client, container, false);

        connectButton = view.findViewById(R.id.connect_button);
        hostIpField = view.findViewById(R.id.host_ip_field);

        clientConnectedLabel = view.findViewById(R.id.client_connected_label);
        clientConnectedIcon = view.findViewById(R.id.client_connected_icon);
        clientSpinner = view.findViewById(R.id.client_spinner);
        invalidIpSnackbar = Snackbar.make(view, "Please insert a valid IP", Snackbar.LENGTH_SHORT);
        waitHostLabel = view.findViewById(R.id.wait_host_label);
        View snackbarView = invalidIpSnackbar.getView();
        int snackbarTextId = com.google.android.material.R.id.snackbar_text ;
        TextView textView = (TextView)snackbarView.findViewById(snackbarTextId);
        textView.setTextColor(getResources().getColor(R.color.white_50));
        snackbarView.setBackground(new ColorDrawable(getResources().getColor(R.color.black_800)));

        connectButton.setOnClickListener(nextButtonListener);

        String deviceName = Settings.Secure.getString(requireActivity().getContentResolver(), "bluetooth_name");
        partyManager.getPartyParams().setDeviceName(deviceName);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
        openMediaPicker();
    }

    private void openMediaPicker() {
        MediaUtils.openMediaPicker(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MediaUtils.SELECT_MEDIA_REQUEST_CODE) {
            if(resultCode == RESULT_CANCELED) backPressedCallback.handleOnBackPressed();
            else if(resultCode == RESULT_OK) {
                Uri selectedUri = data.getData();
                if(selectedUri == null) return;

                MediaParams mediaParams = MediaUtils.analyzeMedia(requireContext(), selectedUri);

                partyManager.getPartyParams().setMediaParams(mediaParams);
            }
        }
    }
}
