package org.faudroids.distributedmemory.ui;

import android.os.Bundle;
import android.widget.TextView;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;

import java.util.List;

import butterknife.ButterKnife;
import timber.log.Timber;


public class HelpDialogActivity extends BaseActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_help_text);

        TextView helpTextView = (TextView)findViewById(R.id.dialog_help_text);
        Bundle extra = getIntent().getExtras();
        if(extra!=null) {
            String helpText;
            helpText = (String) extra.get("org.faudroids.distributedmemory.helpText");
            Timber.d("helpText: " + helpText);
            helpTextView.setText(helpText);
        } else {
            Timber.d("No extras");
        }
		ButterKnife.inject(this);
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}
}
