package org.faudroids.distributedmemory;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


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
				Toast.makeText(this, "Hosting game", Toast.LENGTH_SHORT).show();
				break;

			case R.id.game_join:
				Toast.makeText(this, "Joining game", Toast.LENGTH_SHORT).show();
				break;
		}
	}
}
