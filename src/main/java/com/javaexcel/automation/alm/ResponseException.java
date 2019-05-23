package com.javaexcel.automation.alm;

import javax.ws.rs.core.Response;

public class ResponseException extends Exception
{
    private final Response response;
    
    public ResponseException(Response response, String uri)
    {
        super(String.format("Invalid HTTP response code %s; %s", response.getStatus()+"\n"+response.getCookies()+"\n"+response.toString(), uri));
        
        this.response = response;
    }
    
    public Response response()
    {
        return response;
    }
}
