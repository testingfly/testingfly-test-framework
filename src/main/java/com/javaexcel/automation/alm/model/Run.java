package com.javaexcel.automation.alm.model;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "Entity")
public class Run extends Entity
{
    public static final String STATUS_BLOCKED = "Blocked";
    public static final String STATUS_FAILED = "Failed";
    public static final String STATUS_NA = "N/A";
    public static final String STATUS_NO_RUN = "No Run";
    public static final String STATUS_NOT_COMPLETED = "Not Completed";
    public static final String STATUS_PASSED = "Passed";

    public static final String TEST_TYPE_AUTO = "hp.qc.run.external-test";

    public Run(Entity entity)
    {
        super(entity);
    }

    public Run()
    {
        type("run");
    }

    public String testInstanceId()
    {
        return fieldValue("testcycl-id");
    }

    public void testInstanceId(String value)
    {
        fieldValue("testcycl-id", value);
    }

    public String testCycleId()
    {
        return fieldValue("cycle-id");
    }

    public void testCycleId(String value)
    {
        fieldValue("cycle-id", value);
    }

    public String testId()
    {
        return fieldValue("test-id");
    }

    public void testId(String value)
    {
        fieldValue("test-id", value);
    }

    public String testConfigId()
    {
        return fieldValue("test-config-id");
    }

    public void testConfigId(String value)
    {
        fieldValue("test-config-id", value);
    }

    public String status()
    {
        return fieldValue("status");
    }

    public void status(String value)
    {
        fieldValue("status", value);
    }

    public String owner()
    {
        return fieldValue("owner");
    }

    public void owner(String value)
    {
        fieldValue("owner", value);
    }

    public String testType()
    {
        return fieldValue("subtype-id");
    }

    public void testType(String value)
    {
        fieldValue("subtype-id", value);
    }

    public String host()
    {
        return fieldValue("host");
    }

    public void host(String value)
    {
        fieldValue("host", value);
    }

    public String comments()
    {
        return fieldValue("comments");
    }

    public void comments(String value)
    {
        fieldValue("comments", value);
    }

    public String duration()
    {
        return fieldValue("duration");
    }

    public void duration(String value)
    {
        fieldValue("duration", value);
    }
    
    public String testcycl_id()
    {
        return fieldValue("testcycl-id");
    }

    public void testcycl_id(String value)
    {
        fieldValue("testcycl-id", value);
    }
}
