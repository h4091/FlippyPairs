package org.faudroids.distributedmemory.ui;

import android.content.pm.PackageManager;
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
import timber.log.Timber;


public class AboutActivity extends BaseActivity {

	@InjectView(R.id.activity_about_credits) TextView creditsView;
	@InjectView(R.id.activity_about_version) TextView appVersionView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		ButterKnife.inject(this);

		Spanned spanned = Html.fromHtml(getString(R.string.app_description_credits));
		creditsView.setMovementMethod(LinkMovementMethod.getInstance());
		creditsView.setText(spanned);

		try {
			appVersionView.setText(getString(R.string.version, getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
		} catch (PackageManager.NameNotFoundException nnfe) {
			Timber.e("failed to find version", nnfe);
		}
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}
}
