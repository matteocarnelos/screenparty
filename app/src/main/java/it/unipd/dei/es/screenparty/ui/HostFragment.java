package it.unipd.dei.es.screenparty.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.unipd.dei.es.screenparty.R;
import it.unipd.dei.es.screenparty.media.MediaParams;
import it.unipd.dei.es.screenparty.media.MediaUtils;
import it.unipd.dei.es.screenparty.network.ConnectedClient;
import it.unipd.dei.es.screenparty.network.NetworkCommands;
import it.unipd.dei.es.screenparty.network.NetworkEvents;
import it.unipd.dei.es.screenparty.network.NetworkMessage;
import it.unipd.dei.es.screenparty.party.PartyManager;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class HostFragment extends Fragment {

    private TextView hostIpLabel;
    private List<ImageView> deviceIcons = new ArrayList<>();
    private List<ProgressBar> deviceSpinners = new ArrayList<>();
    private List<TextView> deviceNameLabels = new ArrayList<>();
    private List<TextView> deviceInfoLabels = new ArrayList<>();
    private List<ImageView> deviceConnectedIcons = new ArrayList<>();
    private Button nextButton;
    private Dialogs dialogs = new Dialogs();

    private NavController navController;
    private PartyManager partyManager = PartyManager.getInstance();

    private boolean partyReady = false;

    private View.OnClickListener nextButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            partyManager.sendMessage(new NetworkMessage(NetworkCommands.Host.NEXT));
            navController.navigate(R.id.actionToPrepare);
        }
    };

    private OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if(partyReady) dialogs.showBackConfirmationDialog();
            else goBack();
        }
    };

    private void goBack() {
        partyManager.stop();
        navController.popBackStack();
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what) {
                case NetworkEvents.Host.NOT_STARTED:
                    dialogs.showNotStartedDialog((String)msg.obj);
                    break;
                case NetworkEvents.Host.WAITING_DEVICES:
                    hostIpLabel.setText((String)msg.obj);
                    break;
                case NetworkEvents.CONNECTION_FAILED:
                    dialogs.showConnectionFailedDialog((String)msg.obj);
                    break;
                case NetworkEvents.JOIN_FAILED:
                    dialogs.showJoinFailedDialog((String)msg.obj);
                    break;
                case NetworkEvents.Host.CLIENT_JOINED:
                    clientListChanged((List<ConnectedClient>)msg.obj);
                    break;
                case NetworkEvents.Host.PARTY_READY:
                    partyReady = true;
                    for(ImageView deviceIcon : deviceIcons)
                        deviceIcon.setVisibility(View.VISIBLE);
                    for(ProgressBar deviceSpinner : deviceSpinners)
                        deviceSpinner.setVisibility(View.INVISIBLE);
                    for(TextView deviceInfoLabel : deviceInfoLabels)
                        deviceInfoLabel.setText("Ready");
                    for(ImageView deviceConnectedIcon : deviceConnectedIcons)
                        deviceConnectedIcon.setVisibility(View.VISIBLE);
                    nextButton.setEnabled(true);
                    break;
                case NetworkEvents.Host.CLIENT_LEFT:
                    nextButton.setEnabled(false);
                    if(partyReady) dialogs.showClientLeftDialog();
                    else clientListChanged((List<ConnectedClient>)msg.obj);
                    break;
                case NetworkEvents.COMMUNICATION_FAILED:
                    dialogs.showCommunicationFailedDialog((String)msg.obj);
                    break;
                default: super.handleMessage(msg);
            }
        }
    };

    private class Dialogs {

        private void showNotStartedDialog(String message) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Could not start the server")
                    .setMessage(message)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            goBack();
                        }
                    })
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            partyManager.restart();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goBack();
                        }
                    }).show();
        }

        private void showConnectionFailedDialog(String message) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Connection failed")
                    .setMessage(message)
                    .setPositiveButton("Ok", null)
                    .show();
        }

        private void showJoinFailedDialog(String message) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Join failed")
                    .setMessage(message)
                    .setPositiveButton("Ok", null)
                    .show();
        }

        private void showClientLeftDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Client left")
                    .setMessage("A client left the party, unfortunately you have to start again")
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            partyManager.restart();
                            clientListChanged(new ArrayList<ConnectedClient>());
                        }
                    })
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            partyManager.restart();
                            clientListChanged(new ArrayList<ConnectedClient>());
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
    }

    private void clientListChanged(List<ConnectedClient> clients) {
        for(int i = 1; i <= 2; i++) setCardWaiting(i);
        for(int i = 0; i < clients.size(); i++)
            setCardReady(i + 1, clients.get(i).getDeviceName());
    }

    private void setCardReady(int index, String name) {
        deviceNameLabels.get(index).setText(name);
        deviceInfoLabels.get(index).setText("Ready");
        deviceSpinners.get(index).setVisibility(View.INVISIBLE);
        deviceIcons.get(index).setVisibility(View.VISIBLE);
        deviceConnectedIcons.get(index).setVisibility(View.VISIBLE);
    }

    private void setCardWaiting(int index) {
        deviceNameLabels.get(index).setText("");
        deviceInfoLabels.get(index).setText("");
        deviceSpinners.get(index).setVisibility(View.VISIBLE);
        deviceIcons.get(index).setVisibility(View.INVISIBLE);
        deviceConnectedIcons.get(index).setVisibility(View.INVISIBLE);
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
        View view = inflater.inflate(R.layout.fragment_host, container, false);
        hostIpLabel = view.findViewById(R.id.host_ip_label);
        deviceIcons.addAll(Arrays.asList(new ImageView[]{
                view.findViewById(R.id.host_icon),
                view.findViewById(R.id.client1_icon),
                view.findViewById(R.id.client2_icon)
        }));
        deviceSpinners.addAll(Arrays.asList(new ProgressBar[]{
                view.findViewById(R.id.host_spinner),
                view.findViewById(R.id.client1_spinner),
                view.findViewById(R.id.client2_spinner)
        }));
        deviceNameLabels.addAll(Arrays.asList(new TextView[]{
                view.findViewById(R.id.host_name_label),
                view.findViewById(R.id.client1_name_label),
                view.findViewById(R.id.client2_name_label)
        }));
        deviceInfoLabels.addAll(Arrays.asList(new TextView[]{
                view.findViewById(R.id.host_info_label),
                view.findViewById(R.id.client1_info_label),
                view.findViewById(R.id.client2_info_label)
        }));
        deviceConnectedIcons.addAll(Arrays.asList(new ImageView[]{
                view.findViewById(R.id.host_connected_icon),
                view.findViewById(R.id.client1_connected_icon),
                view.findViewById(R.id.client2_connected_icon)
        }));
        nextButton = view.findViewById(R.id.next_button);
        nextButton.setOnClickListener(nextButtonClickListener);
        String deviceName = Settings.Secure.getString(requireActivity().getContentResolver(), "bluetooth_name");
        setCardReady(0, deviceName);
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

                PartyManager partyManager = PartyManager.getInstance();
                partyManager.getPartyParams().setMediaParams(mediaParams);

                partyManager.startAsHost();
            }
        }
    }
}
