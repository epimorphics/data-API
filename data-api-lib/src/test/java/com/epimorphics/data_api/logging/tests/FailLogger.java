package com.epimorphics.data_api.logging.tests;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
	A Logger that throws away every message.
*/
public class FailLogger implements Logger {

	protected final String name;
	
	public FailLogger(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isTraceEnabled() {
        throw new RuntimeException();
	}

	@Override
	public void trace(String msg) {
        throw new RuntimeException();
		
	}

	@Override
	public void trace(String format, Object arg) {
        throw new RuntimeException();
		
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
        throw new RuntimeException();
		
	}

	@Override
	public void trace(String format, Object... arguments) {
        throw new RuntimeException();
		
	}

	@Override
	public void trace(String msg, Throwable t) {
        throw new RuntimeException();
		
	}

	@Override
	public boolean isTraceEnabled(Marker marker) {
        throw new RuntimeException();
	}

	@Override
	public void trace(Marker marker, String msg) {
        throw new RuntimeException();
		
	}

	@Override
	public void trace(Marker marker, String format, Object arg) {
        throw new RuntimeException();
		
	}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) {
        throw new RuntimeException();
		
	}

	@Override
	public void trace(Marker marker, String format, Object... argArray) {
        throw new RuntimeException();
		
	}

	@Override
	public void trace(Marker marker, String msg, Throwable t) {
        throw new RuntimeException();
		
	}

	@Override
	public boolean isDebugEnabled() {
        throw new RuntimeException();
	}

	@Override
	public void debug(String msg) {
        throw new RuntimeException();
		
	}

	@Override
	public void debug(String format, Object arg) {
        throw new RuntimeException();
		
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
        throw new RuntimeException();
		
	}

	@Override
	public void debug(String format, Object... arguments) {
        throw new RuntimeException();
		
	}

	@Override
	public void debug(String msg, Throwable t) {
        throw new RuntimeException();
		
	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
        throw new RuntimeException();
	}

	@Override
	public void debug(Marker marker, String msg) {
        throw new RuntimeException();
		
	}

	@Override
	public void debug(Marker marker, String format, Object arg) {
        throw new RuntimeException();
		
	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
        throw new RuntimeException();
		
	}

	@Override
	public void debug(Marker marker, String format, Object... arguments) {
        throw new RuntimeException();
		
	}

	@Override
	public void debug(Marker marker, String msg, Throwable t) {
        throw new RuntimeException();
		
	}

	@Override
	public boolean isInfoEnabled() {
        throw new RuntimeException();
	}

	@Override
	public void info(String msg) {
        throw new RuntimeException();
		
	}

	@Override
	public void info(String format, Object arg) {
        throw new RuntimeException();
		
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
        throw new RuntimeException();
		
	}

	@Override
	public void info(String format, Object... arguments) {
        throw new RuntimeException();
		
	}

	@Override
	public void info(String msg, Throwable t) {
        throw new RuntimeException();
		
	}

	@Override
	public boolean isInfoEnabled(Marker marker) {
        throw new RuntimeException();
	}

	@Override
	public void info(Marker marker, String msg) {
        throw new RuntimeException();
		
	}

	@Override
	public void info(Marker marker, String format, Object arg) {
        throw new RuntimeException();
		
	}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) {
        throw new RuntimeException();
		
	}

	@Override
	public void info(Marker marker, String format, Object... arguments) {
        throw new RuntimeException();
		
	}

	@Override
	public void info(Marker marker, String msg, Throwable t) {
        throw new RuntimeException();
		
	}

	@Override
	public boolean isWarnEnabled() {
        throw new RuntimeException();
	}

	@Override
	public void warn(String msg) {
        throw new RuntimeException();
		
	}

	@Override
	public void warn(String format, Object arg) {
        throw new RuntimeException();
		
	}

	@Override
	public void warn(String format, Object... arguments) {
        throw new RuntimeException();
		
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
        throw new RuntimeException();
		
	}

	@Override
	public void warn(String msg, Throwable t) {
        throw new RuntimeException();
		
	}

	@Override
	public boolean isWarnEnabled(Marker marker) {
        throw new RuntimeException();
	}

	@Override
	public void warn(Marker marker, String msg) {
        throw new RuntimeException();
		
	}

	@Override
	public void warn(Marker marker, String format, Object arg) {
        throw new RuntimeException();
		
	}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
        throw new RuntimeException();
		
	}

	@Override
	public void warn(Marker marker, String format, Object... arguments) {
        throw new RuntimeException();
		
	}

	@Override
	public void warn(Marker marker, String msg, Throwable t) {
        throw new RuntimeException();
		
	}

	@Override
	public boolean isErrorEnabled() {
        throw new RuntimeException();
	}

	@Override
	public void error(String msg) {
        throw new RuntimeException();
		
	}

	@Override
	public void error(String format, Object arg) {
        throw new RuntimeException();
		
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
        throw new RuntimeException();
		
	}

	@Override
	public void error(String format, Object... arguments) {
        throw new RuntimeException();
		
	}

	@Override
	public void error(String msg, Throwable t) {
        throw new RuntimeException();
		
	}

	@Override
	public boolean isErrorEnabled(Marker marker) {
        throw new RuntimeException();
	}

	@Override
	public void error(Marker marker, String msg) {
        throw new RuntimeException();
		
	}

	@Override
	public void error(Marker marker, String format, Object arg) {
        throw new RuntimeException();
		
	}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {
        throw new RuntimeException();
		
	}

	@Override
	public void error(Marker marker, String format, Object... arguments) {
        throw new RuntimeException();
		
	}

	@Override
	public void error(Marker marker, String msg, Throwable t) {
        throw new RuntimeException();
		
	}
	
}