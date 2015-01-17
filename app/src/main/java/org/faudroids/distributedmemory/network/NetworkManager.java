package org.faudroids.distributedmemory.network;


import android.app.Activity;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;


@Singleton
public final class NetworkManager {

	private static final String SERVICE_PREFIX = "distributedmemory";
	private static final String SERVICE_TYPE = "_socket._tcp.";

	private final NsdManager nsdManager;

	private NsdManager.RegistrationListener serviceRegistrationListener;
	private NsdManager.DiscoveryListener discoveryListener;

	@Inject
	public NetworkManager(NsdManager nsdManager) {
		this.nsdManager = nsdManager;
	}


	public <T extends Activity & NetworkListener> void registerService(String serviceName, int port, T registrationListener) {
		if (serviceRegistrationListener != null) throw new IllegalStateException("Can only register one service");

		String fullServiceName = SERVICE_PREFIX + serviceName;
		NsdServiceInfo serviceInfo = new NsdServiceInfo();
		serviceInfo.setServiceName(fullServiceName);
		serviceInfo.setServiceType(SERVICE_TYPE);
		serviceInfo.setPort(port);

		serviceRegistrationListener = new RegistrationListener<>(fullServiceName, registrationListener);
		nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, serviceRegistrationListener);
	}


	public void unregisterService() {
		if (serviceRegistrationListener != null) nsdManager.unregisterService(serviceRegistrationListener);
		serviceRegistrationListener = null;
	}


	public <T extends Activity & NetworkListener> void startDiscovery(T networkListener) {
		if (discoveryListener != null) throw new IllegalStateException("Can only listen for one service type");

		discoveryListener = new DiscoveryListener<>(networkListener, nsdManager);
		nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
	}


	public void stopDiscovery() {
		if (discoveryListener != null) nsdManager.stopServiceDiscovery(discoveryListener);
		discoveryListener = null;
	}



	private static final class RegistrationListener<T extends Activity & NetworkListener> implements NsdManager.RegistrationListener {

		private final String serviceName;
		private final T networkListener;

		public RegistrationListener(String serviceName, T networkListener) {
			this.serviceName = serviceName;
			this.networkListener = networkListener;
		}


		@Override
		public void onServiceRegistered(NsdServiceInfo serviceInfo) {
			Timber.i("Service registration success");
			if (serviceInfo.getServiceName().equals(serviceName)) {
				networkListener.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						networkListener.onRegistrationSuccess();
					}
				});
			} else {
				Timber.e("Registered service name did not match");
				networkListener.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						networkListener.onRegistrationError();
					}
				});
			}
		}


		@Override public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
			Timber.e("Service registration failed (" + errorCode + ")");
			networkListener.onRegistrationError();
		}


		@Override
		public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
			Timber.i("Service unregistered");
		}


		@Override
		public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
			Timber.e("Service unregistration failed (" + errorCode + ")");
		}

	}


	private static final class DiscoveryListener<T extends Activity & NetworkListener> implements NsdManager.DiscoveryListener {

		private final T networkListener;
		private final NsdManager nsdManager;

		public DiscoveryListener(T networkListener, NsdManager nsdManager) {
			this.networkListener = networkListener;
			this.nsdManager = nsdManager;
		}


		@Override
		public void onDiscoveryStarted(String serviceType) {
			Timber.i("Service discovery started");
		}


		@Override
		public void onDiscoveryStopped(String serviceType) {
			Timber.i("Service discovery stopped");
		}


		@Override
		public void onServiceFound(NsdServiceInfo serviceInfo) {
			Timber.i("Service discovery success (" + serviceInfo + ")");
			if (!serviceInfo.getServiceType().equals(SERVICE_TYPE) || !serviceInfo.getServiceName().startsWith(SERVICE_PREFIX)) {
				Timber.i("Discarding discovered service");
				return;
			}
			nsdManager.resolveService(serviceInfo, new ResolveListener<>(networkListener));
		}


		@Override
		public void onServiceLost(final NsdServiceInfo serviceInfo) {
			Timber.i("Service lost (" + serviceInfo + ")");
			networkListener.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					networkListener.onServiceLost(serviceInfo.getServiceName().substring(SERVICE_PREFIX.length()));
				}
			});
		}


		@Override
		public void onStartDiscoveryFailed(String serviceType, int errorCode) {
			Timber.e("Service discovery failed (" + errorCode + ")");
			nsdManager.stopServiceDiscovery(this);
			networkListener.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					networkListener.onServiceDiscoveryError();
				}
			});
		}


		@Override
		public void onStopDiscoveryFailed(String serviceType, int errorCode) {
			Timber.e("Stopping service discovery failed (" + errorCode + ")");
			nsdManager.stopServiceDiscovery(this);
			networkListener.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					networkListener.onServiceDiscoveryError();
				}
			});
		}

	}


	private static final class ResolveListener<T extends Activity & NetworkListener> implements NsdManager.ResolveListener {

		private final T networkListener;

		public ResolveListener(T networkListener) {
			this.networkListener = networkListener;
		}


		@Override
		public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
			Timber.e("Service resolve error (" + errorCode + ")");
			networkListener.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					networkListener.onServiceDiscoveryError();
				}
			});
		}


		@Override
		public void onServiceResolved(final NsdServiceInfo serviceInfo) {
			Timber.i("Service resolve success (" + serviceInfo + ")");
			networkListener.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					networkListener.onServiceDiscovered(
							new Host(
									serviceInfo.getServiceName().substring(SERVICE_PREFIX.length()),
									serviceInfo.getHost(),
									serviceInfo.getPort()));
				}
			});
		}

	}

}
