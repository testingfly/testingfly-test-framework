package com.javaexcel.automation.core.data;

public class MultiUserInfo {
	private Integer id=-1;
	private String applicationName=new String();
	private String loginID=new String();
	private String password=new String();
	private String role=new String();
	private String application_Area=new String();
	private String status=new String();
	private String parallel_Login=new String();
	private String application_Type=new String();
	private String url=new String();
	private String firstName;
	private String lastName;
	private String secretKey;
	
	
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public String getLoginID() {
		return loginID;
	}
	public void setLoginID(String loginID) {
		this.loginID = loginID;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getApplication_Area() {
		return application_Area;
	}
	public void setApplication_Area(String application_Area) {
		this.application_Area = application_Area;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getParallel_Login() {
		return parallel_Login;
	}
	public void setParallel_Login(String parallel_Login) {
		this.parallel_Login = parallel_Login;
	}
	public String getApplication_Type() {
		return application_Type;
	}
	public void setApplication_Type(String application_Type) {
		this.application_Type = application_Type;
	}
}
