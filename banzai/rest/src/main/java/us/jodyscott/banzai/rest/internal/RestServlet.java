package us.jodyscott.banzai.rest.internal;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.jodyscott.clouddb.core.CloudException;
import us.jodyscott.clouddb.core.Entity;
import us.jodyscott.clouddb.core.JsonMapper;

public class RestServlet<T> extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Entity<T> handler;
	private final Class<T> clazz;

	RestServlet(Entity<T> handler, Class<T> clazz) {
		super();
		assert handler != null;
		assert clazz != null;
		this.clazz = clazz;
		this.handler = handler;

	}

	private String getParam(String key, HttpServletRequest request) throws CloudException {
		assert key != null;
		assert request != null;
		String value = request.getParameter(key);
		if (value == null) {
			throw CloudException.requestMalformed("Missing {}", key);
		}
		return value;
	}

	private String getParamOrNull(String key, HttpServletRequest request) throws CloudException {
		assert key != null;
		assert request != null;
		return request.getParameter(key);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		String[] uri = request.getRequestURI().split("/");
		String endpoint = uri[uri.length - 1];

		try {

			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_OK);

			switch (endpoint) {

			case "get": {
				response.getWriter().println(handler.get(getParam("key", request)));
				return;
			}

			case "delete": {
				response.getWriter().println(handler.get(getParam("key", request)));
				return;
			}

			case "getall": {
				String search = getParamOrNull("search", request);
				// TODO We are not validating search yet
				if (search == null) {
					response.getWriter().println(handler.values().toString());
				} else {
					response.getWriter().println(handler.search(search).toString());
				}

				return;
			}

			default:
				throw CloudException.requestMalformed("Enpoint {} not mapped", endpoint);

			}

		} catch (CloudException e) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
			response.getWriter().println(e.toSimpleException());
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String[] uri = request.getRequestURI().split("/");
		String endpoint = uri[uri.length - 1];

		try {

			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_OK);

			switch (endpoint) {

			case "create": {
				T dao = JsonMapper.singleton().jsonToObject(clazz, getData(request).toString());
				dao = handler.create(dao);
				response.getWriter().println(dao);
				return;
			}

			default:
				throw CloudException.requestMalformed("Enpoint {} not mapped", endpoint);

			}

		} catch (CloudException e) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
			response.getWriter().println(e.toSimpleException());
		}

	}

	// name = {}"

	private StringBuilder getData(HttpServletRequest request) throws IOException {
		StringBuilder result = new StringBuilder();
		String line = null;
		BufferedReader reader = request.getReader();
		while ((line = reader.readLine()) != null)
			result.append(line);
		return result;
	}

}
