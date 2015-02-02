package org.faudroids.distributedmemory.ui;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class AboutActivity extends BaseActivity {

	@InjectView(R.id.activity_about_credits) TextView creditsView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		ButterKnife.inject(this);

		Spanned spanned = Html.fromHtml(getString(R.string.app_description_credits));
		creditsView.setMovementMethod(LinkMovementMethod.getInstance());
		creditsView.setText(spanned);
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}
}
