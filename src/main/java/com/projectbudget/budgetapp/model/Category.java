package com.projectbudget.budgetapp.model;


public class Category {
	
	private int categoryId;
	private String owner;
	private String title;
	
	public Category(String title)
	{
		this.title = title;
	}
	
	public Category() {}
	
	public int getCategoryId() {
		return categoryId;
	}
	
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

}
