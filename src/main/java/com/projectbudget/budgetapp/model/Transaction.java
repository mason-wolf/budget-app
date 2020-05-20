package com.projectbudget.budgetapp.model;

public class Transaction {
	
	private int transactionId;
	private String owner;
	private Double income;
	private String date;
	private Boolean archived;
	private Double amount;
	private String category;
	private String account;
	
	public Transaction()
	{
		
	}
	public Transaction(String category, double amount)
	{
		this.category = category;
		this.amount = amount;
	}
	public int getTransactionId() {
		return transactionId;
	}
	
	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public Double getIncome() {
		return income;
	}
	
	public void setIncome(Double income) {
		this.income = income;
	}
	
	public String getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public Boolean isArchived()
	{
		return this.archived;
	}
	
	public void archive(Boolean status)
	{
		this.archived = status;
	}
	
	public Double getAmount() {
		return this.amount;
	}
	
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getAccount() {
		return account;
	}
	
	public void setAccount(String account) {
		this.account = account;
	}
}
