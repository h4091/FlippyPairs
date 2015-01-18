package org.faudroids.distributedmemory.network;


import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;

import org.faudroids.distributedmemory.utils.Assert;

import java.io.IOException;
import java.net.Socket;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;


@Singleton
public final class NetworkManager {

	private static final String SERVICE_PREFIX = "distributedmemory";
	private static final String SERVICE_TYPE = "_socket._tcp.";

	private final NsdManager nsdManager;
	private final HostSocketHandler hostSocketHandler;

	private NsdManager.RegistrationListener serviceRegistrationListener;
	private NsdManager.DiscoveryListener discoveryListener;

	@Inject
	public NetworkManager(NsdManager nsdManager, HostSocketHandler hostSocketHandler) {
		this.nsdManager = nsdManager;
		this.hostSocketHandler = hostSocketHandler;
	}


	public void startServer(String serviceName, final HostListener hostListener, final Handler handler) {
		Assert.assertFalse(hostSocketHandler.isRunning(), "can only start one server");

		try {
			int serverPort = hostSocketHandler.start(new HostSocketHandler.ClientConnectionListener() {
				@Override
				public void onClientConnected(Socket socket) {
					try {
						final ConnectionHandler connectionHandler = new SimpleConnectionHandler(socket);
						handler.post(new Runnable() {
							@Override
							public void run() {
								hostListener.onConnectedToClient(connectionHandler);
							}
						});
					} catch (IOException ioe) {
						Timber.e(ioe, "failed to connect to client");
					}
				}
			});

			String fullServiceName = SERVICE_PREFIX + serviceName;
			NsdServiceInfo serviceInfo = new NsdServiceInfo();
			serviceInfo.setServiceName(fullServiceName);
			serviceInfo.setServiceType(SERVICE_TYPE);
			serviceInfo.setPort(serverPort);

			serviceRegistrationListener = new RegistrationListener(fullServiceName, hostListener, handler);
			nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, serviceRegistrationListener);

		} catch (IOException ioe) {
			Timber.e(ioe, "failed to start server");
			hostListener.onServerStartError();
		}

	}


	public boolean isServerRunning() {
		return hostSocketHandler.isRunning();
	}


	public void stopServer() {
		Assert.assertTrue(hostSocketHandler.isRunning(), "sever not started");
		hostSocketHandler.shutdown();
		nsdManager.unregisterService(serviceRegistrationListener);
		serviceRegistrationListener = null;
	}


	public void startDiscovery(ClientListener clientListener, Handler handler) {
		Assert.assertTrue(discoveryListener != null, "Can only listen for one service type");

		discoveryListener = new DiscoveryListener(clientListener, nsdManager, handler);
		nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
	}


	public boolean isDiscoveryRunning() {
		return discoveryListener != null;
	}


	public void stopDiscovery() {
		nsdManager.stopServiceDiscovery(discoveryListener);
		discoveryListener = null;
	}


	public void connectToHost(final HostInfo hostInfo, final ClientListener clientListener, final Handler handler) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					final ConnectionHandler connectionHandler = new SimpleConnectionHandler(hostInfo.getAddress(), hostInfo.getPort());
					handler.post(new Runnable() {
						@Override
						public void run() {
							clientListener.onConnectedToHostSuccess(connectionHandler);
						}
					});
				} catch (IOException ioe) {
					Timber.e(ioe, "failed to connect to client");
					handler.post(new Runnable() {
						@Override
						public void run() {
							clientListener.onConnectedToHostError();
						}
					});
				}
			}
		}).start();
	}


	private static final class RegistrationListener implements NsdManager.RegistrationListener {

		private final String serviceName;
		private final HostListener hostListener;
		private final Handler handler;

		public RegistrationListener(String serviceName, HostListener hostListener, Handler handler) {
			this.serviceName = serviceName;
			this.hostListener = hostListener;
			this.handler = handler;
		}


		@Override
		public void onServiceRegistered(NsdServiceInfo serviceInfo) {
			Timber.i("Service registration success");
			if (serviceInfo.getServiceName().equals(serviceName)) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						hostListener.onServerStartSuccess();
					}
				});
			} else {
				Timber.e("Registered service name did not match");
				handler.post(new Runnable() {
					@Override
					public void run() {
						hostListener.onServerStartError();
					}
				});
			}
		}


		@Override public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
			Timber.e("Service registration failed (" + errorCode + ")");
			handler.post(new Runnable() {
				@Override
				public void run() {
					hostListener.onServerStartError();
				}
			});
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


	private static final class DiscoveryListener implements NsdManager.DiscoveryListener {

		private final ClientListener clientListener;
		private final NsdManager nsdManager;
		private final Handler handler;

		public DiscoveryListener(ClientListener clientListener, NsdManager nsdManager, Handler handler) {
			this.clientListener = clientListener;
			this.nsdManager = nsdManager;
			this.handler = handler;
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
			nsdManager.resolveService(serviceInfo, new ResolveListener(clientListener, handler));
		}


		@Override
		public void onServiceLost(final NsdServiceInfo serviceInfo) {
			Timber.i("Service lost (" + serviceInfo + ")");
			handler.post(new Runnable() {
				@Override
				public void run() {
					clientListener.onServiceLost(serviceInfo.getServiceName().substring(SERVICE_PREFIX.length()));
				}
			});
		}


		@Override
		public void onStartDiscoveryFailed(String serviceType, int errorCode) {
			Timber.e("Service discovery failed (" + errorCode + ")");
			nsdManager.stopServiceDiscovery(this);
			handler.post(new Runnable() {
				@Override
				public void run() {
					clientListener.onServiceDiscoveryError();
				}
			});
		}


		@Override
		public void onStopDiscoveryFailed(String serviceType, int errorCode) {
			Timber.e("Stopping service discovery failed (" + errorCode + ")");
			nsdManager.stopServiceDiscovery(this);
			handler.post(new Runnable() {
				@Override
				public void run() {
					clientListener.onServiceDiscoveryError();
				}
			});
		}

	}


	private static final class ResolveListener implements NsdManager.ResolveListener {

		private final ClientListener clientListener;
		private final Handler handler;

		public ResolveListener(ClientListener clientListener, Handler handler) {
			this.clientListener = clientListener;
			this.handler = handler;
		}


		@Override
		public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
			Timber.e("Service resolve error (" + errorCode + ")");
			handler.post(new Runnable() {
				@Override
				public void run() {
					clientListener.onServiceDiscoveryError();
				}
			});
		}


		@Override
		public void onServiceResolved(final NsdServiceInfo serviceInfo) {
			Timber.i("Service resolve success (" + serviceInfo + ")");
			handler.post(new Runnable() {
				@Override
				public void run() {
					clientListener.onServiceDiscovered(
							new HostInfo(
									serviceInfo.getServiceName().substring(SERVICE_PREFIX.length()),
									serviceInfo.getHost(),
									serviceInfo.getPort()));
				}
			});
		}

	}

}
