package org.faudroids.distributedmemory.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.core.GameManager;

import java.util.List;

import butterknife.ButterKnife;


public class GameActivity extends BaseActivity {

    private int first;
    private int second;

    private Button firstBtn;
    private Button sndBtn;
    private TextView txtPlayer;

    private boolean toggle = false;

    private GameManager manager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		ButterKnife.inject(this);
        GridLayout grid = (GridLayout)findViewById(R.id.gameGrid);
        int numberOfPairs = (grid.getColumnCount()*(grid.getRowCount()-1))/2;
        Log.d("onCreate", "numberOfPairs: " + numberOfPairs);
        this.manager = new GameManager(numberOfPairs,2);
        this.txtPlayer = (TextView)findViewById(R.id.playerText);
        txtPlayer.setText(manager.getPlayerName(manager.getCurrentPlayer()));
	}

	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

    public void onCardSelect(View v) {
        Button b = (Button)v;
        GridLayout g = (GridLayout)b.getParent();

        int id = (int)(b.getX()/b.getWidth()+g.getColumnCount()*(int)(b.getY()/b.getHeight()));

        if(this.toggle) {
            this.second = id;
            this.sndBtn = b;
            b.setText(""+manager.getCardValue(id));
            this.toggle = false;
        } else {
            this.first = id;
            this.firstBtn = b;
            b.setText(""+manager.getCardValue(id));
            this.toggle = true;
        }

        if(first>0 && second>0) {
            GameManager.gameEvents events = manager.run(first, second);
            if(events == GameManager.gameEvents.FINISH) {
                Toast winnerToast = Toast.makeText(getApplicationContext(),
                        "Winner: " + manager.getPlayerName(manager.getWinnerId()),
                        Toast.LENGTH_LONG);
                winnerToast.show();
            } else if(events == GameManager.gameEvents.MATCH) {
                Log.d("run", "match");
                this.first = 0;
                this.second = 0;
            } else if(events == GameManager.gameEvents.SWITCH) {
                this.firstBtn.setText("?");
                this.sndBtn.setText("?");
                this.first = 0;
                this.second = 0;
                this.txtPlayer.setText(manager.getPlayerName(manager.getCurrentPlayer()));
            } else if(events == GameManager.gameEvents.ERROR) {
                Log.e("Error", "An error occured!");
            }
        }
    }
}
