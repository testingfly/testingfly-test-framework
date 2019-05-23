package com.javaexcel.automation.core.swagger;

import com.javaexcel.automation.core.enums.ParameterType;
import com.javaexcel.automation.core.enums.ParameterDataType;

public class ParameterInfo {
	
	
	private String parameterName;
	private ParameterType parameterType;
	private ParameterDataType parameterDataType = ParameterDataType.Unknown;
	private Boolean isRequired=false;
	private int minLength = -1;
	private int maxLength = -1;
	private int[] statusCodes;
	public ParameterInfo(){}
	public ParameterInfo(String parameterName, ParameterType parameterType, ParameterDataType parameterDataType, Boolean isRequired, int minLength, int maxLength, int[] statusCodes)
	{		
		this.parameterName = parameterName.toLowerCase();
		this.parameterType = parameterType;
		this.parameterDataType = parameterDataType; 
		this.isRequired = isRequired;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.statusCodes = statusCodes;
	}
	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}
	
	public ParameterType getParameterType()
	{
		return parameterType;
	}
	public void setParameterType(ParameterType parameterType)
	{
		this.parameterType = parameterType;
	}
	
	public ParameterDataType getParameterDataType()
	{
		return parameterDataType;
	}
	public void setParameterDataType(ParameterDataType parameterDataType)
	{
		this.parameterDataType = parameterDataType;
	}
	
	public Boolean getIsRequired() {
		return isRequired;
	}

	public void setIsRequired(Boolean isRequired) {
		this.isRequired = isRequired;
	}

	public int getMinLength() {
		return minLength;
	}

	public void setMinLength(int minLength) {
		this.minLength = minLength;
	}
	
	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
	
	public void setStatusCodes(int[] statusCodes)
	{
		this.statusCodes = statusCodes;
	}
	
	public int[] getStatusCodes()
	{
		return statusCodes;
	}
	public String toString(){
		try
		{
		return parameterName + "|" + 
				parameterType.toString() + "|" +
				parameterDataType.toString() + "|" +
				isRequired.toString() + "|" +  
				String.valueOf(minLength) + "|" +
				String.valueOf(maxLength) + "|";
		}
		catch(Exception exp){return "null";}
		//		
	}
}
