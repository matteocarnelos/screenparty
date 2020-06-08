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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.unipd.dei.es.screenparty.R;
import it.unipd.dei.es.screenparty.network.NetworkCommands;
import it.unipd.dei.es.screenparty.network.NetworkEvents;
import it.unipd.dei.es.screenparty.network.NetworkMessage;
import it.unipd.dei.es.screenparty.party.PartyManager;
import it.unipd.dei.es.screenparty.party.PartyParams;

public class PrepareFragment extends Fragment {

    private NavController navController;
    private PartyManager partyManager = PartyManager.getInstance();

    private Dialogs dialogs = new Dialogs();

    private View.OnClickListener startButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            partyManager.sendMessage(new NetworkMessage(NetworkCommands.Host.NEXT));
            navController.navigate(R.id.actionToMedia);
        }
    };

    private OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            dialogs.showBackConfirmationDialog();
        }
    };

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what) {
                case NetworkEvents.Host.CLIENT_LEFT:
                    dialogs.showClientLeftDialog();
                    break;
                case NetworkEvents.Client.HOST_LEFT:
                    dialogs.showHostLeftDialog();
                    break;
                case NetworkEvents.COMMUNICATION_FAILED:
                    dialogs.showCommunicationFailedDialog((String)msg.obj);
                    break;
                case NetworkEvents.Client.HOST_NEXT:
                    navController.navigate(R.id.actionToMedia);
                    break;
                default: super.handleMessage(msg);
            }
        }
    };

    private class Dialogs {

        private void showBackConfirmationDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Warning")
                    .setMessage("Are you sure you want to go back? You'll have to start again")
                    .setPositiveButton("Cancel", null)
                    .setNegativeButton("Go back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goBack();
                        }
                    }).show();
        }

        private void showClientLeftDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Client left")
                    .setMessage("A client left the party, unfortunately you have to start again")
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            goBack();
                        }
                    })
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goBack();
                        }
                    }).show();
        }

        private void showHostLeftDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Party no long exists")
                    .setMessage("The host has left the party")
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            goBack();
                        }
                    })
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goBack();
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
                            goBack();
                        }
                    })
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goBack();
                        }
                    })
                    .show();
        }
    }

    private void goBack() {
        partyManager.stop();
        navController.popBackStack();
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
        View view = inflater.inflate(R.layout.fragment_prepare, container, false);

        Button startButton = view.findViewById(R.id.start_button);
        if(partyManager.getPartyParams().getRole() == PartyParams.Role.HOST)
            startButton.setOnClickListener(startButtonListener);
        else startButton.setVisibility(View.INVISIBLE);

        ImageView leftArrowIcon = view.findViewById(R.id.left_arrow_icon);
        ImageView rightArrowIcon = view.findViewById(R.id.right_arrow_icon);
        if(partyManager.getPartyParams().getPosition() == PartyParams.Position.LEFT)
            rightArrowIcon.setVisibility(View.INVISIBLE);
        if(partyManager.getPartyParams().getPosition() == PartyParams.Position.RIGHT)
            leftArrowIcon.setVisibility(View.INVISIBLE);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
    }
}
