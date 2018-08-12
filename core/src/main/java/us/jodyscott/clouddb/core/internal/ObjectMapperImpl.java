package us.jodyscott.clouddb.core.internal;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

import us.jodyscott.clouddb.core.CloudConfig;
import us.jodyscott.clouddb.core.CloudMember;
import us.jodyscott.clouddb.core.HelloWorldDAO;
import us.jodyscott.clouddb.core.IpAddrPort;
import us.jodyscott.clouddb.core.PersonDAO;
import us.jodyscott.clouddb.core.SimpleError;

public class ObjectMapperImpl extends ObjectMapper {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	{
		SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
		this.setSerializationInclusion(Include.NON_NULL);

		resolver.addMapping(SimpleError.class, SimpleErrorImpl.class);
		resolver.addMapping(CloudConfig.class, CloudConfigImpl.class);
		resolver.addMapping(CloudMember.class, CloudMemberImpl.class);

		resolver.addMapping(HelloWorldDAO.class, HelloWorldDAOImpl.class);
		resolver.addMapping(PersonDAO.class, PersonDAOImpl.class);

		resolver.addMapping(IpAddrPort.class, IpAddrPortImpl.class);

		SimpleModule module = new SimpleModule();
		module.setAbstractTypes(resolver);
		this.registerModule(module);
	}

}
