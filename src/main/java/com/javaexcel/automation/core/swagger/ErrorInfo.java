package com.javaexcel.automation.core.swagger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

//import com.wellsfargo.automation.core.data.Configurables;
//import com.wellsfargo.automation.core.utils.Utils;

public class ErrorInfo
{
	private String errorCode;
	private String statusLine;
	private String errorDescription;
	
	private String fieldNameValidation = "errors[%d]field_name#%s";
	private String fieldValueValidation = "errors[%d]field_value#%s";
	private String fieldValueValidationForEmpty = "errors[%d]field_value$Exist";
	private String apiSpecificationUrl = "errors[%d]api_specification_url#%s";
	
	public ErrorInfo(String statusLine, String errorCode, String errorDescription )
	{
		this.statusLine = statusLine;
		this.errorCode = errorCode;		
		this.errorDescription = errorDescription;
	}
	
	public String getFieldNameValidation()
	{
		return fieldNameValidation;
	}
	
	public String getFieldValueValidation()
	{
		return fieldValueValidation;
	}
	
	public String getFieldValueValidationForEmpty()
	{
		return fieldValueValidationForEmpty;
	}
	
	public String getAPISpecificationUrl()
	{
		return apiSpecificationUrl;
	}
	// Hello
	public String getErrorCode()
	{		
		return errorCode;
	}
	
	public String getStatusLine()
	{
		return statusLine;
	}
	
	public String getErrorDescription()
	{
		return errorDescription;
	}
	
}
