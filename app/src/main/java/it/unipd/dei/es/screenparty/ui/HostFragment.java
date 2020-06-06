package it.unipd.dei.es.screenparty.ui;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;

import it.unipd.dei.es.screenparty.R;
import it.unipd.dei.es.screenparty.media.MediaParams;
import it.unipd.dei.es.screenparty.network.NetworkEvents;
import it.unipd.dei.es.screenparty.party.PartyManager;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class HostFragment extends Fragment {

    private static final int SELECT_MEDIA_REQUEST_CODE = 0;

    private TextView hostIpLabel;
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
                    Toast.makeText(getContext(), "A device has joined the party!", Toast.LENGTH_LONG).show();
                    break;
                case NetworkEvents.Host.PARTY_READY:
                    navController.navigate(R.id.actionToPrepare);
                    break;
                case NetworkEvents.FILE_TRANSFER_FAILED:
                    dialogs.showFileTransferFailedDialog((String)msg.obj);
                    break;
                case NetworkEvents.Host.CLIENT_LEFT:
                    Toast.makeText(getContext(), "A device has left the party", Toast.LENGTH_LONG).show();
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
                            backPressedCallback.handleOnBackPressed();
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
                            backPressedCallback.handleOnBackPressed();
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

        private void showFileTransferFailedDialog(String message) {
            new MaterialAlertDialogBuilder((requireContext()))
                    .setTitle("File trasfer failed")
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
                    })
                    .show();
        }

        private void showMediaErrorDialog(String message) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Media error")
                    .setMessage(message)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            backPressedCallback.handleOnBackPressed();
                        }
                    })
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            openMediaPicker();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
        View view = inflater.inflate(R.layout.fragment_host, container, false);
        hostIpLabel = view.findViewById(R.id.host_ip_label);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
        openMediaPicker();
    }

    private void openMediaPicker() {
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("image/* video/*");
        fileIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});

        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/* video/*");

        Intent chooserIntent = Intent.createChooser(fileIntent, "Select the media you want to play");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { galleryIntent });

        startActivityForResult(chooserIntent, SELECT_MEDIA_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SELECT_MEDIA_REQUEST_CODE) {
            if(resultCode == RESULT_CANCELED) backPressedCallback.handleOnBackPressed();
            else if(resultCode == RESULT_OK) {
                Uri selectedUri = data.getData();
                if(selectedUri == null) return;

                ContentResolver contentResolver = requireContext().getContentResolver();
                String type = contentResolver.getType(selectedUri);
                if(type == null || !type.startsWith("image") && !type.startsWith("video")) {
                    dialogs.showMediaErrorDialog("Invalid media type, please try again with another one");
                    return;
                }
                MediaParams.Type mediaType = type.startsWith("image") ? MediaParams.Type.IMAGE : MediaParams.Type.VIDEO;

                Bitmap bitmap;
                if(mediaType == MediaParams.Type.VIDEO) {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(getContext(), selectedUri);
                    bitmap = retriever.getFrameAtTime();
                    retriever.release();
                } else {
                    try { bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedUri); }
                    catch (IOException ignored) {
                        dialogs.showMediaErrorDialog("An error occurred during file processing, please try again");
                        return;
                    }
                }
                float aspectRatio = (float)bitmap.getWidth() / (float)bitmap.getHeight();

                MediaParams mediaParams = new MediaParams(selectedUri, mediaType, aspectRatio);

                try {
                    mediaParams.setInputStream(requireContext().getContentResolver().openInputStream(selectedUri));
                } catch(FileNotFoundException e) {
                    dialogs.showMediaErrorDialog("An error occurred during file processing, please try again");
                }

                PartyManager partyManager = PartyManager.getInstance();
                partyManager.getPartyParams().setMediaParams(mediaParams);

                partyManager.startAsHost();
            }
        }
    }
}
