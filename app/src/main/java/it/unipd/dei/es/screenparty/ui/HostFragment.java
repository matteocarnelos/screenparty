package it.unipd.dei.es.screenparty.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
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
import it.unipd.dei.es.screenparty.party.PartyEvents;
import it.unipd.dei.es.screenparty.party.PartyManager;

public class HostFragment extends Fragment {

    private TextView waitingLabel;

    private Uri mediaUri;

    private NavController navController;
    private PartyManager manager = PartyManager.getInstance();

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            switch (inputMessage.what) {
                case PartyEvents.Host.NOT_STARTED:
                    new AlertDialog.Builder(getActivity())
                            .setMessage((String)inputMessage.obj)
                            .setTitle("Could not start the server")
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    manager.stop();
                                    navController.popBackStack(R.id.startFragment, false);
                                }
                            })
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    manager.restart();
                                }
                            }).show();
                    break;
                case PartyEvents.Host.WAITING_DEVICES:
                    waitingLabel.setText(R.string.waiting_label_text2);
                    break;
                case PartyEvents.CONNECTION_FAILED:
                    new AlertDialog.Builder(getActivity())
                            .setMessage((String)inputMessage.obj)
                            .setTitle("Connection failed")
                            .setPositiveButton("OK", null)
                            .show();
                    break;
                case PartyEvents.JOIN_FAILED:
                    new AlertDialog.Builder(getActivity())
                            .setMessage((String)inputMessage.obj)
                            .setTitle("Join failed")
                            .setPositiveButton("OK", null)
                            .show();
                    break;
                case PartyEvents.Host.CLIENT_JOINED:
                    Toast.makeText(getContext(), "A device has joined the party!", Toast.LENGTH_LONG).show();
                    break;
                case PartyEvents.Host.PARTY_READY:
                    // TODO: Add partyOptions in arguments to pass
                    navController.navigate(R.id.actionToPrepare);
                    break;
                case PartyEvents.Host.CLIENT_LEFT:
                    Toast.makeText(getContext(), "A device has left the party", Toast.LENGTH_LONG).show();
                    break;
                case PartyEvents.COMMUNICATION_FAILED:
                    new AlertDialog.Builder(getActivity())
                            .setMessage((String)inputMessage.obj)
                            .setTitle("Communication failed")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    manager.stop();
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
        manager.setEventsHandler(handler);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                manager.stop();
                navController.popBackStack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_host, container, false);

        TextView invitationCodeLabel = view.findViewById(R.id.invitationCodeLabel);
        waitingLabel = view.findViewById(R.id.waitingLabel);

        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);

        manager.startAsHost(displayMetrics.widthPixels, displayMetrics.heightPixels);

        invitationCodeLabel.setText(ip);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
        if(getArguments() != null)
            mediaUri = HostFragmentArgs.fromBundle(getArguments()).getMediaUri();
    }
}
