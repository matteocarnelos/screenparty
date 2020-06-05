package it.unipd.dei.es.screenparty.ui;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.io.IOException;

import it.unipd.dei.es.screenparty.R;
import it.unipd.dei.es.screenparty.media.MediaParams;
import it.unipd.dei.es.screenparty.party.PartyManager;

import static android.app.Activity.RESULT_OK;

public class StartFragment extends Fragment {

    private static final int SELECT_MEDIA_REQUEST_CODE = 0;

    private NavController navController;

    private Button hostButton;

    private View.OnClickListener hostButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("video/*");
            Intent pickIntent = new Intent(Intent.ACTION_PICK);
            pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "video/*");
            Intent chooserIntent = Intent.createChooser(getIntent, "Select the media you want to display");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
            startActivityForResult(chooserIntent, SELECT_MEDIA_REQUEST_CODE);
        }
    };

    private View.OnClickListener joinButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.actionToClient);
        }
    };

    private void showMediaErrorDialog(String message) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Media error")
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hostButton.performClick();
                    }
                }).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SELECT_MEDIA_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri selectedUri = data.getData();
            if(selectedUri == null) return;

            ContentResolver contentResolver = requireContext().getContentResolver();
            String type = contentResolver.getType(selectedUri);
            if(type == null || !type.startsWith("image") && !type.startsWith("video")) {
                showMediaErrorDialog("Invalid media type, please try again with another one");
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
                    showMediaErrorDialog("An error occurred during file reading, please try again");
                    return;
                }
            }
            float aspectRatio = (float)bitmap.getWidth() / (float)bitmap.getHeight();

            MediaParams mediaParams = new MediaParams(selectedUri, mediaType, aspectRatio);

            PartyManager partyManager = PartyManager.getInstance();
            partyManager.getPartyParams().setMediaParams(mediaParams);

            navController.navigate(R.id.actionToHost);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);

        hostButton = view.findViewById(R.id.hostButton);
        Button joinButton = view.findViewById(R.id.joinButton);

        hostButton.setOnClickListener(hostButtonListener);
        joinButton.setOnClickListener(joinButtonListener);
    }
}
