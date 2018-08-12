package us.jodyscott.clouddb.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*
 * Exactly the same as CloudException just extends Error instead of Exception.
 * 
 */
public class CloudError extends Error {

	/**
	 * Wraps a CloudException into an Error. This allows it to be thrown
	 * unchecked. Then the receiver can extract the Exception and re-throw it
	 * checked.
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CloudException cloudException;

	public static CloudError newInstance(CloudException cloudException) {
		return new CloudError(cloudException);
	}

	public CloudError(CloudException cloudException) {
		assert cloudException != null;
		this.cloudException = cloudException;
	}

	public CloudException getCloudException() {
		return cloudException;
	}

	@Override
	@JsonIgnore
	public String toString() {
		return JsonMapper.singleton().objectToJson(this);
	}

}
