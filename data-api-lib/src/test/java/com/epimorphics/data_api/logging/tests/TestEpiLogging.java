package com.epimorphics.data_api.logging.tests;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.helpers.SubstituteLogger;

import com.epimorphics.data_api.logging.EpiLogger;

public class TestEpiLogging {

	@Test public void t() {
		EpiLogger l = EpiLogger.createFrom(TestEpiLogging.class);
		
	}

	static class LoggingHistory extends SubstituteLogger {
		
		LoggingHistory(Logger delegate) {
			super(delegate.getName());
		}
		
		@Override public void info(String s) {
			super.info(s);
		}
		
		@Override public void error(String s) {
			super.error(s);
		}
		
		@Override public void error(String s, Throwable e) {
			super.error(s,  e);
		}
	}


}
