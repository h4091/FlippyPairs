package org.faudroids.distributedmemory.network;


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

import static android.net.wifi.p2p.WifiP2pManager.Channel;

public final class P2pManager {

	private static final String TAG = "DistributedMemory";

	private static final String SERVICE_PREFIX = "org.faudroids.distributedmemory.";

	private final WifiP2pManager manager;
	private final Channel channel;

	private final Set<String> discoveredServices = new HashSet<>();
	private final List<ServiceDiscoveryListener> serviceDiscoveryListeners = new LinkedList<>();


	public P2pManager(WifiP2pManager manager, Channel channel) {
		this.manager = manager;
		this.channel = channel;
	}


	public void startServiceRegistration(final String serviceName, final ServiceRegistrationListener registrationListener) {
		final String fullServiceName = SERVICE_PREFIX + serviceName;
		Map<String, String> record = new HashMap<>();
		record.put("available", "visible");

		WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(fullServiceName, "_presence._tcp", record);
		manager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
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
		manager.clearServiceRequests(channel, new WifiP2pManager.ActionListener() {
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
				new WifiP2pManager.DnsSdServiceResponseListener() {
					@Override
					public void onDnsSdServiceAvailable(String fullServiceName, String registrationType, WifiP2pDevice srcDevice) {
						Log.i(TAG, "Service \"" + fullServiceName + "\" discovered");
						if (!fullServiceName.startsWith(SERVICE_PREFIX)) return;

						String serviceName = fullServiceName.substring(SERVICE_PREFIX.length());
						discoveredServices.add(serviceName);
						for (ServiceDiscoveryListener listener : serviceDiscoveryListeners) {
							listener.onNewService(serviceName);
						}
					}
				},
				new WifiP2pManager.DnsSdTxtRecordListener() {
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
				new WifiP2pManager.ActionListener() {
					@Override
					public void onSuccess() {
						Log.i(TAG, "Add service request success");
					}

					@Override
					public void onFailure(int reason) {
						Log.i(TAG, "Service request error (" + reason + ")");
					}
				});
		manager.discoverServices(channel, new WifiP2pManager.ActionListener() {
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


	public void register(ServiceDiscoveryListener listener) {
		this.serviceDiscoveryListeners.add(listener);
	}


	public void unregister(ServiceDiscoveryListener listener) {
		this.serviceDiscoveryListeners.remove(listener);
	}


	public Set<String> getAllDiscoveredServices() {
		return new HashSet<>(discoveredServices);
	}

}
