package it.unipd.dei.es.screenparty.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

import it.unipd.dei.es.screenparty.R;
import it.unipd.dei.es.screenparty.network.NetworkEvents;
import it.unipd.dei.es.screenparty.party.PartyManager;

public class ClientFragment extends Fragment {

    private final String ipPatternString = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    private TextInputLayout hostIpField;
    private TextView clientConnectedLabel;
    private ProgressBar clientSpinner;
    private ImageView clientConnectedIcon;
    private Snackbar invalidIpSnackbar;
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

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case NetworkEvents.Client.PARTY_CONNECTING:
                    clientConnectedLabel.setText("Connecting...");
                    clientSpinner.setVisibility(View.VISIBLE);
                    clientConnectedIcon.setVisibility(View.GONE);
                    break;
                case NetworkEvents.JOIN_FAILED:
                    clientConnectedLabel.setText("");
                    clientSpinner.setVisibility(View.GONE);
                    clientConnectedIcon.setVisibility(View.GONE);
                    dialogs.showJoinFailedDialog((String)msg.obj);
                    break;
                case NetworkEvents.Client.PARTY_JOINED:
                    clientConnectedLabel.setText("Connected!");
                    clientSpinner.setVisibility(View.GONE);
                    clientConnectedIcon.setVisibility(View.VISIBLE);
                    break;
                case NetworkEvents.Client.HOST_NEXT:
                    clientConnectedLabel.setText("");
                    clientSpinner.setVisibility(View.GONE);
                    clientConnectedIcon.setVisibility(View.GONE);
                    navController.navigate(R.id.actionToPrepare);
                    break;
                case NetworkEvents.Client.PARTY_FULL:
                    clientConnectedLabel.setText("");
                    clientSpinner.setVisibility(View.GONE);
                    clientConnectedIcon.setVisibility(View.GONE);
                    dialogs.showPartyFullDialog();
                    break;
                case NetworkEvents.Client.HOST_LEFT:
                    clientConnectedLabel.setText("");
                    clientSpinner.setVisibility(View.GONE);
                    clientConnectedIcon.setVisibility(View.GONE);
                    dialogs.showHostLeftDialog();
                    break;
                case NetworkEvents.FILE_TRANSFER_FAILED:
                    clientConnectedLabel.setText("");
                    clientSpinner.setVisibility(View.GONE);
                    clientConnectedIcon.setVisibility(View.GONE);
                    dialogs.showFileTransferFailedDialog((String)msg.obj);
                    break;
                case NetworkEvents.COMMUNICATION_FAILED:
                    clientConnectedLabel.setText("");
                    clientSpinner.setVisibility(View.GONE);
                    clientConnectedIcon.setVisibility(View.GONE);
                    dialogs.showCommunicationFailedDialog((String)msg.obj);
                    break;
                default: super.handleMessage(msg);
            }
        }
    };

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
                            partyManager.restart();
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
                    .setTitle("You are alone")
                    .setMessage("The host has left the party")
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

        private void showFileTransferFailedDialog(String message) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("File transfer failed")
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

        Button connectButton = view.findViewById(R.id.connect_button);
        hostIpField = view.findViewById(R.id.host_ip_field);

        clientConnectedLabel = view.findViewById(R.id.client_connected_label);
        clientConnectedIcon = view.findViewById(R.id.client1_connected_icon);
        clientSpinner = view.findViewById(R.id.client_spinner);
        invalidIpSnackbar = Snackbar.make(view, "Please insert a valid IP", Snackbar.LENGTH_SHORT);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!((hostIpField.getEditText().getText().toString()).matches(ipPatternString))) {
                    invalidIpSnackbar.show();
                } else {
                    partyManager.startAsClient(hostIpField.getEditText().getText().toString());
                }
            }
        });



        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
        ((AppCompatActivity)requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
