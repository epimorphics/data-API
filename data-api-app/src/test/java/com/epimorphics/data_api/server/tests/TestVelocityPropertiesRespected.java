package com.epimorphics.data_api.server.tests;

import static org.junit.Assert.*;

import java.io.File;

import javax.servlet.ServletException;
import javax.validation.constraints.AssertTrue;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestVelocityPropertiesRespected {
	
	static Tomcat tomcat;
	
	static final String logFile = "/tmp/appbase-velocity.log";
	
	@BeforeClass public static void setUp() throws ServletException, LifecycleException {
		
		new File(logFile).delete();
		
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
	
	    tomcat.addWebapp(contextPath,  rootF.getAbsolutePath());
	    tomcat.start();
		
	}
	
	@AfterClass public static void tearDown() throws LifecycleException {
		tomcat.stop();
	}
	
    @Test public void runMe() {
    	assertTrue("velocity properties not created in initialisation", new File(logFile).exists());
    }

}
