package it.unipd.dei.es.screenparty.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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
                    .setTitle(R.string.dialog_title_warning)
                    .setMessage(R.string.dialog_message_warning)
                    .setPositiveButton(R.string.dialog_button_cancel, null)
                    .setNegativeButton(R.string.dialog_button_go_back, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goBack();
                        }
                    }).show();
        }

        private void showClientLeftDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_client_left)
                    .setMessage(R.string.dialog_message_client_left)
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

        private void showHostLeftDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_party_no_longer_exists)
                    .setMessage(R.string.dialog_message_party_no_longer_exists)
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
    }

    public int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId > 0) return getResources().getDimensionPixelSize(resourceId);
        return 0;
    }

    public int getNavigationBarHeight() {
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if(resourceId > 0) return getResources().getDimensionPixelSize(resourceId);
        return 0;
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
        ImageView alignmentIcon = view.findViewById(R.id.alignment_icon);

        alignmentIcon.setImageResource(R.drawable.alignment_center);
        if(partyManager.getPartyParams().getPosition() == PartyParams.Position.LEFT) {
            rightArrowIcon.setVisibility(View.INVISIBLE);
            alignmentIcon.setImageResource(R.drawable.alignment_left);
        }
        if(partyManager.getPartyParams().getPosition() == PartyParams.Position.RIGHT) {
            leftArrowIcon.setVisibility(View.INVISIBLE);
            alignmentIcon.setImageResource(R.drawable.alignment_right);
        }

        ConstraintLayout.LayoutParams leftArrowLayoutParams = (ConstraintLayout.LayoutParams)leftArrowIcon.getLayoutParams();
        leftArrowLayoutParams.bottomMargin += getStatusBarHeight();
        leftArrowLayoutParams.topMargin += getNavigationBarHeight();
        //leftArrowIcon.setLayoutParams(leftArrowLayoutParams);

        ConstraintLayout.LayoutParams rightArrowLayoutParams = (ConstraintLayout.LayoutParams)rightArrowIcon.getLayoutParams();
        rightArrowLayoutParams.bottomMargin += getStatusBarHeight();
        rightArrowLayoutParams.topMargin += getNavigationBarHeight();
        //rightArrowIcon.setLayoutParams(rightArrowLayoutParams);

        Log.d("SCREENPARTY_DIMENS", "Nav bar dimen: " + getNavigationBarHeight());
        Log.d("SCREENPARTY_DIMENS", "Stat bar dimen: " + getStatusBarHeight());

        Log.d("SCREENPARTY_DIMENS", "Left arrow layout params (bottom margin): " + leftArrowLayoutParams.bottomMargin);
        Log.d("SCREENPARTY_DIMENS", "Left arrow layout params (top margin): " + leftArrowLayoutParams.topMargin);

        Log.d("SCREENPARTY_DIMENS", "Right arrow layout params (bottom margin): " + rightArrowLayoutParams.bottomMargin);
        Log.d("SCREENPARTY_DIMENS", "Right arrow layout params (top margin): " + rightArrowLayoutParams.topMargin);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
    }
}
