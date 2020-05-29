package it.unipd.dei.es.screenparty.ui;

import android.content.Intent;
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

import it.unipd.dei.es.screenparty.R;

import static android.app.Activity.RESULT_OK;

public class StartFragment extends Fragment {

    private static final int SELECT_MEDIA_REQUEST_CODE = 0;

    private NavController navController;

    private View.OnClickListener hostButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/* video/*");
            startActivityForResult(intent, SELECT_MEDIA_REQUEST_CODE);
        }
    };

    private View.OnClickListener joinButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.actionToClient);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SELECT_MEDIA_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri selectedMediaUri = data.getData();
            if(selectedMediaUri != null) {
                StartFragmentDirections.ActionToHost action = StartFragmentDirections.actionToHost(selectedMediaUri);
                navController.navigate(action);
            }
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

        Button hostButton = view.findViewById(R.id.hostButton);
        Button joinButton = view.findViewById(R.id.joinButton);

        hostButton.setOnClickListener(hostButtonListener);
        joinButton.setOnClickListener(joinButtonListener);
    }
}
