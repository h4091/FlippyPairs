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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;


public class GameActivity extends BaseActivity implements ClientGameListener, View.OnClickListener {

	@Inject ClientGameManager clientGameManager;

	private ProgressDialog waitingForHostDialog;
	private final List<Card> cards = new ArrayList<>();

	@InjectView(R.id.cards_grid) GridLayout gridLayout;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		ButterKnife.inject(this);

		int rowCount = getResources().getInteger(R.integer.grid_row_count);
		int columnCount = getResources().getInteger(R.integer.grid_column_count);

		// fill grid layout with buttons
		for (int column = 0; column < columnCount; ++column) {
			for (int row = 0; row < rowCount; ++row) {
                Button button = new Button(this);
                button.setOnClickListener(this);
				gridLayout.addView(
						button,
						new GridLayout.LayoutParams(
								GridLayout.spec(row, 1),
								GridLayout.spec(column, 1)));
			}
		}
	}

	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}


	@Override
	public void onResume() {
		super.onResume();
		clientGameManager.registerClientGameListener(this);
		onCardsChanged();

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
		cards.addAll(clientGameManager.getClosedCards().values());
		Collections.sort(cards, new Comparator<Card>() {
			@Override
			public int compare(Card lhs, Card rhs) {
				return Integer.valueOf(lhs.getId()).compareTo(rhs.getId());
			}
		});


		int xIdx = 0, yIdx = 0;
		int columns = gridLayout.getColumnCount();
		for (Card card : cards) {
			Button button = (Button) gridLayout.getChildAt(xIdx + yIdx * columns);
			button.setTag(R.id.cardId, card.getId());
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


	@Override
	public void onCardsChanged() {
		Map<Integer, Card> matchedCards = clientGameManager.getMatchedCards();
		Map<Integer, Card> selectedCards = clientGameManager.getSelectedCards();

		// iterate over all cards and update UI accordingly
		int xIdx = 0, yIdx = 0;
		int columns = gridLayout.getColumnCount();
		for (Card card : cards) {
			Button button = (Button) gridLayout.getChildAt(xIdx + yIdx * columns);
			if (matchedCards.containsKey(card.getId()) || selectedCards.containsKey(card.getId())) {
				button.setEnabled(false);
			} else {
				button.setEnabled(true);
			}
			++xIdx;
			if (xIdx >= columns) {
				xIdx = 0;
				++yIdx;
			}
		}
	}


	@Override
	public void onCardsMatch() {
		Toast.makeText(this, "Match!", Toast.LENGTH_SHORT).show();
	}


	@Override
	public void onCardsMismatch() {
		Toast.makeText(this, "nope ...", Toast.LENGTH_SHORT).show();
	}


	@Override
	public void onClick(View view) {
		if (clientGameManager.getCurrentState() != GameState.SELECT_1ST_CARD
				&& clientGameManager.getCurrentState() != GameState.SELECT_2ND_CARD) {
			Timber.w("Dropped click request due to wrong state");
			return;
		}

		Button button = (Button) view;
		int cardId = (int) button.getTag(R.id.cardId);
		clientGameManager.selectCard(cardId);

		/*
		if(toggle) {
			second = id;
			Timber.d("selected id: " + id);
			clientGameManager.selectCard(id);
			toggle = false;
		} else {
			first = id;
			Timber.d("selected id: " + id);
			clientGameManager.selectCard(id);
			toggle = true;
		}

		if(first>=0 && second>=0) {
			if (first != second) {
				if(clientGameManager.getClosedCards().get(first).getValue()
						==clientGameManager.getClosedCards().get(second).getValue()) {
					Timber.i("Matching pair!");
				}
			} else {
				Toast errorToast = Toast.makeText(getApplicationContext(), "Invalid selection!", Toast.LENGTH_LONG);
				errorToast.show();
			}
		}
		*/
	}
}
