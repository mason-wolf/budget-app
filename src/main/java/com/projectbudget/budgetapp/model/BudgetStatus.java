package com.projectbudget.budgetapp.model;

public class BudgetStatus {
	
	int budgetId;
	String category;
	double budgetAmount;
	double budgetSpent;
	public double percentageSpent;
	
	public BudgetStatus ()
	{

	}
	
	public int getBudgetId()
	{
		return budgetId;
	}
	
	public void setBudgetId(int budgetId)
	{
		this.budgetId = budgetId;
	}
	
	public double getPercentageSpent()
	{
		this.percentageSpent = (budgetSpent / budgetAmount) * 100;
		return percentageSpent;
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
