package com.projectbudget.budgetapp.model;

public class User {
	
	int userId;
	String username;
	String password;
	
	public User(String username, String password)
	{
		this.username = username;
		this.password = password;
	}
	
	public User() { } ;
	
	public int getUserId()
	{
		return userId;
	}
	
	public void setUserId(int userId)
	{
		this.userId = userId;
	}
	
	public String getUserName()
	{
		return username;
	}
	
	public void setUserName(String username)
	{
		this.username = username;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public void setPassword(String password)
	{
		this.password = password;
	}
	
}