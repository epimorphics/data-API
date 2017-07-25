package com.epimorphics.data_api.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class QueryID implements Filter {

	@Override public void init(FilterConfig filterConfig) throws ServletException {
		// No init needed yet.
	}

	@Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {

		System.err.println("BEFORE");
        chain.doFilter(request, response);
        System.err.println("AFTER");
    
	}

	@Override public void destroy() {	
		// No action needed.
	}

}
