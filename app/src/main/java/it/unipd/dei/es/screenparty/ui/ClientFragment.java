package it.unipd.dei.es.screenparty.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import it.unipd.dei.es.screenparty.R;
import it.unipd.dei.es.screenparty.network.NetworkEvents;
import it.unipd.dei.es.screenparty.party.PartyManager;

public class ClientFragment extends Fragment {

    private EditText invitationCodeText;

    private NavController navController;
    private PartyManager partyManager = PartyManager.getInstance();

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            switch (inputMessage.what) {
                case NetworkEvents.JOIN_FAILED:
                    new AlertDialog.Builder(getActivity())
                            .setMessage((String)inputMessage.obj)
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
                            })
                            .show();
                    break;
                case NetworkEvents.Client.PARTY_JOINED:
                    navController.navigate(R.id.actionToPrepare);
                    break;
                case NetworkEvents.Client.PARTY_FULL:
                    new AlertDialog.Builder(getActivity())
                            .setMessage("The party you are trying to connect is full")
                            .setTitle("Party full")
                            .setPositiveButton("OK", null)
                            .show();
                    break;
                case NetworkEvents.Client.HOST_LEFT:
                    new AlertDialog.Builder(getActivity())
                            .setMessage("The host has left the party")
                            .setTitle("You are alone")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    partyManager.stop();
                                    navController.popBackStack(R.id.startFragment, false);
                                }
                            })
                            .show();
                    break;
                case NetworkEvents.COMMUNICATION_FAILED:
                    new AlertDialog.Builder(getActivity())
                            .setMessage((String)inputMessage.obj)
                            .setTitle("Communication failed")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    partyManager.stop();
                                    navController.popBackStack(R.id.startFragment, false);
                                }
                            })
                            .show();
                    break;
                default: super.handleMessage(inputMessage);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        partyManager.setEventsHandler(handler);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                partyManager.stop();
                navController.popBackStack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client, container, false);

        Button nextButton = view.findViewById(R.id.nextButton);
        invitationCodeText = view.findViewById(R.id.invitationCodeText);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(invitationCodeText.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please insert a valid ip", Toast.LENGTH_LONG).show();
                } else partyManager.startAsClient(invitationCodeText.getText().toString());
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
    }
}
