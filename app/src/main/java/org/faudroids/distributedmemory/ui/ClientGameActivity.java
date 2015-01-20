package org.faudroids.distributedmemory.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.core.ClientGameManager;
import org.faudroids.distributedmemory.core.HostGameManager;

import java.util.List;

import butterknife.ButterKnife;


public class ClientGameActivity extends BaseActivity {

    private int first;
    private int second;

    private Button firstBtn;
    private Button sndBtn;
    private TextView txtPlayer;
    private TextView txtPoints;

    private boolean toggle = false;

    private ClientGameManager manager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		ButterKnife.inject(this);
        GridLayout grid = (GridLayout)findViewById(R.id.gameGrid);
        int numberOfPairs = (grid.getColumnCount()*(grid.getRowCount()-1))/2;
        Log.d("onCreate", "numberOfPairs: " + numberOfPairs);
        this.manager = new ClientGameManager();
		/*
		// TODO will be fixed once the GameManager API is a little more stable ...
        this.manager = new GameManager(numberOfPairs,2);
        this.txtPlayer = (TextView)findViewById(R.id.playerText);
        txtPlayer.setText(manager.getPlayerName(manager.getCurrentPlayer()));
        this.txtPoints = (TextView)findViewById(R.id.playerPoints);
        txtPoints.setText("Points: " + manager.getPlayerPoints(manager.getCurrentPlayer()));
        this.first = -1;
        this.second = -1;
        */
	}

	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

    public void onCardSelect(View v) {
        Button b = (Button)v;
        GridLayout g = (GridLayout)b.getParent();

		/*
        int id = (int)(b.getX()/b.getWidth()+g.getColumnCount()*(int)(b.getY()/b.getHeight()));

        if(this.toggle) {
            this.second = id;
            Log.d("select", "id: "+id);
            this.sndBtn = b;
            b.setText(""+manager.getCardValue(id));
            this.toggle = false;
        } else {
            this.first = id;
            Log.d("select", "id: "+id);
            this.firstBtn = b;
            b.setText(""+manager.getCardValue(id));
            this.toggle = true;
        }

        if(first>=0 && second>=0) {
            if (first != second) {
                GameManager.gameEvents events = manager.run(first, second);
                if (events == GameManager.gameEvents.FINISH) {
                    Toast winnerToast = Toast.makeText(getApplicationContext(),
                            "Winner: " + manager.getPlayerName(manager.getWinnerId()),
                            Toast.LENGTH_LONG);
                    winnerToast.show();
                } else if (events == GameManager.gameEvents.MATCH) {
                    txtPoints.setText("Points: " + manager.getPlayerPoints(manager.getCurrentPlayer()));
                    Log.d("run", "match");
                    this.first = -1;
                    this.second = -1;
                } else if (events == GameManager.gameEvents.SWITCH) {
                    android.os.Handler handler = new android.os.Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            firstBtn.setText("?");
                            sndBtn.setText("?");
                            first = -1;
                            second = -1;
                            txtPlayer.setText(manager.getPlayerName(manager.getCurrentPlayer()));
                            txtPoints.setText("Points: " + manager.getPlayerPoints(manager.getCurrentPlayer()));
                        }
                    }, 300);
                } else if (events == GameManager.gameEvents.ERROR) {
                    Log.e("Error", "An error occured!");
                }
            } else {
                Toast errorToast = Toast.makeText(getApplicationContext(), "Invalid selection!", Toast.LENGTH_LONG);
                errorToast.show();
            }
        }
        */
    }
}
