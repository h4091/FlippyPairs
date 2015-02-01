package org.faudroids.distributedmemory.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.core.Card;
import org.faudroids.distributedmemory.core.ClientGameListener;
import org.faudroids.distributedmemory.core.ClientGameManager;
import org.faudroids.distributedmemory.core.GameState;
import org.faudroids.distributedmemory.core.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;


public class GameActivity extends BaseActivity implements ClientGameListener, View.OnClickListener {

	private static final String FILE_NAME_CARD_BACK = "card_back.png";

	@Inject ClientGameManager clientGameManager;
	@Inject BitmapCache bitmapCache;

	private ProgressDialog waitingForHostDialog;
	private final List<Card> cards = new ArrayList<>();

	@InjectView(R.id.cards_grid) TableLayout tableLayout;

	private boolean animationRunning = false;
	private Handler handler;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
        setTitle(R.string.app_name);
		ButterKnife.inject(this);
		handler = new Handler(Looper.getMainLooper());

		int rowCount = getResources().getInteger(R.integer.grid_row_count);
		int columnCount = getResources().getInteger(R.integer.grid_column_count);

		// fill grid layout with buttons
		TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0, 1);
		for (int row = 0; row < rowCount; ++row) {
			TableRow tableRow = new TableRow(this);
			tableRow.setLayoutParams(tableLayoutParams);
			for (int column = 0; column < columnCount; ++column) {
				ImageButton button = new ImageButton(this);
				button.setScaleType(ImageView.ScaleType.FIT_CENTER);
				button.setOnClickListener(this);
				button.setAdjustViewBounds(true);
				button.setBackgroundResource(0);
				tableRow.addView(button);
			}
			tableLayout.addView(tableRow);
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
		refreshAllCards();

        String currentPlayerName;
        currentPlayerName = clientGameManager.getPlayers().get(clientGameManager
                .getCurrentPlayerIdx()).getName();
        setTitle(currentPlayerName + "'s turn");

		if (clientGameManager.getCurrentState() == GameState.SETUP
				|| clientGameManager.getCurrentState() == GameState.CONNECTING) {

                ProgressDialog.show(
					this,
					"Waiting for host",
					"Waiting for host to start the game. Maybe giving him a cookie will make him work faster?",
					false, true,
                        new DialogInterface.OnCancelListener(){
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            clientGameManager.stopGame();
                            Intent intent = new Intent(GameActivity.this, JoinGameActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
		}
	}


	@Override
	public void onPause() {
		clientGameManager.unregisterClientGameListener(this);
		super.onPause();
	}


	@Override
	public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_game_back_pressed_title)
                .setMessage(R.string.activity_game_back_pressed_message)
                .setPositiveButton(R.string.btn_continue, null)
                .setNegativeButton(R.string.btn_stop, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clientGameManager.stopGame();
                        finish();
                    }
                })
                .show();
	}


	@Override
	public void onGameStarted() {
		// setup cards
		refreshAllCards();
		// cancel waiting dialog
		waitingForHostDialog.cancel();
		waitingForHostDialog = null;
        //Update actionbar
        String currentPlayerName;
        currentPlayerName = clientGameManager.getPlayers().get(clientGameManager
                .getCurrentPlayerIdx()).getName();
        setTitle(currentPlayerName);
	}

	@Override
	public void onCardsMatch(Collection<Card> matchedCards) {
	}


	@Override
	public void onCardsMismatch(final Collection<Card> mismatchedCards) {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				for (Card card : mismatchedCards) {
					for (int row = 0; row < tableLayout.getChildCount(); ++row) {
						TableRow rowView = (TableRow) tableLayout.getChildAt(row);
						for (int column = 0; column < rowView.getChildCount(); ++column) {
							ImageButton button = (ImageButton) rowView.getChildAt(column);
							if (button.getTag(R.id.cardId).equals(card.getId())) {
								flipCard(button, FILE_NAME_CARD_BACK, true);
								startAnimationTimer();
								break;
							}

						}
					}
				}
			}
		}, 2000);

		animationRunning = true; // disable click events until back animation is finished
	}


	@Override
	public void onNewRound(Player currentPlayer, int playerPoints) {
        setTitle(currentPlayer.getName() + "'s turn");
	}


	@Override
	public void onGameFinished() {
		List<Pair<Player, Integer>> leaderBoard = getLeaderBoard();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Leader Board:\n");
		for (Pair<Player, Integer> pair : leaderBoard) {
			stringBuilder.append(pair.first.getName()).append(": ").append(pair.second).append(" points\n");
		}

		new AlertDialog.Builder(this)
				.setTitle("Game Over")
				.setMessage(stringBuilder.toString())
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								finish();
							}
						})
								.setOnCancelListener(new DialogInterface.OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								finish();
							}
						})
				.show();
	}


	@Override
	public void onHostLost() {
		Toast.makeText(this, "Lost connection to host, shutting down game!", Toast.LENGTH_LONG).show();
	}


	@Override
	public void onClick(View view) {
		// wait for any flipping to stop
		if (animationRunning) return;

		// do not forward click events while game manager is not ready
		if (clientGameManager.getCurrentState() != GameState.SELECT_1ST_CARD
				&& clientGameManager.getCurrentState() != GameState.SELECT_2ND_CARD) {
			Timber.w("Dropped click request due to wrong state");
			return;
		}

		// actually process click
		int cardId = (int) view.getTag(R.id.cardId);
        flipCard((ImageButton) view, clientGameManager.getClosedCards().get(cardId).getValue() + ".png", false);
		startAnimationTimer();
		clientGameManager.selectCard(cardId);
		animationRunning = true;
	}


	private List<Pair<Player, Integer>> getLeaderBoard() {
		List<Player> players = clientGameManager.getPlayers();
		List<Integer> playerPoints = clientGameManager.getPlayerPoints();
		List<Pair<Player, Integer>> leaderBoard = new LinkedList<>();
		for (int i = 0; i < players.size(); ++i) {
			leaderBoard.add(new Pair<>(players.get(i), playerPoints.get(i)));
		}
		Collections.sort(leaderBoard, new Comparator<Pair<Player, Integer>>() {
			@Override
			public int compare(Pair<Player, Integer> lhs, Pair<Player, Integer> rhs) {
				return lhs.second.compareTo(rhs.second);
			}
		});
		return leaderBoard;
	}


	private void refreshAllCards() {
		cards.clear();
		Map<Integer, Card> matchedCards = clientGameManager.getMatchedCards();
		Map<Integer, Card> selectedCards = clientGameManager.getSelectedCards();
		cards.addAll(clientGameManager.getClosedCards().values());
		cards.addAll(matchedCards.values());
		cards.addAll(selectedCards.values());
		Collections.sort(cards, new Comparator<Card>() {
			@Override
			public int compare(Card lhs, Card rhs) {
				return Integer.valueOf(lhs.getId()).compareTo(rhs.getId());
			}
		});

		// iterate over all cards and update UI accordingly
		Iterator<Card> cardIterator = cards.iterator();
		for (int row = 0; row < tableLayout.getChildCount(); ++row) {
			TableRow rowView = (TableRow) tableLayout.getChildAt(row);
			for (int column = 0; column < rowView.getChildCount(); ++column) {
				if (!cardIterator.hasNext()) break;
				Card card = cardIterator.next();

				Bitmap bitmap;
				if (clientGameManager.getClosedCards().containsKey(card.getId())) bitmap = bitmapCache.getBitmap(FILE_NAME_CARD_BACK);
				else bitmap = bitmapCache.getBitmap(card.getValue() + ".png");
				ImageButton button = (ImageButton) rowView.getChildAt(column);
				button.setTag(R.id.cardId, card.getId());
				button.setImageBitmap(bitmap);
				if (matchedCards.containsKey(card.getId()) || selectedCards.containsKey(card.getId())) {
					button.setEnabled(false);
				} else {
					button.setEnabled(true);
				}

			}
		}
	}


	private void flipCard(final ImageButton button, final String newCardFileName, boolean enable) {
		Animation startFlipAnimation = AnimationUtils.loadAnimation(this, R.anim.flip_to_middle);
		final Animation stopFlipAnimation = AnimationUtils.loadAnimation(this, R.anim.flip_from_middle);

		startFlipAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) { }

			@Override
			public void onAnimationEnd(Animation animation) {
				button.setImageBitmap(bitmapCache.getBitmap(newCardFileName));
				button.startAnimation(stopFlipAnimation);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {  }
		});

		button.setEnabled(enable);
		button.clearAnimation();
		button.startAnimation(startFlipAnimation);
	}


	// Clear animation flag at the end of animation. Handle here such that multiple animations can occur in parallel.
	private void startAnimationTimer() {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				animationRunning = false;
			}
		}, getResources().getInteger(R.integer.flip_card_half_duration) * 2);
	}

}
