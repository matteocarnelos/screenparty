package it.unipd.dei.es.screenparty.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

public class HostFragment extends Fragment {

    private TextView waitingLabel;

    private NavController navController;
    private PartyManager partyManager = PartyManager.getInstance();

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what) {
                case NetworkEvents.Host.NOT_STARTED:
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Could not start the server")
                            .setMessage((String)msg.obj)
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    partyManager.restart();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    partyManager.stop();
                                    navController.popBackStack(R.id.startFragment, false);
                                }
                            }).show();
                    break;
                case NetworkEvents.Host.WAITING_DEVICES:
                    waitingLabel.setText(R.string.waiting_label_text2);
                    break;
                case NetworkEvents.CONNECTION_FAILED:
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Connection failed")
                            .setMessage((String)msg.obj)
                            .setPositiveButton("OK", null)
                            .show();
                    break;
                case NetworkEvents.JOIN_FAILED:
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Join failed")
                            .setMessage((String)msg.obj)
                            .setPositiveButton("OK", null)
                            .show();
                    break;
                case NetworkEvents.Host.CLIENT_JOINED:
                    Toast.makeText(getContext(), "A device has joined the party!", Toast.LENGTH_LONG).show();
                    break;
                case NetworkEvents.Host.PARTY_READY:
                    navController.navigate(R.id.actionToPrepare);
                    break;
                case NetworkEvents.Host.CLIENT_LEFT:
                    Toast.makeText(getContext(), "A device has left the party", Toast.LENGTH_LONG).show();
                    break;
                case NetworkEvents.COMMUNICATION_FAILED:
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Communication failed")
                            .setMessage((String)msg.obj)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    partyManager.stop();
                                    navController.popBackStack(R.id.startFragment, false);
                                }
                            })
                            .show();
                    break;
                default: super.handleMessage(msg);
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
        View view = inflater.inflate(R.layout.fragment_host, container, false);

        TextView invitationCodeLabel = view.findViewById(R.id.invitationCodeLabel);
        waitingLabel = view.findViewById(R.id.waitingLabel);

        WifiManager wifiManager = (WifiManager) requireActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());

        partyManager.startAsHost();

        invitationCodeLabel.setText(ip);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
    }
}
