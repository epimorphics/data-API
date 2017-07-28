package com.epimorphics.data_api.logging.tests;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.Test;
import static org.junit.Assert.*;

import com.epimorphics.data_api.logging.QueryID;

public class TestQueryID {

	Response httpResponse = new Response();
	Request httpRequest = new Request();
	FilterConfig config = makeFilterConfig();
	QueryID filter = new QueryID();
	
	String filterId = filter.getDefaultId();
	
	@Test public void testNeitherHeaderNorParam() throws IOException, ServletException {
		testThat(null, null, "DEFAULT");
	}	
	
	@Test public void testHeader() throws IOException, ServletException {
		testThat("fromHeader", null, "fromHeader");
	}	
	
	@Test public void testParam() throws IOException, ServletException {
		testThat(null, "fromParam", "fromParam");
	}	
	
	@Test public void testHeaderAndParam() throws IOException, ServletException {
		testThat("fromHeader", "fromParam", "fromParam");
	}
	
	private void testThat(String header, String param, String expect) throws IOException, ServletException {
		filter.init(config);

		if (header != null) httpRequest.headers.put(QueryID.X_REQUEST_ID, header);
		if (param != null) httpRequest.params.put(QueryID.QUERY_ID_PARAM, param);
		
		System.setProperty(QueryID.DSAPI_INSTANCE, "DEFAULT");
		
		LogChain chain = new LogChain();
		
		filter.doFilter(httpRequest, httpResponse, chain);

		assertTrue("the chain was not invoked", chain.wasInvoked);
		assertEquals("wrong generated value: ", expect, httpResponse.headers.get(QueryID.X_RESPONSE_ID));
	}
	

	private FilterConfig makeFilterConfig() {
		FilterConfig f = new FilterConfig() {

			@Override public String getFilterName() {
				return null;
			}

			@Override public ServletContext getServletContext() {
				return null;
			}

			@Override public String getInitParameter(String name) {
				return null;
			}

			@Override public Enumeration<String> getInitParameterNames() {
				return null;
			}};
		return f;
	}
	
	static class LogChain implements FilterChain {

		boolean wasInvoked = false;
		
		@Override public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
			wasInvoked = true;
		}
		
	}
}
