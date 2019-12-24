package com.projectbudget.budgetapp.dao;

import java.util.List;

import com.projectbudget.budgetapp.model.Account;
import com.projectbudget.budgetapp.model.Budget;
import com.projectbudget.budgetapp.model.Category;
import com.projectbudget.budgetapp.model.Transaction;

public interface AccountDao {
	
	public void addAccount(String username, Account account);
	public Account getAccount(String username);
	public void addIncome(String username, double amount);
	public List<Budget> getBudget(String username);
	public List<Transaction> getTransactions(String username);
	public List<Category> getTransactionCategories(String username);
	public void addTransactionCategory(String username, String categoryTitle);
	public void deleteTransactionCategory(int categoryId);
}
