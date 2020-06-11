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

import org.jetbrains.annotations.NotNull;

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
import it.unipd.dei.es.screenparty.party.PartyUtils;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Manages the HostFragment fragment.
 */
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

    /**
     * Manages the event of the back button being pressed.
     */
    private OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if(partyManager.getPartyParams().isPartyReady()) dialogs.showBackConfirmationDialog();
            else goBack();
        }
    };

    /**
     * Used to navigate back to the previous fragment ({@link StartFragment}) upon pressing the back
     * button on the navigation bar or in the top left corner of the screen.
     */
    private void goBack() {
        partyManager.stop();
        navController.popBackStack();
    }

    /**
     * Listen for the nextButton {@link Button} to be pressed.
     * When this happens it navigates to the next fragment ({@link PrepareFragment}).
     */
    private View.OnClickListener nextButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            partyManager.sendMessage(new NetworkMessage(NetworkCommands.Host.NEXT));
            navController.navigate(R.id.actionToPrepare);
        }
    };

    /**
     * Updates the state of the {@link HostFragment} accordingly to the {@link NetworkEvents}
     * related to the received {@link Message}.
     */
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what) {
                case NetworkEvents.Host.NOT_STARTED:
                    dialogs.showNotStartedDialog((String)msg.obj);
                    break;
                case NetworkEvents.Host.WAITING_DEVICES:
                    String ip = (String)msg.obj;
                    if(ip == null) dialogs.showInvalidIPDialog();
                    else hostIpLabel.setText((String)msg.obj);
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
                    for(ImageView deviceIcon : deviceIcons)
                        deviceIcon.setVisibility(View.VISIBLE);
                    for(ProgressBar deviceSpinner : deviceSpinners)
                        deviceSpinner.setVisibility(View.INVISIBLE);
                    for(TextView deviceInfoLabel : deviceInfoLabels)
                        deviceInfoLabel.setText(R.string.device_info_label_ready);
                    for(ImageView deviceConnectedIcon : deviceConnectedIcons)
                        deviceConnectedIcon.setVisibility(View.VISIBLE);
                    nextButton.setEnabled(true);
                    break;
                case NetworkEvents.Host.CLIENT_LEFT:
                    nextButton.setEnabled(false);
                    if(partyManager.getPartyParams().isPartyReady()) {
                        partyManager.restart();
                        clientListChanged(new ArrayList<ConnectedClient>());
                        dialogs.showClientLeftDialog();
                    }
                    else clientListChanged((List<ConnectedClient>)msg.obj);
                    break;
                case NetworkEvents.COMMUNICATION_FAILED:
                    dialogs.showCommunicationFailedDialog((String)msg.obj);
                    break;
                default: super.handleMessage(msg);
            }
        }
    };

    /**
     * Manages the Dialog's windows.
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
         * Shows the "Could not start the server" dialog window.
         * @param message: The message to be displayed in the dialog window.
         */
        private void showNotStartedDialog(String message) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_could_not_start_the_server)
                    .setMessage(message)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            goBack();
                        }
                    })
                    .setPositiveButton(R.string.dialog_button_retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            partyManager.restart();
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
         * Shows the "Invalid IP Address" dialog window.
         */
        private void showInvalidIPDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_invalid_ip)
                    .setMessage(R.string.dialog_message_invalid_ip)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            goBack();
                        }
                    })
                    .setPositiveButton(R.string.dialog_button_open_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goBack();
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
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
         * Shows the "Connection failed" dialog window.
         * @param message: The message to be displayed in the dialog window.
         */
        private void showConnectionFailedDialog(String message) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_communication_failed)
                    .setMessage(message)
                    .setPositiveButton(R.string.dialog_button_ok, null)
                    .show();
        }

        /**
         * Shows the "Join Failed" dialog window.
         * @param message: The message to be displayed in the dialog window.
         */
        private void showJoinFailedDialog(String message) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_join_failed)
                    .setMessage(message)
                    .setPositiveButton(R.string.dialog_button_ok, null)
                    .show();
        }

        /**
         * Shows the "Client left" dialog window.
         */
        private void showClientLeftDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_client_left)
                    .setMessage(R.string.dialog_message_client_left)
                    .setPositiveButton(R.string.dialog_button_ok, null).show();
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
                            goBack();
                        }
                    })
                    .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goBack();
                        }
                    })
                    .show();
        }

        /**
         * Shows the "Back confirmation" dialog window.
         */
        private void showBackConfirmationDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_back_confirmation)
                    .setMessage(R.string.dialog_message_back_confirmation)
                    .setPositiveButton(R.string.dialog_button_cancel, null)
                    .setNegativeButton(R.string.dialog_button_quit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goBack();
                        }
                    }).show();
        }
    }

    /**
     * Updates the list of the connected devices when a new device is connected.
     * @param clients: list of the connected devices.
     */
    private void clientListChanged(List<ConnectedClient> clients) {
        for(int i = 1; i <= 2; i++) setCardWaiting(i);
        for(int i = 0; i < clients.size(); i++)
            setCardReady(i + 1, clients.get(i).getDeviceName());
    }

    /**
     * Set the state of the device card as ready, that is:
     * - The label of the {@link TextView} at the index {@param index} of the deviceNameLabels
     *   {@link List<TextView>} shows the name of the connected device.
     * - The label of the {@link TextView} at the index {@param index} of the deviceInfoLabels
     *   {@link List<TextView>} shows "Ready".
     * - The visibility of the {@link ProgressBar} at the index {@param index} of the
     *   deviceSpinners {@link List<ProgressBar>} is set as not visible.
     * - The visibility of the {@link ImageView} at the index {@param index} of the deviceIcons
     *   {@link List<ImageView>} is set as visible.
     * - The visibility of the {@link ImageView} at the index {@param index} of the
     *   deviceConnectedIcons {@link List<ImageView>} is set as visible.
     * @param index: The index of the connected device in the deviceNameLabels
     * {@link List<TextView>}.
     * @param name: The name of the connected device.
     */
    private void setCardReady(int index, String name) {
        deviceNameLabels.get(index).setText(name);
        deviceInfoLabels.get(index).setText(R.string.device_info_label_ready);
        deviceSpinners.get(index).setVisibility(View.INVISIBLE);
        deviceIcons.get(index).setVisibility(View.VISIBLE);
        deviceConnectedIcons.get(index).setVisibility(View.VISIBLE);
    }

    /**
     * Set the state of the device card as waiting, that is:
     * - The label of the {@link TextView} at the index {@param index} of the deviceNameLabels
     *   {@link List<TextView>} shows the default text "Waiting".
     * - The label of the {@link TextView} at the index {@param index} of the deviceInfoLabels
     *   {@link List<TextView>} shows the default text "".
     * - The visibility of the {@link ProgressBar} at the index {@param index} of the deviceSpinners
     *   {@link List<ProgressBar>} is set as visible.
     * - The visibility of the {@link ImageView} at the index {@param index} of the deviceIcons
     *   {@link List<ImageView>} is set as not visible.
     * - The visibility of the {@link ImageView} at the index {@param index} of the
     *   deviceConnectedIcons {@link List<ImageView>} is set as not visible.
     * @param index: The index of the connected device in the deviceNameLabels
     * {@link List<TextView>}.
     */
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
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
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

        String deviceName = PartyUtils.getDeviceName(requireActivity().getContentResolver());
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
                if(selectedUri == null) dialogs.showInvalidUriDialog();

                MediaParams mediaParams = MediaUtils.analyzeMedia(requireContext(), selectedUri);

                PartyManager partyManager = PartyManager.getInstance();
                partyManager.getPartyParams().setMediaParams(mediaParams);

                partyManager.startAsHost();
            }
        }
    }
}
