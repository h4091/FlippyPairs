package org.faudroids.distributedmemory.ui;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.core.GameManager;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class GameActivity extends BaseActivity {

    private GameManager manager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		ButterKnife.inject(this);
        this.manager = new GameManager(5,2);
	}

	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

    @OnClick(R.id.submit_id)
    protected void submitCardId() {
        EditText et = (EditText)findViewById(R.id.edit_card_id1);
        EditText et2 = (EditText)findViewById(R.id.edit_card_id2);
        int id1 = Integer.parseInt(et.getText().toString());
        int id2 = Integer.parseInt(et2.getText().toString());
        GameManager.gameStatus state = manager.run(id1, id2);
        if(state == GameManager.gameStatus.FINISHED) {
            Toast winnerToast = Toast.makeText(getApplicationContext(),
                    "Winner: " + manager.getPlayerName(manager.getWinnerId()),
                    Toast.LENGTH_LONG);
            winnerToast.show();
        }
    }

}
