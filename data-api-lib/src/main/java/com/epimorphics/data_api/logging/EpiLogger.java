package com.epimorphics.data_api.logging;

import org.slf4j.LoggerFactory;
import org.slf4j.helpers.SubstituteLogger;
import org.slf4j.Logger;


public class EpiLogger {

	final Logger delegate;

	public EpiLogger(Logger delegate) {
		this.delegate = delegate;
	}

	public static EpiLogger createFrom(Class c) {
		return toEpiLogger(LoggerFactory.getLogger(c));
	}

	public static EpiLogger toEpiLogger(Logger logger) {
		return new EpiLogger(new SubstituteLogger(logger.getName()));
	}

	public void info(String s) {
		delegate.info(QueryID.getQueryId(), s);
	}

	public void error(String s) {
		delegate.error(QueryID.getQueryId(), s);
	}

	public void error(String s, Exception e) {
		// TODO
	}


}
