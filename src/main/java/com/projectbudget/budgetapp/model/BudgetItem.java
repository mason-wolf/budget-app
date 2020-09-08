package com.projectbudget.budgetapp.model;

public class BudgetItem {

	private int budgetId;
	private String owner;
	private String category;
	private Boolean archived;
	private String startDate;
	private String endDate;
	private double amount;
	
	public int getBudgetId() {
		return budgetId;
	}
	
	public void setBudgetId(int budgetId) {
		this.budgetId = budgetId;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public Boolean getArchived() {
		return archived;
	}
	
	public void setArchived(Boolean archived) {
		this.archived = archived;
	}
	
	public String getStartDate() {
		return startDate;
	}
	
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	
	public String getEndDate() {
		return endDate;
	}
	
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public void setAmount(double amount) {
		this.amount = amount;
	}
}
