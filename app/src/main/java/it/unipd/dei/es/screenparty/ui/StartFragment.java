package it.unipd.dei.es.screenparty.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import org.jetbrains.annotations.NotNull;

import it.unipd.dei.es.screenparty.R;

/**
 * Manage the StartFragment fragment.
 */
public class StartFragment extends Fragment {

    private NavController navController;

    /**
     * Listen for the hostButton {@link Button} to be pressed. When this happens, it navigates
     * to the {@link HostFragment} fragment.
     */
    private View.OnClickListener hostButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.actionToHost);
        }
    };

    /**
     * Listen for the joinButton {@link Button} to be pressed. When this happens, it navigates
     * to the {@link ClientFragment} fragment.
     */
    private View.OnClickListener joinButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.actionToClient);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);

        Button hostButton = view.findViewById(R.id.host_button);
        Button joinButton = view.findViewById(R.id.join_button);

        hostButton.setOnClickListener(hostButtonListener);
        joinButton.setOnClickListener(joinButtonListener);
    }
}
