package it.unipd.dei.es.screenparty;

import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatActivity;

import it.unipd.dei.es.screenparty.party.PartyManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        float width = displayMetrics.widthPixels / displayMetrics.xdpi;
        float height = displayMetrics.heightPixels / displayMetrics.ydpi;

        PartyManager partyManager = PartyManager.getInstance();
        partyManager.init(width, height, displayMetrics.xdpi, displayMetrics.ydpi);
    }
}
