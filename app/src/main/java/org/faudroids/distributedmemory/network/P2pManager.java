package org.faudroids.distributedmemory.network;


import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.net.wifi.p2p.WifiP2pManager.*;
import static android.net.wifi.p2p.WifiP2pManager.Channel;

public final class P2pManager {

	private static final String TAG = "DistributedMemory";

	private static final String SERVICE_PREFIX = "org.faudroids.distributedmemory.";

	private final WifiP2pManager manager;
	private final Channel channel;

	private final Set<P2pService> discoveredServices = new HashSet<>();
	private final List<ServiceDiscoveryListener> serviceDiscoveryListeners = new LinkedList<>();

	private WiFiDirectBroadcastReceiver receiver;


	public P2pManager(WifiP2pManager manager, Channel channel) {
		this.manager = manager;
		this.channel = channel;
	}


	public void startServiceRegistration(final String serviceName, final ServiceRegistrationListener registrationListener) {
		final String fullServiceName = SERVICE_PREFIX + serviceName;
		Map<String, String> record = new HashMap<>();
		record.put("available", "visible");

		WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(fullServiceName, "_presence._tcp", record);
		manager.addLocalService(channel, serviceInfo, new ActionListener() {
			@Override
			public void onSuccess() {
				Log.i(TAG, "Service \"" + fullServiceName + "\" registration successful");
				registrationListener.onRegistrationSuccess(serviceName);
			}

			@Override
			public void onFailure(int reason) {
				Log.i(TAG, "Service \"" + fullServiceName + "\" registration failed");
				registrationListener.onRegistrationSuccess(serviceName);
			}
		});
	}


	public void stopServiceRegistration() {
		manager.clearLocalServices(channel, new ActionListener() {
			@Override
			public void onSuccess() {
				Log.i(TAG, "Clearing services success");
			}

			@Override
			public void onFailure(int reason) {
				Log.i(TAG, "Clearing services error (" + reason + ")");
			}
		});
	}


	public void startServiceDiscovery() {
		manager.setDnsSdResponseListeners(
				channel,
				new DnsSdServiceResponseListener() {
					@Override
					public void onDnsSdServiceAvailable(String fullServiceName, String registrationType, WifiP2pDevice srcDevice) {
						Log.i(TAG, "Service \"" + fullServiceName + "\" discovered");
						if (!fullServiceName.startsWith(SERVICE_PREFIX)) return;

						String serviceName = fullServiceName.substring(SERVICE_PREFIX.length());
						P2pService service = new P2pService(serviceName, srcDevice.deviceAddress);
						discoveredServices.add(service);
						for (ServiceDiscoveryListener listener : serviceDiscoveryListeners) {
							listener.onNewService(service);
						}
					}
				},
				new DnsSdTxtRecordListener() {
					@Override
					public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
						Log.i(TAG, "Device " + srcDevice.deviceName + " available");
					}
				}
		);

		WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
		manager.addServiceRequest(
				channel,
				serviceRequest,
				new ActionListener() {
					@Override
					public void onSuccess() {
						Log.i(TAG, "Add service request success");
					}

					@Override
					public void onFailure(int reason) {
						Log.i(TAG, "Service request error (" + reason + ")");
					}
				});
		manager.discoverServices(channel, new ActionListener() {
			@Override
			public void onSuccess() {
				Log.i(TAG, "Service discovery success");
			}

			@Override
			public void onFailure(int reason) {
				Log.i(TAG, "Service discover error (" + reason + ")");
			}
		});
	}


	public void stopServiceDiscovery() {
		manager.clearServiceRequests(channel, new ActionListener() {
			@Override
			public void onSuccess() {
				Log.i(TAG, "Stopping service discovery success");
			}

			@Override
			public void onFailure(int reason) {
				Log.i(TAG, "Stopping service discovery error (" + reason + ")");
			}
		});
	}


	public void register(ServiceDiscoveryListener listener) {
		this.serviceDiscoveryListeners.add(listener);
	}


	public void unregister(ServiceDiscoveryListener listener) {
		this.serviceDiscoveryListeners.remove(listener);
	}


	public Set<P2pService> getAllDiscoveredServices() {
		return new HashSet<>(discoveredServices);
	}


	public void connectTo(P2pService service, boolean groupOwner) {
		stopServiceDiscovery();

		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = service.getDeviceAddress();
		config.wps.setup = WpsInfo.PBC;
		if (groupOwner) config.groupOwnerIntent = 0;
		else config.groupOwnerIntent = 15;

		manager.connect(channel, config, new ActionListener() {
			@Override
			public void onSuccess() {
				Log.i(TAG, "Connecting start success");
			}

			@Override
			public void onFailure(int reason) {
				Log.i(TAG, "Connecting start error (" + reason + ")");
			}
		});
	}


	public void register(P2pConnectionListener listener, Context context) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter.addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter.addAction(WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		receiver = new WiFiDirectBroadcastReceiver(listener, manager, channel);
		context.registerReceiver(receiver, intentFilter);
	}


	public void unregister(P2pConnectionListener listener, Context context) {
		context.unregisterReceiver(receiver);
	}

}
