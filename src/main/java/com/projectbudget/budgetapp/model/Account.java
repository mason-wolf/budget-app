package com.projectbudget.budgetapp.model;

public class Account {
	
	private int id;
	private String accountOwner;
	private String accountName;
	private String accountType;
	private double balance;
	private Boolean isPrimary;
	private String budgetStartDate;
	private String budgetEndDate;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getAccountOwner() {
		return accountOwner;
	}
	
	public void setAccountOwner(String accountOwner) {
		this.accountOwner = accountOwner;
	}
	
	public String getAccountName() {
		return accountName;
	}
	
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	
	public String getAccountType()
	{
		return accountType;
	}
	
	public void setAccountType(String accountType)
	{
		this.accountType = accountType;
	}
	
	public double getBalance() {
		return balance;
	}
	
	public void setBalance(double balance) {
		this.balance = balance;
	}
	
	public Boolean isPrimary()
	{
		return isPrimary;
	}
	
	public void isPrimary(boolean isPrimary)
	{
		this.isPrimary = isPrimary;
	}
	
	public String getBudgetStartDate() {
		return budgetStartDate;
	}
	
	public void setBudgetStartDate(String budgetStartDate) {
		this.budgetStartDate = budgetStartDate;
	}
	
	public String getBudgetEndDate() {
		return budgetEndDate;
	}
	
	public void setBudgetEndDate(String budgetEndDate) {
		this.budgetEndDate = budgetEndDate;
	}
	
}
