package us.jodyscott.clouddb.core.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.flakeidgen.FlakeIdGenerator;

import us.jodyscott.clouddb.core.CloudConfig;
import us.jodyscott.clouddb.core.CloudController;
import us.jodyscott.clouddb.core.CloudException;
import us.jodyscott.clouddb.core.CloudMember;
import us.jodyscott.clouddb.core.CloudRoles;
import us.jodyscott.clouddb.core.Constants;
import us.jodyscott.clouddb.core.Entities;
import us.jodyscott.clouddb.core.IpAddrPort;
import us.jodyscott.clouddb.core.util.IpTools;

/*
 * This class implements much of EntitiesHandler except for the part dependent of the
 * class types
 * 
 * The HazelcastInstance must NOT escape from this class
 * 
 */
public class CloudControllerImpl implements CloudController {

	// TODO: Client should handle situation where cluster was NOT active but
	// them becomes active again

	private static final Logger LOG = LoggerFactory.getLogger(CloudControllerImpl.class);

	private static final String CLOUD_MEMBER_MAP = "CLOUD_MEMBER_MAP";
	private static final String CLOUD_MEMBER_LOCK = "CLOUD_MEMBER_LOCK";

	private static final int QUORUM_RECOUNT_ATTEMPTS = 10;

	private final EntitiesAbstractImpl entities;
	private final CloudMember cloudMember = new CloudMemberImpl();
	private final AtomicInteger clusterMemberSize = new AtomicInteger();

	private final HazelcastInstance hazelcastInstance;
	private final ExecutorService executorService = Executors.newCachedThreadPool();
	private final String membershipId;

	private int majority;
	private ScheduledFuture<?> mapCleanup;

	private volatile boolean online;
	private volatile boolean isFinal;

	public CloudControllerImpl(CloudConfig cloudConfig) {
		cloudConfig = CloudConfig.newInstance(cloudConfig);

		LOG.trace("enter (cloudConfig={})", cloudConfig);

		try {

			assert cloudConfig != null;
			cloudConfig = CloudConfig.newInstance(cloudConfig);

			if (cloudConfig.getCloudRoles().contains(CloudRoles.SERVER)
					&& cloudConfig.getCloudRoles().contains(CloudRoles.CLIENT)) {
				throw new RuntimeException("RoleType SERVER and CLIENT are mutually exclusive");
			}

			if (cloudConfig.getCloudRoles().contains(CloudRoles.QUORUM)) {
				if (cloudConfig.getCloudRoles().contains(CloudRoles.CLIENT)) {
					throw new RuntimeException("RoleType CLIENT can not be assiged RoleType QUORUM");
				}
				if (!cloudConfig.getCloudRoles().contains(CloudRoles.SERVER)) {
					LOG.info("Adding {} to config", CloudRoles.SERVER);
					cloudConfig.getCloudRoles().add(CloudRoles.SERVER);
				}
			}

			if (cloudConfig.getCloudRoles().contains(CloudRoles.SERVER)) {

				LOG.info("Starting Server Instance");

				com.hazelcast.config.Config hazelcastConfig = new com.hazelcast.config.Config();
				hazelcastConfig.setProperty("hazelcast.logging.type", "slf4j");

				hazelcastConfig.getGroupConfig().setName(cloudConfig.getName());
				hazelcastConfig.getGroupConfig().setPassword(cloudConfig.getSecret());
				// Network discovery
				hazelcastConfig.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
				hazelcastConfig.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);

				// TODO Merge Policy Check
				// hazelcastConfig.getMapConfig("default").setMergePolicy(MergePolicyImpl.class.getName());

				// TCP Port
				hazelcastConfig.getNetworkConfig().setPort(cloudConfig.getDbPort());

				/*
				 * Peers. If the peer port is zero then we will assume it is
				 * actually the same as our port
				 */
				if (cloudConfig.getPeers().isEmpty()) {
					LOG.warn("No peers configured. Seems a little strange.");
				} else {
					LOG.debug("Adding peers");
					for (IpAddrPort peer : cloudConfig.getPeers()) {

						String peerString = null;

						if (!peer.hasPort()) {
							LOG.info("Peer port is zero. Changing to {}", cloudConfig.getDbPort());
							peerString = IpTools.ipDecimalNotationToString(peer.getIpAddr())
									+ IpAddrPort.IP_PORT_DELIMITER + cloudConfig.getDbPort();
						} else {
							peerString = IpTools.ipDecimalNotationToString(peer.getIpAddr())
									+ IpAddrPort.IP_PORT_DELIMITER + peer.getPort();
						}

						LOG.trace("Adding peer {}", peer);
						hazelcastConfig.getNetworkConfig().getJoin().getTcpIpConfig().addMember(peerString);
					}
				}

				hazelcastConfig.getSerializationConfig().addDataSerializableFactory(Constants.FACTORY_ID,
						new SerialFactory());

				this.hazelcastInstance = Hazelcast.newHazelcastInstance(hazelcastConfig);

			} else if (cloudConfig.getCloudRoles().contains(CloudRoles.CLIENT)) {

				LOG.info("Starting Client Instance");
				com.hazelcast.client.config.ClientConfig hazelcastConfig = new com.hazelcast.client.config.ClientConfig();
				hazelcastConfig.setProperty("hazelcast.logging.type", "slf4j");

				hazelcastConfig.getGroupConfig().setName(cloudConfig.getName());
				hazelcastConfig.getGroupConfig().setPassword(cloudConfig.getSecret());

				// Cluster Peers / Members
				if (cloudConfig.getPeers().isEmpty()) {
					throw new RuntimeException("Atleast one peer must be configured");
				}

				LOG.debug("Adding peers");
				for (IpAddrPort peer : cloudConfig.getPeers()) {
					LOG.trace("Adding peer {}", peer);

					String peerString = null;

					if (!peer.hasPort()) {
						LOG.info("Peer port is zero. Changing to {}", cloudConfig.getDbPort());
						peerString = IpTools.ipDecimalNotationToString(peer.getIpAddr()) + IpAddrPort.IP_PORT_DELIMITER
								+ cloudConfig.getDbPort();
					} else {
						peerString = IpTools.ipDecimalNotationToString(peer.getIpAddr()) + IpAddrPort.IP_PORT_DELIMITER
								+ peer.getPort();

					}

					hazelcastConfig.getNetworkConfig().addAddress(peerString);
				}

				hazelcastConfig.getSerializationConfig().addDataSerializableFactory(Constants.FACTORY_ID,
						new SerialFactory());
				this.hazelcastInstance = HazelcastClient.newHazelcastClient(hazelcastConfig);

			} else {
				throw new AssertionError("RoleType SERVER or CLIENT is required");
			}

			this.membershipId = hazelcastInstance.getCluster().addMembershipListener(new MembershipListenerImpl());
			assert membershipId != null;

			if ((cloudConfig.getQuorumSize() % 2) == 0) {
				throw new IllegalArgumentException(
						"Quorum count must be odd hence " + cloudConfig.getQuorumSize() + " is invalid");
			}

			// Majority is half + 1. We require Quorum to be odd so by
			// adding 1
			// it
			// becomes even and then we need only divide it in half
			this.majority = (cloudConfig.getQuorumSize() + 1) / 2;

			this.cloudMember.getCloudRoles().addAll((cloudConfig.getCloudRoles()));
			if (cloudConfig.hasDbPort()) {
				this.cloudMember.setPort(cloudConfig.getDbPort());
			}
			if (cloudConfig.hasHostname()) {
				this.cloudMember.setHostname(cloudConfig.getHostname());
			}

			if (cloudConfig.getCloudRoles().contains(CloudRoles.QUORUM)) {

				this.cloudMember.setUuid(hazelcastInstance.getCluster().getLocalMember().getUuid());

				this.mapCleanup = Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {

					if (getCloudMemberLock().tryLock()) {

						try {

							LOG.debug("Running {} cleanup", CloudMember.class.getSimpleName());

							// Set us as the owner of the lock

							Set<String> uuidSet = new HashSet<>();
							for (Member member : hazelcastInstance.getCluster().getMembers()) {
								uuidSet.add(member.getUuid());
							}

							for (CloudMember cloudMember : getCloudMemberMap().values()) {

								if (uuidSet.contains(cloudMember.getUuid())) {
									LOG.trace("CloudMember {} is active", cloudMember);

								} else {
									LOG.info("CloudMember {} is stale and will be removed", cloudMember);
									getCloudMemberMap().remove(cloudMember.getUuid());
								}

							}
						}

						finally {
							getCloudMemberLock().unlock();
						}

					} else {
						LOG.debug("Someone else is doing it");
					}

				}, 10, 10, TimeUnit.SECONDS);

			} else {
				this.cloudMember.setUuid(hazelcastInstance.getLocalEndpoint().getUuid());
			}

			LOG.info("Me->{}", this.cloudMember);
			getCloudMemberMap().put(this.cloudMember.getUuid(), cloudMember);

			this.entities = new EntitiesImpl(this);

			computeQuorum();

		} catch (Throwable t) {

			// On a throwable we catch and and shut down the Executor Service(s)
			// so that the main thread does not hang.

			if (this.mapCleanup == null) {
				LOG.trace("MapCleanup is not running for this instance");
			} else {
				LOG.trace("Cancelling MapCleanup");
				this.mapCleanup.cancel(true);
			}

			LOG.trace("Shutting down Membership Listener");
			executorService.shutdown();

			LOG.error("Unhandled Throwable->{}", t);
			throw t;

		} finally {
			LOG.trace("exit void start()");
		}

	}

	@Override
	public synchronized void shutdown() {
		LOG.trace("enter void shutdown()");

		LOG.debug("Shutting down");

		if (mapCleanup == null) {
			LOG.trace("MapCleanup is not running for this instance");
		} else {
			LOG.trace("Cancelling MapCleanup");
			mapCleanup.cancel(true);
		}

		LOG.trace("Shutting down Membership Listener");
		executorService.shutdown();

		LOG.trace("Removing self from membership listener");
		hazelcastInstance.getCluster().removeMembershipListener(membershipId);

		LOG.trace("Shutting down Hazelcast");
		hazelcastInstance.shutdown();

		LOG.trace("exit void shutdown()");
	}

	@Override
	public Entities entities() {
		// Accessor to final entity. No need to log.
		return entities;
	}

	@Override
	public void tryQuorum() throws CloudException {
		try {
			LOG.trace("enter void tryQuorum()");

			assertNotFinal();

			while (!online) {
				try {
					LOG.trace("Waiting to be set online");
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}

			int c = 0;
			while (true) {

				int clusterMemberSize = this.clusterMemberSize.get();

				LOG.trace("Quorum is {} and present is {}", this.majority, clusterMemberSize);

				if (clusterMemberSize >= this.majority) {
					return;
				}

				computeQuorum();

				c++;

				if (c > QUORUM_RECOUNT_ATTEMPTS) {
					throw CloudException.quorum("Quorum is {} and present is {}", this.majority, clusterMemberSize);
				} else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}

		} finally {
			LOG.trace("exit void tryQuorum()");
		}
	}

	private IMap<String, CloudMember> getCloudMemberMap() {
		return hazelcastInstance.getMap(CLOUD_MEMBER_MAP);
	}

	private ILock getCloudMemberLock() {
		return hazelcastInstance.getLock(CLOUD_MEMBER_LOCK);
	}

	IMap<String, ?> getMap(String name) throws CloudException {
		assertNotFinal();
		assert name != null;
		return hazelcastInstance.getMap(name);
	}

	ILock getLock(String name) throws CloudException {
		assertNotFinal();
		assert name != null;
		return hazelcastInstance.getLock(name);
	}

	FlakeIdGenerator getFlakeIdGenerator(String name) throws CloudException {
		assertNotFinal();
		assert name != null;
		return hazelcastInstance.getFlakeIdGenerator(name);
	}

	private void computeQuorum() {
		LOG.trace("enter void setCount()");

		int c = 0;

		// NEED Cleanup

		// Remove stale CloudMembers

		for (Member member : hazelcastInstance.getCluster().getMembers()) {

			CloudMember cloudMember = getCloudMemberMap().get(member.getUuid());

			if (cloudMember == null) {
				LOG.warn("CloudMember for UID {} not found", member.getUuid());
			} else {

				if (cloudMember.getCloudRoles().contains(CloudRoles.QUORUM)) {
					LOG.info("UID {} has type QUORUM", member.getUuid());
					c++;
				}

			}
		}
		clusterMemberSize.set(c);
		LOG.trace("exit void setCount()");
	}

	private class MembershipListenerImpl implements MembershipListener {

		@Override
		public void memberAdded(MembershipEvent membershipEvent) {
			LOG.trace("enter memberAdded");

			if (isFinal) {
				return;
			}

			executorService.execute(() -> {
				computeQuorum();
			});

			LOG.trace("exit memberAdded");
		}

		@Override
		public void memberAttributeChanged(MemberAttributeEvent membershipEvent) {
			LOG.trace("enter memberAttributeChanged");

			if (isFinal) {
				return;
			}
			executorService.execute(() -> {
				computeQuorum();
			});

			LOG.trace("exit memberAttributeChanged");
		}

		@Override
		public void memberRemoved(MembershipEvent membershipEvent) {
			LOG.trace("enter memberRemoved");

			if (isFinal) {
				return;
			}

			executorService.execute(() -> {
				computeQuorum();
			});

			LOG.trace("exit memberRemoved");
		}

	}

	private void assertNotFinal() throws CloudException {
		if (isFinal) {
			throw CloudException.offline("Database was shutdown and is now final");
		}
	}

	@Override
	public void setOnline() {
		this.online = true;
	}

}
