package com.projectbudget.budgetapp.dao;

import java.util.List;

import com.projectbudget.budgetapp.model.Account;
import com.projectbudget.budgetapp.model.Budget;
import com.projectbudget.budgetapp.model.BudgetStatus;
import com.projectbudget.budgetapp.model.Category;
import com.projectbudget.budgetapp.model.Transaction;

public interface AccountDao {
	
	public void addAccount(String username, Account account);
	public Account getAccount(String username);
	public void updateBalance(String username, double amount);
	public void addTransaction(Transaction transaction);
	public void addBudgetItem(String username, Budget budgetItem);
	public void deleteBudgetItem(int budgetId);
	public List<Budget> getBudgetByCategory(String username);
	public List<Budget> getTotalBudgeted(String username);
	public Transaction getTransaction(int transactionId);
	public void deleteTransasction(int transactionId);
	public List<Transaction> getTransactionHistory(String username);
	public List<Transaction> getTransactionsByCategory(String username);
	public List<Transaction> getTotalSpentByCategory(String username);
	public List<Category> getBudgetCategories(String username);
	public List<BudgetStatus> getbudgetStatus(String username);
	public void addTransactionCategory(String username, String categoryTitle);
	public String getTransactionCategory(int categoryId);
	public void deleteTransactionCategory(int categoryId);
}
