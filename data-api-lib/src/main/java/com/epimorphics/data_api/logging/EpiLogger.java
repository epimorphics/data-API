package com.epimorphics.data_api.logging;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class EpiLogger {

	public final Logger delegate;

	public EpiLogger(Logger delegate) {
		this.delegate = delegate;
	}

	public static EpiLogger createFrom(Class c) {
		return toEpiLogger(LoggerFactory.getLogger(c));
	}

	public static EpiLogger toEpiLogger(Logger logger) {
		return new EpiLogger(logger);
	}

	public void info(String format) {
		delegate.info(format + " %s ", QueryID.getQueryId());
	}

	public void error(String format) {
		delegate.error("%s" + format, QueryID.getQueryId());
	}

	public void error(String format, Throwable e) {
		delegate.error("%s" + format, QueryID.getQueryId(), e);
	}

}
