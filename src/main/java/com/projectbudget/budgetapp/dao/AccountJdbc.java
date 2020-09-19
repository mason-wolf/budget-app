package com.projectbudget.budgetapp.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import com.mysql.cj.xdevapi.Statement;
import com.projectbudget.budgetapp.model.Account;
import com.projectbudget.budgetapp.model.BudgetItem;
import com.projectbudget.budgetapp.model.BudgetStatus;
import com.projectbudget.budgetapp.model.Category;
import com.projectbudget.budgetapp.model.Transaction;

public class AccountJdbc implements AccountDao{

	private DataSource dataSource;
	private JdbcTemplate jdbcTemplateObject;
	private static ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
	public static AccountJdbc query = (AccountJdbc)context.getBean("AccountJdbc");
	
	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	    this.jdbcTemplateObject = new JdbcTemplate(dataSource);
	}
	
	public DataSource getDataSource()
	{
		return this.dataSource;
	}

	@Override
	public void addAccount(String username, Account account) {
		
		String query = "insert into accounts (accountOwner, accountName, accountType, isPrimary, balance, budgetStartDate) values (?, ?, ?, ?, ?, ?)";
		jdbcTemplateObject.update(query, username, account.getAccountName(), account.getAccountType(), account.isPrimary(), account.getBalance(), account.getBudgetStartDate());
		
	}

	@Override
	public Account getAccount(String username) {
		
		String query = "select * from accounts where accountOwner = ? and isPrimary = 1";
		Account account = jdbcTemplateObject.queryForObject(query, new Object[] { username }, new AccountMapper());
		return account;
		
	}

	@Override
	public void updateBalance(String username, double amount) {
		
		String accountIdQuery = "select id from accounts where accountOwner = ? and isPrimary = 1";
		int accountId =  jdbcTemplateObject.queryForObject(accountIdQuery, new Object[] { username}, Integer.class);
		String addIncomeQuery = "update accounts set balance = ? where id = ?";
		jdbcTemplateObject.update(addIncomeQuery, amount, accountId);
		
	}

	@Override
	public List<BudgetItem> getBudgetByCategory(String username, int month, int year) {
		
		String query = "select * from budgets where owner = ? and archived = 0 and MONTH(startDate) = ? and YEAR(startDate) = ? group by category";
		List<BudgetItem> budget;
		
		try {
		budget = jdbcTemplateObject.query(query,  new Object[] { username, month, year}, new BudgetItemMapper());
		}
		catch (Exception e)
		{
			budget = null;
		}
		
		return budget;
		
	}

	@Override
	public List<Transaction> getTransactionsByCategory(String username) {
		
		String query = "select * from transactions where owner = ? and archived='false' group by category order by category";
		
		List<Transaction> transactions;
		
		try {
			
			transactions = jdbcTemplateObject.query(query, new Object[] { username },  new TransactionMapper());
			
		}
		catch (Exception e)
		{
			transactions = null;
		}
		
		return transactions;
	}

	@Override
	public List<Category> getBudgetCategories(String username) {
		
		String query = "select * from budgetCategories where owner = ? order by title";
		
		List<Category> categories = jdbcTemplateObject.query(query, new Object[] { username}, new CategoryMapper());
		return categories;
	}

	@Override
	public void addTransactionCategory(String username, String categoryTitle) {
		
		String query = "insert into budgetCategories (owner, title) values (?, ?)";
		jdbcTemplateObject.update(query, username, categoryTitle);
		
	}

	@Override
	public void deleteTransactionCategory(int categoryId) {
		
		String query = "delete from budgetCategories where id= ?";
		jdbcTemplateObject.update(query, categoryId);
	}

	/** Adds a transaction with an archive flag. True = Archived, False = Active (Not archived) 
	 * If a transaction is not archived, it is part of the active budget or current month.
	 * If a transaction is archived, it's part of an older budget.
	 * */
	@Override
	public void addTransaction(Transaction transaction, boolean archived) {
		
		String query;
		
		if (archived)
		{
			query = "insert into transactions (owner, date, amount, category, account, archived) values (?, ?, ?, ?, ?, '1')";
		}
		else
		{
			query = "insert into transactions (owner, date, amount, category, account, archived) values (?, ?, ?, ?, ?, '0')";
		}

		jdbcTemplateObject.update(query, transaction.getOwner(), transaction.getDate(), transaction.getAmount(), transaction.getCategory(), transaction.getAccount());
	}

	@Override
	public String getTransactionCategory(int categoryId) {
		String query = "select title from budgetCategories where id = ?";
		String category =  jdbcTemplateObject.queryForObject(query, new Object[] { categoryId }, String.class);
		return category;	
	}

	@Override
	public List<Transaction> getTotalSpentByCategory(String username, int month, int year) {
		String query = "select id, owner, archived, date, category, account, sum(amount) as amount from transactions where owner= ? "
				+ "and archived='0' and category != 'income' and MONTH(date) =? and YEAR(date) = ? group by category order by category";
		List<Transaction> transactions = jdbcTemplateObject.query(query, new Object[] { username, month, year }, new TransactionMapper());
		return transactions;
	}

	@Override
	public void addBudgetItem(String username, BudgetItem budgetItem) {
		String categoryQuery = "select title from budgetCategories where id=?";
		String category = jdbcTemplateObject.queryForObject(categoryQuery, new Object[] { budgetItem.getBudgetId() }, String.class);
		String budgetItemQuery = "select id from budgets where category = ? and owner = ? and archived=0";
		
		int budgetId;
		
		try 
		{
			// If the projected expense already exists, update the budget amount.
			budgetId = jdbcTemplateObject.queryForObject(budgetItemQuery, new Object[] { category, username }, Integer.class);
			String updateBudgetQuery = "update budgets set amount = ? where id = ? and owner=?";
			jdbcTemplateObject.update(updateBudgetQuery, budgetItem.getAmount(), budgetId, username);
		}
		catch(Exception e)
		{
			String query = "insert into budgets (owner, category, archived, startDate, endDate, amount) values (?, ?, ?, ?, ?, ?)";
			jdbcTemplateObject.update(query, username, category, budgetItem.getArchived(), budgetItem.getStartDate(), budgetItem.getEndDate(), budgetItem.getAmount());
		}
	}

	@Override
	public List<BudgetItem> getTotalBudgeted(String username) {
		
		String query = "select id, owner, category, archived, startDate, endDate, sum(amount) as amount from budgets where owner = ? and archived = '0' group by category";
		List<BudgetItem> budgetItemTotals = jdbcTemplateObject.query(query, new Object[] { username }, new  BudgetItemMapper());
		return budgetItemTotals;
		
	}

	@Override
	public void deleteBudgetItem(int budgetId) {
		String categoryQuery = "select category from budgets where id = ?";
		String category = jdbcTemplateObject.queryForObject(categoryQuery, new Object[] { budgetId }, String.class);
		String matchingCategories = "select * from budgets where category = ?";
		List<BudgetItem> budgetItems = jdbcTemplateObject.query(matchingCategories, new Object[] { category }, new BudgetItemMapper());
		
		for (BudgetItem budgetItem : budgetItems)
		{
			jdbcTemplateObject.update("delete from budgets where id= ? and archived = '0'", budgetItem.getBudgetId());
		}
	}

	/**
	 * Retrieves the status of the current budget. Budget Amount versus Budget Spent.
	 */
	@Override
	public List<BudgetStatus> getBudgetStatus(String username) {

		String query = "select budgets.category , sum(case when transactions.archived = 0 THEN transactions.amount else 0 END) as budgetSpent, budgets.amount as budgetAmount from budgets  \n" + 
				"				left join\n" + 
				"				transactions on budgets.category = transactions.category\n" + 
				"                where budgets.owner = ? and budgets.archived = 0\n" + 
				"				group by budgets.category";
		List<BudgetStatus> budgetStatus = jdbcTemplateObject.query(query, new Object[] { username }, new BudgetStatusMapper());
		return budgetStatus;
	}

	@Override
	public List<Transaction> getTransactionHistory(String username) {
		
		String query = "select * from transactions where owner= ? order by id desc limit 100";
		List<Transaction> transactionList = jdbcTemplateObject.query(query, new Object[] { username }, new TransactionMapper());
		return transactionList;
	}

	@Override
	public Transaction getTransaction(int transactionId) {
		String query = "select * from transactions where id = ? LIMIT 60";
		Transaction transaction = jdbcTemplateObject.queryForObject(query, new Object[] { transactionId }, new TransactionMapper());
		return transaction;
	}

	@Override
	public void deleteTransasction(int transactionId) {
		String query = "delete from transactions where id = ?";
		jdbcTemplateObject.update(query, transactionId);
	}

	@Override
	public void archiveAccount(Account account) {
		
		LocalDate currentDate = LocalDate.now();
		String accountQuery = "update accounts set budgetStartDate = ? where accountOwner = ?";
		String budgetQuery = "update budgets set archived=1 where owner = ?";
		String transactionQuery = "update transactions set archived=1 where owner = ?";
				
		// Update the user's account to reflect the new month, new budget.
		jdbcTemplateObject.update(accountQuery, currentDate, account.getAccountOwner());
		// Archive the budgeted items
		jdbcTemplateObject.update(budgetQuery, account.getAccountOwner());
		// Archive the transactions from last month
		jdbcTemplateObject.update(transactionQuery, account.getAccountOwner());
	}

	@Override
	public Double getTotalNotBudgeted(String username) {
		
		String budgetQuery = "select sum(transactions.amount) from transactions left join budgets on transactions.category = budgets.category \n" + 
				"where budgets.category IS NULL and transactions.category != \"Income\" and transactions.owner=? and transactions.archived = 0;";
		double result = 0 ;
		
		try 
		{
		result = jdbcTemplateObject.queryForObject(budgetQuery, new Object[] { username }, Double.class);
		}
		catch(Exception e)
		{
			result = 0;
		}
		
		return result;
	}

	/**
	 * Retrieves all archived budgets.
	 */
	@Override
	public List<BudgetItem> getBudgetArchive(String username) {
		
		String budgetQuery = "select * from budgets where archived=1 and owner = ?";
		List<BudgetItem> archivedBudgetList = jdbcTemplateObject.query(budgetQuery, new Object[] { username }, new  BudgetItemMapper());
		return archivedBudgetList;
	}


}
