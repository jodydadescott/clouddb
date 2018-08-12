package us.jodyscott.clouddb.core.util;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.jodyscott.clouddb.core.CloudRoles;

public class RoleUtil {

	private static final Logger LOG = LoggerFactory.getLogger(RoleUtil.class);

	private Set<CloudRoles> roles = new HashSet<>();

	public void addRole(String role) {

		role = role.replaceAll("\\s+", "");

		if (role.equals("=")) {
			LOG.warn("Role is just a equals sign");
			return;
		}

		if (role.contains("=")) {

			String[] split = role.split("=");
			String key = (split.length > 0) ? split[0] : null;
			String value = (split.length > 1) ? split[1] : null;

			if (isTrueOrFalse(value)) {

				if (isTrue(value)) {
					internalAddRole(key);
				}

			} else {
				internalAddRole(key);
				internalAddRole(value);
			}

			return;
		}

		internalAddRole(role);

	}

	public Set<CloudRoles> getRoles() {
		return roles;
	}

	private void internalAddRole(String role) {

		try {
			roles.add(CloudRoles.valueOf(role.toUpperCase()));

		} catch (IllegalArgumentException e) {
			LOG.warn("Role {} is not known to us. This is not necessarily bad.", role);
		}

	}

	private boolean isTrue(String eval) {

		if (eval == null) {
			return false;
		}

		eval = eval.toUpperCase();

		if (eval.equals("TRUE")) {
			return true;
		}

		return false;
	}

	private boolean isTrueOrFalse(String eval) {

		if (eval == null) {
			return false;
		}

		eval = eval.toUpperCase();

		if (eval.equals("FALSE") || eval.equals("TRUE")) {
			return true;
		}

		return false;
	}

}
