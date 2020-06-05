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
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import it.unipd.dei.es.screenparty.R;
import it.unipd.dei.es.screenparty.network.NetworkEvents;
import it.unipd.dei.es.screenparty.party.PartyManager;

public class ClientFragment extends Fragment {

    private TextInputLayout hostIpField;
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
            switch (msg.what) {
                case NetworkEvents.JOIN_FAILED:
                    dialogs.showJoinFailedDialog((String)msg.obj);
                    break;
                case NetworkEvents.Client.PARTY_JOINED:
                    navController.navigate(R.id.actionToPrepare);
                    break;
                case NetworkEvents.Client.PARTY_FULL:
                    dialogs.showPartyFullDialog();
                    break;
                case NetworkEvents.Client.HOST_LEFT:
                    dialogs.showHostLeftDialog();
                    break;
                case NetworkEvents.COMMUNICATION_FAILED:
                    dialogs.showCommunicationFailedDialog((String)msg.obj);
                    break;
                default: super.handleMessage(msg);
            }
        }
    };

    private class Dialogs {

        private void showJoinFailedDialog(String message) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setMessage(message)
                    .setTitle("Join failed")
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
                    .setMessage("The party you are trying to connect is full")
                    .setTitle("Party full")
                    .setPositiveButton("OK", null)
                    .show();
        }

        private void showHostLeftDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                    .setMessage("The host has left the party")
                    .setTitle("You are alone")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            partyManager.stop();
                            navController.popBackStack(R.id.startFragment, false);
                        }
                    }).show();
        }

        private void showCommunicationFailedDialog(String message) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setMessage(message)
                    .setTitle("Communication failed")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            partyManager.stop();
                            navController.popBackStack(R.id.startFragment, false);
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
        View view = inflater.inflate(R.layout.fragment_client, container, false);

        Button connectButton = view.findViewById(R.id.connect_button);
        hostIpField = view.findViewById(R.id.host_ip_field);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hostIpField.getEditText().getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please insert a valid ip", Toast.LENGTH_LONG).show();
                } else partyManager.startAsClient(hostIpField.getEditText().getText().toString());
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
