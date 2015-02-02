package org.faudroids.distributedmemory.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import java.util.ListIterator;
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
	protected void onStop() {
		if (waitingForHostDialog != null) waitingForHostDialog.dismiss();
		super.onStop();
	}


	@Override
	public void onResume() {
		super.onResume();
		clientGameManager.registerClientGameListener(this);
		refreshAllCards();
		updateCurrentPlayer();

		if (clientGameManager.getCurrentState() == GameState.SETUP
				|| clientGameManager.getCurrentState() == GameState.CONNECTING) {

			waitingForHostDialog = new ProgressDialog(this) {

				{
					setTitle(R.string.activity_game_waiting_title);
					setMessage(getString(R.string.activity_game_waiting_message));
					setIndeterminate(false);
					setCancelable(true);
				}

				@Override
				public void onBackPressed() {
					GameActivity.this.onBackPressed();
				}

			};
			waitingForHostDialog.show();
		}

		if (clientGameManager.getCurrentState() == GameState.FINISHED) finish();
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
		waitingForHostDialog.dismiss();
		waitingForHostDialog = null;
	}


	@Override
	public void onCardsMatch(Collection<Card> matchedCards) { }


	@Override
	public void onCardsMismatch(final Collection<Card> mismatchedCards) {
		final List<ImageButton> buttons = new LinkedList<>();
		for (Card card : mismatchedCards) {
			for (int row = 0; row < tableLayout.getChildCount(); ++row) {
				TableRow rowView = (TableRow) tableLayout.getChildAt(row);
				for (int column = 0; column < rowView.getChildCount(); ++column) {
					ImageButton button = (ImageButton) rowView.getChildAt(column);
					if (button.getTag(R.id.cardId).equals(card.getId())) {
						button.setTag(R.id.cardBackFlipDelayed, true);
						buttons.add(button);
					}

				}
			}
		}

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				for (ImageButton button : buttons) {
					button.setTag(R.id.cardBackFlipDelayed, false);
					flipCard(button, FILE_NAME_CARD_BACK, true);
					startAnimationTimer();
				}
			}
		}, 2000);

		animationRunning = true; // disable click events until back animation is finished
	}


	@Override
	public void onNewRound(Player currentPlayer, int playerPoints) {
		updateCurrentPlayer();
	}


	@Override
	public void onGameFinished() {
		List<Pair<Player, Integer>> leaderBoard = getLeaderBoard();
		Pair<Player, Integer> firstPlayer = leaderBoard.get(leaderBoard.size() - 1);
		Pair<Player, Integer> secondPlayer = leaderBoard.get(leaderBoard.size() - 2);

		StringBuilder stringBuilder = new StringBuilder();
		if (firstPlayer.second.equals(secondPlayer.second)) {
			stringBuilder.append(getString(R.string.activity_game_finish_draw));
		} else {
			stringBuilder.append(getString(R.string.activity_game_finish_winner, firstPlayer.first.getName()));
		}
		stringBuilder.append("\n");

		ListIterator<Pair<Player, Integer>> iterator = leaderBoard.listIterator(leaderBoard.size());
		while(iterator.hasPrevious()) {
			stringBuilder.append("\n");
			Pair<Player, Integer> pair = iterator.previous();
			stringBuilder.append(getString(R.string.activity_game_finish_points, pair.first.getName(), pair.second));

		}

		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.activity_game_finish_title))
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
	public void onHostBusy() {
		refreshAllCards();
	}


	@Override
	public void onHostLost() {
		Dialog dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.activity_game_host_error_title)
				.setMessage(R.string.activity_game_host_error_message)
				.setIcon(R.drawable.ic_action_error)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				})
				.create();
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				finish();
			}
		});
		dialog.show();
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
				ImageButton button = (ImageButton) rowView.getChildAt(column);
				boolean backFlipDelayed = Boolean.TRUE.equals(button.getTag(R.id.cardBackFlipDelayed));

				Bitmap bitmap;
				if (!backFlipDelayed && clientGameManager.getClosedCards().containsKey(card.getId())) bitmap = bitmapCache.getBitmap(FILE_NAME_CARD_BACK);
				else bitmap = bitmapCache.getBitmap(card.getValue() + ".png");
				button.setTag(R.id.cardId, card.getId());
				Animation animation = button.getAnimation();
				if (animation != null) animation.setAnimationListener(null);
				button.clearAnimation();
				button.setImageBitmap(bitmap);

				if (matchedCards.containsKey(card.getId()) || selectedCards.containsKey(card.getId())) {
					button.setEnabled(false);
				} else {
					button.setEnabled(true);
				}
			}
		}
		animationRunning = false;
	}


	private void updateCurrentPlayer() {
		Player player = clientGameManager.getCurrentPlayer();
		if (player != null) setTitle(getString(R.string.activity_game_current_player, player.getName()));
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


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

}
