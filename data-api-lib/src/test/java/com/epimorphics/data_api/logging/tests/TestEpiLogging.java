package com.epimorphics.data_api.logging.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.epimorphics.data_api.logging.EpiLogger;
import com.epimorphics.data_api.logging.QueryID;

public class TestEpiLogging {
	
	final LoggingHistory h = new LoggingHistory("TestEpiLogging");
	final EpiLogger l = EpiLogger.toEpiLogger(h);

	@Before public void setUp() {
		QueryID.setQueryId("MESSAGEID");
	}

	@Test public void testWarn() {
		l.warn("INFORMATIVE");
		assertEquals(expect("WARN INFORMATIVE_%s MESSAGEID"), h.history);
	}

	@Test public void testIdDebugIsenabled() {
		assertFalse(l.isDebugEnabled());
		assertEquals(expect("DEBUG ISENABLED"), h.history);
	}
	
	@Test public void testDebug() {
		l.debug("DEBUGGING");
		assertEquals(expect("DEBUG DEBUGGING_%s MESSAGEID"), h.history);
	}

	@Test public void testInfo() {
		l.info("INFORMATIVE");
		assertEquals(expect("INFO INFORMATIVE_%s MESSAGEID"), h.history);
	}

	 @Test public void testError() {
		l.error("UNFORTUNATE");
		assertEquals(expect("ERROR UNFORTUNATE%s MESSAGEID"), h.history);
	}

	@Test public void testErrorWithException() {
		l.error("DISTRESS", new RuntimeException());
		assertEquals(expect("ERROR %sDISTRESS MESSAGEID"), h.history);
	}
	
	List<List<String>> expect(String x) {
		List<List<String>> result = new ArrayList<List<String>>();
		for (String line: x.split(";")) {
			result.add(chopAtSpace(line));
		}
		return result;
	}

	private List<String> chopAtSpace(String line) {
		List<String> items = Arrays.asList(line.split(" "));
		for (int i = 0; i < items.size(); i += 1) {
			items.set(i, items.get(i).replaceAll("_",  " "));
		}
		return items;
	}

	/**
	 	Saves logged messages for later comparison
	*/
	public static class LoggingHistory extends FailLogger {
		
		final List<List<String>> history = new ArrayList<List<String>>();
		
		public LoggingHistory(String name) {
			super(name);
		}
		
		@Override public void debug(String format, Object x) {
			history.add(Arrays.asList("DEBUG", format, x.toString()));
		}
		
		@Override public void warn(String format, Object x) {
			history.add(Arrays.asList("WARN", format, x.toString()));
		}
		
		@Override public boolean isDebugEnabled() {
			history.add(Arrays.asList("DEBUG", "ISENABLED"));
			return false;
		}
		
		@Override public void info(String format, Object x) {
			history.add(Arrays.asList("INFO", format, x.toString()));
		}
		
		@Override public void error(String s) {
			history.add(Arrays.asList("ERROR", s));
		}
		
		@Override public void error(String format, Object arg) {
			history.add(Arrays.asList("ERROR", format, arg.toString()));
		}
		
		@Override public void error(String format, Throwable e) {
			history.add(Arrays.asList("ERROR", format));
		}
	}


}
