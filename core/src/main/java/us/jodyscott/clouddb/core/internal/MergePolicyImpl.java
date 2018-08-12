package us.jodyscott.clouddb.core.internal;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.EntryView;
import com.hazelcast.map.merge.MapMergePolicy;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

import us.jodyscott.clouddb.core.DAO;
import us.jodyscott.clouddb.core.util.FString;

public class MergePolicyImpl implements MapMergePolicy {

	private static final Logger LOG = LoggerFactory.getLogger(CloudControllerImpl.class);

	@Override
	public void readData(ObjectDataInput arg0) throws IOException {

	}

	@Override
	public void writeData(ObjectDataOutput arg0) throws IOException {

	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object merge(String map, EntryView mergingEntry, EntryView existingEntry) {

		DAO mergingEntryDao = (DAO) mergingEntry;
		DAO existingEntryDao = (DAO) existingEntry;
		String key = mergingEntryDao.getKey();

		if (existingEntry == null) {
			LOG.info(FString.format("Map->{}, Key->{} :: Existing is null thus using merging", map, key));
			return mergingEntry;
		}

		LOG.info(FString.format("Map->{}, Key->{} :: Existing has later timestamp, using merging", map, key));
		return existingEntryDao;

	}

}
