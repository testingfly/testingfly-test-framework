package com.javaexcel.automation.alm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

public class LoggingFilter implements ClientRequestFilter, ClientResponseFilter, WriterInterceptor {
	private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());	
	private static final String ENTITY_STREAM_PROPERTY = LoggingFilter.class.getName() + ".entityLogger";
	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	private void log(StringBuilder sb) {
		//LOGGER.info(sb.toString());
		//System.out.println("Request: "+sb.toString());
	}

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
//    	System.out.println("\nDebug Method: "+requestContext.getMethod());
//    	System.out.println("Debug URI: "+requestContext.getUri());
//    	System.out.println("Debug Headers: "+requestContext.getHeaders().toString());
    	
		
		if (requestContext.hasEntity()) {
            OutputStream stream = new LoggingStream(requestContext.getEntityStream());
            requestContext.setEntityStream(stream);
            requestContext.setProperty(ENTITY_STREAM_PROPERTY, stream);
        }
    	
    	if(requestContext.getEntity()!=null){
//    		System.out.println("Debug Request type: "+((Run)requestContext.getEntity()).type() );
//    		System.out.println("Debug Request name: "+((Run)requestContext.getEntity()).name() );
//    		System.out.println("Debug Request subtype: "+((Run)requestContext.getEntity()).testType() );
//    		System.out.println("Debug Request testId: "+((Run)requestContext.getEntity()).testId() );
//    		System.out.println("Debug Request status: "+((Run)requestContext.getEntity()).status() );
//    		System.out.println("Debug Request testCycleId: "+((Run)requestContext.getEntity()).testCycleId() );
//    		System.out.println("Debug Request testcycl_id: "+((Run)requestContext.getEntity()).testcycl_id() );
//    		System.out.println("Debug Request owner: "+((Run)requestContext.getEntity()).owner() );
    	}
    	
    }

	@Override
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        
        if(responseContext.getStatus()==401){
        	//System.out.println(responseContext.getHeaders());
        	//System.err.println("Login failed: Invalid username or password. Please verify the login credentials. Aborting task.");
        	//System.exit(1);
        }
		//System.out.println("Response: "+responseContext.getLocation()+": "+responseContext.getStatus());
	}

	@Override
	public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
		// TODO Auto-generated method stub
		LoggingStream stream = (LoggingStream) 
                context.getProperty(ENTITY_STREAM_PROPERTY);
		context.proceed();
		if (stream != null) {
		log(stream.getStringBuilder(DEFAULT_CHARSET));
}
	}

	
}