package com.javaexcel.automation.core.utils;

public class TokenDetails {
private String access_token;
private String scope;
private String token_type;
private String expires_in;
public String getAccess_token() {
	return access_token;
}
public void setAccess_token(String access_token) {
	this.access_token = access_token;
}
public String getScope() {
	return scope;
}
public void setScope(String scope) {
	this.scope = scope;
}
public String getToken_type() {
	return token_type;
}
public void setToken_type(String token_type) {
	this.token_type = token_type;
}
public String getExpires_in() {
	return expires_in;
}
public void setExpires_in(String expires_in) {
	this.expires_in = expires_in;
}
@Override
public String toString() {
	return "TokenDetails [access_token=" + access_token + ", scope=" + scope
			+ ", token_type=" + token_type + ", expires_in=" + expires_in + "]";
}
}
