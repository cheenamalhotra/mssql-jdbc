package com.microsoft.sqlserver.jdbc;

public class InformationType {
    public String name;
    public String id;

    public InformationType(String name, String id)
    {
        this.name = name;
        this.id = id;
    }
    
    public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}
}