package com.projectbudget.budgetapp.dao;

import java.util.List;

import com.projectbudget.budgetapp.model.Account;
import com.projectbudget.budgetapp.model.BudgetItem;
import com.projectbudget.budgetapp.model.BudgetStatus;
import com.projectbudget.budgetapp.model.Category;
import com.projectbudget.budgetapp.model.Transaction;

public interface AccountDao {
	
	public void addAccount(String username, Account account);
	public Account getAccount(String username);
	public void archiveAccount(Account account);
	public void updateBalance(String username, double amount);
	public void addTransaction(Transaction transaction, boolean archived);
	public void addBudgetItem(String username, BudgetItem budgetItem);
	public void deleteBudgetItem(int budgetId);
	public List<BudgetItem> getBudgetByCategory(String username, int month, int year);
	public List<BudgetItem> getTotalBudgeted(String username);
	public List<BudgetItem> getAllBudgetArchives(String username);
	public Double getTotalNotBudgeted(String username);
	public Double getAmountEarned(String username, int month, int year);
	public Transaction getTransaction(int transactionId);
	public void deleteTransasction(int transactionId);
	public List<Transaction> getTransactionHistory(String username);
	public List<Transaction> getTransactionsByCategory(String username);
	public List<Transaction> getTotalSpentByCategory(String username, int month, int year);
	public List<Category> getBudgetCategories(String username);
	public List<BudgetStatus> getBudgetStatus(String username);
	public List<BudgetStatus> getBudgetArchive(String username, int month, int year);
	public void addTransactionCategory(String username, String categoryTitle);
	public String getTransactionCategory(int categoryId);
	public void deleteTransactionCategory(int categoryId);
}
