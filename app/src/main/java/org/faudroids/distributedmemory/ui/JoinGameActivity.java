package org.faudroids.distributedmemory.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseListActivity;
import org.faudroids.distributedmemory.network.ClientNetworkListener;
import org.faudroids.distributedmemory.network.ConnectionHandler;
import org.faudroids.distributedmemory.network.HostInfo;
import org.faudroids.distributedmemory.network.NetworkManager;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;


public class JoinGameActivity extends BaseListActivity implements ClientNetworkListener<JsonNode> {

	@Inject ClientUtils clientUtils;
	@Inject NetworkManager networkManager;
	@Inject QRCodeUtils qrCodeUtils;
	private ArrayAdapter<HostInfo> adapter;
	private ProgressDialog connectingToHostDialog;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_join_game);
		ButterKnife.inject(this);

		adapter = new ArrayAdapter<>(this, R.layout.list_item);
		setListAdapter(adapter);
	}


	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		HostInfo hostInfo =  adapter.getItem(position);
		connectToHost(hostInfo);
	}


	@Override
	public void onResume() {
		super.onResume();
		networkManager.startDiscovery(this, new Handler(getMainLooper()));
		adapter.clear();
		adapter.notifyDataSetChanged();
	}


	@Override
	public void onPause() {
		networkManager.stopDiscovery();
		super.onPause();
	}


	@OnClick(R.id.join_help)
	public void onHelpJoinClicked() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.join_game_help_title)
				.setMessage(getString(R.string.join_game_help_client_instructions1))
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						IntentIntegrator intentIntegrator = new IntentIntegrator(JoinGameActivity.this);
						intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
						intentIntegrator.setPrompt(getString(R.string.join_game_help_client_instructions2));
						intentIntegrator.setResultDisplayDuration(0);
						intentIntegrator.initiateScan();
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanResult != null) {
			new AsyncTask<Void, Void, HostInfo>() {

				@Override
				protected HostInfo doInBackground(Void... params) {
					return qrCodeUtils.readHostInfo(scanResult.getContents());
				}

				@Override
				protected void onPostExecute(HostInfo hostInfo) {
					if (hostInfo != null) connectToHost(hostInfo);
					else Toast.makeText(JoinGameActivity.this, getString(R.string.join_game_help_client_error), Toast.LENGTH_LONG).show();
				}

			}.execute(null, null);
			Timber.d(scanResult.getContents());
		}
	}


	@Override
	public void onServiceDiscovered(HostInfo hostInfo) {
		adapter.add(hostInfo);
		adapter.notifyDataSetChanged();
	}


	@Override
	public void onServiceLost(String hostName) {
		for (int i = 0; i < adapter.getCount(); ++i) {
			if (adapter.getItem(i).getName().equals(hostName)) {
				adapter.remove(adapter.getItem(i));
				break;
			}
		}
		adapter.notifyDataSetChanged();
	}


	@Override
	public void onServiceDiscoveryError() {  }


	@Override
	public void onConnectedToHostSuccess(ConnectionHandler<JsonNode> connectionHandler) {
		connectingToHostDialog.cancel();
		connectingToHostDialog = null;

		clientUtils.setupClient(connectionHandler);

		Intent intent = new Intent(this, GameActivity.class);
		startActivity(intent);
		finish();
	}


	@Override
	public void onConnectedToHostError() {
		connectingToHostDialog.cancel();
		connectingToHostDialog = null;
		Toast.makeText(this, "Failed to join game!", Toast.LENGTH_LONG).show();
	}


	private void connectToHost(HostInfo hostInfo) {
		networkManager.connectToHost(hostInfo, this, new Handler(getMainLooper()));
		connectingToHostDialog = ProgressDialog.show(this, "Connecting to host", "Please wait ...", false);
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

}
