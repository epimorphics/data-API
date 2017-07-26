package com.epimorphics.data_api.logging;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class QueryID implements Filter {

	@Override public void init(FilterConfig filterConfig) throws ServletException {
		// No init needed yet.
	}

	
	public static ThreadLocal<String> queryID = new ThreadLocal<String>();
	
	static final String ZOG = "EPI_ID";
	static final String PAR = "EPI_PAR";
	
	@Override public void doFilter(
		ServletRequest request
		, ServletResponse response
		, FilterChain chain
		)
		throws IOException, ServletException {
		HttpServletResponse httpResponse = (HttpServletResponse)response;
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		String query = httpRequest.getQueryString();
		String path = httpRequest.getRequestURI();
		
		String ID = null;
		String headerID = httpRequest.getHeader(ZOG);
		String paramID = httpRequest.getParameter(PAR);

		if (ID == null) ID = paramID;
		if (ID == null) ID = headerID;
		if (ID == null) ID = generateID();

		setQueryId(ID);
        chain.doFilter(request, response);
    
	}

	private void setQueryId(String id) {
		queryID.set(id);
	}

	public static String getQueryId() {
		return queryID.get();
	}

	private String generateID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public void destroy() {	
		// No action needed.
	}

}

//
//ELog.setQueryId(ID);
//
//long requestCount = queryCount.incrementAndGet();	        
//String seqId = Long.toString(requestCount);
//
//ELog.setSeqID(seqId);
//
//log.info(String.format
//	( "Request  [%s, %s] : %s"
//	, seqId
//	, ID
//	, path) + (query == null ? "" : ("?" + query))
//	);
//
//httpResponse.addHeader(X_RESPONSE_ID, "[" + seqId + ", " + ID + "]");			
//
//long startTime = System.currentTimeMillis();
//chain.doFilter(request, response);
//long endTime = System.currentTimeMillis();
//
//int status = getStatus(httpResponse);
//String statusString = status < 0 ? "(status unknown)" : "" + status;
//
//log.info(String.format
//	( "Response [%s, %s] : %s (%s)"
//	, seqId
//	, ID
//	, statusString
//    , NameUtils.formatDuration(endTime - startTime) ) 
//    );
//
//} else {
//chain.doFilter(request, response);
//}
//}
//    if (logThis) {
//        long transaction = transactionCount.incrementAndGet();	        
//        long start = System.currentTimeMillis();
//        log.info(String.format("Request  [%d] : %s", transaction, path) + (query == null ? "" : ("?" + query)));
//        httpResponse.addHeader(REQUEST_ID_HEADER, Long.toString(transaction));
//        chain.doFilter(request, response);
//        
//        String queryID = ELog.getQueryId();
//        int status = getStatus(httpResponse);
//        String statusString = status < 0 ? "(status unknown)" : "" + status;
//		log.info(String.format
//			( "Response [%d, %s] : %s (%s)"
//			, transaction
//			, queryID
//			, statusString
//            , NameUtils.formatDuration(System.currentTimeMillis() - start) ) 
//            );
//		
//}
//
//// The check for NoSuchMethodError is because Tomcat6 doesn't have a
//// getStatus() in its HttpServletResponse implementation, and we have
//// users still on Tomcat 6. -1 is a "no not really" value.
//private int getStatus(HttpServletResponse httpResponse) {
//	try { return httpResponse.getStatus(); }
//	catch (NoSuchMethodError e) { return -1; }
//}