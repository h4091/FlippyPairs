package org.faudroids.distributedmemory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class MainActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		findViewById(R.id.game_host).setOnClickListener(this);
		findViewById(R.id.game_join).setOnClickListener(this);
    }


	@Override
	public void onClick(View view) {
		switch(view.getId()) {
			case R.id.game_host:
				startActivity(new Intent(this, HostGameActivity.class));
				break;

			case R.id.game_join:
				startActivity(new Intent(this, ClientGameActivity.class));
				break;
		}
	}
}
