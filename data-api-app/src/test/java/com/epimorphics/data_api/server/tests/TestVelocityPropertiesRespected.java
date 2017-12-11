package com.epimorphics.data_api.server.tests;

import java.io.File;

import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestVelocityPropertiesRespected {
	
	static Tomcat tomcat;
	
	@BeforeClass public static void setUp() throws ServletException, LifecycleException {
		
		System.err.println(">> discard any existing logfile");
		
		String root = "src/main/webapp";

	    tomcat = new Tomcat();
	    tomcat.setPort(8080);
	
	    tomcat.setBaseDir(".");
	
	    String contextPath = "/dsapi";
	
	    File rootF = new File(root);
	    if (!rootF.exists()) {
	        rootF = new File(".");
	    }
	    if (!rootF.exists()) {
	        System.err.println("Can't find root app: " + root);
	        System.exit(1);
	    }
	
	    System.err.println(">> about to add webapp.");
	    tomcat.addWebapp(contextPath,  rootF.getAbsolutePath());
	    System.err.println(">> starting ...");
	    tomcat.start();
		
	}
	
	@AfterClass public static void tearDown() throws LifecycleException {
		tomcat.stop();
	}
	
    @Test public void runMe() {
    	System.err.println(">> test that logfile has been created.");
    }

}
