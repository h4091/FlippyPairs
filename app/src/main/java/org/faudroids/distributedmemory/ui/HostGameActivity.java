package org.faudroids.distributedmemory.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.core.HostGameManager;
import org.faudroids.distributedmemory.core.Player;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class HostGameActivity extends BaseActivity {

    @Inject HostGameManager hostGameManager;
	@InjectView(R.id.player_count_value) TextView playerCountValue;
	@InjectView(R.id.game_name_value) TextView gameNameValue;
    @InjectView(R.id.pairs_count_value) TextView pairsCountValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_game);
		ButterKnife.inject(this);
	}


	@Override
	public void onPause() {
		super.onPause();
	}


	@OnClick({R.id.player_count_value, R.id.player_count_description})
	public void changePlayerCount() {
		View numberPickerLayout = getLayoutInflater().inflate(R.layout.dialog_number_picker, null);
		final NumberPicker numberPicker = (NumberPicker) numberPickerLayout.findViewById(R.id.number_picker);
		numberPicker.setValue(Integer.valueOf(playerCountValue.getText().toString()));
		numberPicker.setMinValue(2);
		numberPicker.setMaxValue(100);
		numberPicker.setWrapSelectorWheel(false);
        final TextView playersTextView = (TextView) numberPickerLayout.findViewById(R.id
                .number_picker_text);
        playersTextView.setText(R.string.activity_host_game_players_count_players);

		new AlertDialog.Builder(this)
				.setView(numberPickerLayout)
				.setTitle(R.string.activity_host_game_players_count_title)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						playerCountValue.setText(String.valueOf(numberPicker.getValue()));
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
	}

    @OnClick({R.id.pairs_count_value, R.id.pairs_count_description})
    public void changePairsCount() {
        View numberPickerLayout = getLayoutInflater().inflate(R.layout.dialog_number_picker, null);
        final NumberPicker numberPicker = (NumberPicker) numberPickerLayout.findViewById(R.id.number_picker);
        numberPicker.setValue(Integer.valueOf(pairsCountValue.getText().toString()));
        numberPicker.setMinValue(2);
        numberPicker.setMaxValue(hostGameManager.getTotalCardImages());
        numberPicker.setWrapSelectorWheel(false);
        final TextView pairsTextView = (TextView) numberPickerLayout.findViewById(R.id
                .number_picker_text);
        pairsTextView.setText(R.string.activity_host_game_pairs_count_pairs);

        new AlertDialog.Builder(this)
                .setView(numberPickerLayout)
                .setTitle(R.string.activity_host_game_pairs_count_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pairsCountValue.setText(String.valueOf(numberPicker.getValue()));
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }



	@OnClick({R.id.game_name_value, R.id.game_name_description})
	public void changeGameName() {
		View inputLayout = getLayoutInflater().inflate(R.layout.dialog_input_text, null);
		final EditText editText = (EditText) inputLayout.findViewById(R.id.edit_text);
		editText.setFilters(new InputFilter[]{new GameNameInputFilter()});
		editText.setText(gameNameValue.getText());

		new AlertDialog.Builder(this)
				.setView(inputLayout)
				.setTitle(R.string.activity_host_game_game_name_title)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						gameNameValue.setText(editText.getText().toString());
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
	}


	@OnClick(R.id.start_hosting)
	public void startHosting() {
		hostGameManager.initGame();
        int pairsCount = Integer.valueOf(pairsCountValue.getText().toString());
        hostGameManager.setUsedCardImages(pairsCount);
		int playerCount = Integer.valueOf(playerCountValue.getText().toString());
        for(int i=0; i < playerCount; ++i) {
            hostGameManager.addPlayer(new Player(i, "Player " + (i + 1)));
        }
        Intent hostIntent = new Intent(this, HostService.class);
        startService(hostIntent);
        Intent lobbyIntent = new Intent(this, LobbyActivity.class);
        startActivity(lobbyIntent);
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}


	/**
	 * Host game name must contain only letters since it is used for Nsd Network discovery.
	 */
	private static final class GameNameInputFilter implements InputFilter {

		@Override
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			if (source.equals("")) return source;
			if (source.toString().matches("[a-zA-Z]+")) return source;
			return "";
		}

	}

}
