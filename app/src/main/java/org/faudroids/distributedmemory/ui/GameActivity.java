package org.faudroids.distributedmemory.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.core.Card;
import org.faudroids.distributedmemory.core.ClientGameListener;
import org.faudroids.distributedmemory.core.ClientGameManager;
import org.faudroids.distributedmemory.core.GameState;
import org.faudroids.distributedmemory.core.HostGameManager;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;


public class GameActivity extends BaseActivity implements ClientGameListener {

	@Inject ClientGameManager clientGameManager;

	private ProgressDialog waitingForHostDialog;
	private final TreeSet<Card> cards = new TreeSet<>(new Comparator<Card>() {
		@Override
		public int compare(Card lhs, Card rhs) {
			return Integer.valueOf(lhs.getId()).compareTo(rhs.getId());
		}
	});

	@InjectView(R.id.cards_grid) GridLayout gridLayout;

    private int first = -1;
    private int second = -1;

    private Button firstBtn;
    private Button sndBtn;

    private boolean toggle = false;

    private HostGameManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		ButterKnife.inject(this);

		// fill grid layout with buttons
		for (int column = 0; column < getResources().getInteger(R.integer.grid_column_count); ++column) {
			for (int row = 0; row < getResources().getInteger(R.integer.grid_row_count); ++row) {
                Button btn = new Button(this);
                btn.setOnClickListener(onCardSelect);
				gridLayout.addView(
						btn,
						new GridLayout.LayoutParams(
								GridLayout.spec(row, 1),
								GridLayout.spec(column, 1)));
			}
		}

		/*
        GridLayout grid = (GridLayout)findViewById(R.id.gameGrid);
        int numberOfPairs = (grid.getColumnCount()*(grid.getRowCount()-1))/2;
        Log.d("onCreate", "numberOfPairs: " + numberOfPairs);
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

    private Button.OnClickListener onCardSelect = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            Button b = (Button)v;
            GridLayout g = (GridLayout)b.getParent();

            //int id = (int)(b.getY()/b.getHeight()+g.getRowCount()*(int)(b.getX()/b.getWidth()));
            int id = (int)(b.getY()/(g.getHeight()/g.getRowCount())+g.getRowCount()*(int)(b.getX
                    ()/(g.getWidth()/g.getColumnCount())));

            if(toggle) {
                second = id;
                Timber.d("selected id: " + id);
                clientGameManager.selectCard(id);
                sndBtn = b;
                //b.setText(""+manager.getCardValue(id));
                toggle = false;
            } else {
                first = id;
                Timber.d("selected id: " + id);
                clientGameManager.selectCard(id);
                firstBtn = b;
                //b.setText(""+manager.getCardValue(id));
                toggle = true;
            }

            if(first>=0 && second>=0) {
                if (first != second) {
                    if(clientGameManager.getClosedCards().get(first).getValue()
                            ==clientGameManager.getClosedCards().get(second).getValue()) {
                        Timber.i("Matching pair!");
                    }
//                GameManager.gameEvents events = manager.run(first, second);
//                if (events == GameManager.gameEvents.FINISH) {
//                    Toast winnerToast = Toast.makeText(getApplicationContext(),
//                            "Winner: " + manager.getPlayerName(manager.getWinnerId()),
//                            Toast.LENGTH_LONG);
//                    winnerToast.show();
//                } else if (events == GameManager.gameEvents.MATCH) {
//                    Log.d("run", "match");
//                    this.first = -1;
//                    this.second = -1;
//                } else if (events == GameManager.gameEvents.SWITCH) {
//                    android.os.Handler handler = new android.os.Handler();
//                    handler.postDelayed(new Runnable() {
//                        public void run() {
//                            firstBtn.setText("?");
//                            sndBtn.setText("?");
//                            first = -1;
//                            second = -1;
//                            txtPlayer.setText(manager.getPlayerName(manager.getCurrentPlayer()));
//                            txtPoints.setText("Points: " + manager.getPlayerPoints(manager.getCurrentPlayer()));
//                        }
//                    }, 300);
//                } else if (events == GameManager.gameEvents.ERROR) {
//                    Log.e("Error", "An error occured!");
//                }
                } else {
                    Toast errorToast = Toast.makeText(getApplicationContext(), "Invalid selection!", Toast.LENGTH_LONG);
                    errorToast.show();
                }
            }

        }
    };

//    void onCardSelect(View v) {
//        Button b = (Button)v;
//        GridLayout g = (GridLayout)b.getParent();
//
//        int id = (int)(b.getX()/b.getWidth()+g.getColumnCount()*(int)(b.getY()/b.getHeight()));
//
//        if(this.toggle) {
//            this.second = id;
//            Timber.d("select", "id: " + id);
//            this.sndBtn = b;
//            //b.setText(""+manager.getCardValue(id));
//            this.toggle = false;
//        } else {
//            this.first = id;
//            Timber.d("select", "id: " + id);
//            this.firstBtn = b;
//            //b.setText(""+manager.getCardValue(id));
//            this.toggle = true;
//        }
//
//        if(first>=0 && second>=0) {
//            if (first != second) {
//                GameManager.gameEvents events = manager.run(first, second);
//                if (events == GameManager.gameEvents.FINISH) {
//                    Toast winnerToast = Toast.makeText(getApplicationContext(),
//                            "Winner: " + manager.getPlayerName(manager.getWinnerId()),
//                            Toast.LENGTH_LONG);
//                    winnerToast.show();
//                } else if (events == GameManager.gameEvents.MATCH) {
//                    Log.d("run", "match");
//                    this.first = -1;
//                    this.second = -1;
//                } else if (events == GameManager.gameEvents.SWITCH) {
//                    android.os.Handler handler = new android.os.Handler();
//                    handler.postDelayed(new Runnable() {
//                        public void run() {
//                            firstBtn.setText("?");
//                            sndBtn.setText("?");
//                            first = -1;
//                            second = -1;
//                            txtPlayer.setText(manager.getPlayerName(manager.getCurrentPlayer()));
//                            txtPoints.setText("Points: " + manager.getPlayerPoints(manager.getCurrentPlayer()));
//                        }
//                    }, 300);
//                } else if (events == GameManager.gameEvents.ERROR) {
//                    Log.e("Error", "An error occured!");
//                }
//            } else {
//                Toast errorToast = Toast.makeText(getApplicationContext(), "Invalid selection!", Toast.LENGTH_LONG);
//                errorToast.show();
//            }
//        }
//    }


	@Override
	public void onResume() {
		super.onResume();
		clientGameManager.registerClientGameListener(this);

		if (clientGameManager.getCurrentState() == GameState.SETUP
				|| clientGameManager.getCurrentState() == GameState.CONNECTING) {

			waitingForHostDialog = ProgressDialog.show(
					this,
					"Waiting for host",
					"Waiting for host to start the game. Maybe giving him a cookie will make him work faster?",
					false);
		}
	}

	@Override
	public void onPause() {
		clientGameManager.unregisterClientGameListener();
		super.onPause();
	}

	@Override
	public void onGameStarted() {
		// setup cards
		cards.clear();
		cards.addAll(clientGameManager.getClosedCards());

		int xIdx = 0, yIdx = 0;
		int columns = gridLayout.getColumnCount();
		for (Card card : cards) {
			Button button = (Button) gridLayout.getChildAt(xIdx + yIdx * columns);
			button.setText(card.getValue() + " (" + card.getId() + ")");
			++xIdx;
			if (xIdx >= columns) {
				xIdx = 0;
				++yIdx;
			}
		}

		// cancel waiting dialog
		waitingForHostDialog.cancel();
		waitingForHostDialog = null;
	}

}
