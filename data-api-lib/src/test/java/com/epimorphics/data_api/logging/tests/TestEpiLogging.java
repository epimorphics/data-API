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

	@Test public void t() {
		l.info("INFORMATIVE");
		assertEquals(expect("INFO MESSAGEID INFORMATIVE"), h.history);
	}
	
	List<List<String>> expect(String x) {
		List<List<String>> result = new ArrayList<List<String>>();
		for (String line: x.split(";")) {
			result.add(chopAtSpace(line));
		}
		return result;
	}

	private List<String> chopAtSpace(String line) {
		return Arrays.asList(line.split(" "));
	}

	/**
	 	Saves logged messages for later comparison
	*/
	public static class LoggingHistory extends NullLogger {
		
		final List<List<String>> history = new ArrayList<List<String>>();
		
		public LoggingHistory(String name) {
			super(name);
		}
		
		@Override public void info(String format, Object x) {
			System.err.println(">> info; x = " + x);
			System.err.println(">> format; x = " + format);
			history.add(Arrays.asList("INFO", format, x.toString()));
		}
		
		@Override public void error(String s) {
			history.add(Arrays.asList("ERROR", s));
		}
		
		@Override public void error(String s, Throwable e) {
			history.add(Arrays.asList("ERROR", s));
		}
	}


}
