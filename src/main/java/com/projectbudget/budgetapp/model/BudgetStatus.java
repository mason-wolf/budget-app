package com.projectbudget.budgetapp.model;

public class BudgetStatus {
	
	String category;
	double budgetAmount;
	double budgetSpent;
	
	public BudgetStatus ()
	{
		
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public double getBudgetAmount() {
		return budgetAmount;
	}

	public void setBudgetAmount(double budgetAmount) {
		this.budgetAmount = budgetAmount;
	}

	public double getBudgetSpent() {
		return budgetSpent;
	}

	public void setBudgetSpent(double budgetSpent) {
		this.budgetSpent = budgetSpent;
	}


}
