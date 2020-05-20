package com.projectbudget.budgetapp.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import com.projectbudget.budgetapp.model.Account;
import com.projectbudget.budgetapp.model.Budget;
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
	public List<Budget> getBudgetByCategory(String username) {
		
		String query = "select * from budgets where owner = ? and archived = 0 group by category";
		List<Budget> budget;
		
		try {
		budget = jdbcTemplateObject.query(query,  new Object[] { username }, new BudgetMapper());
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

	@Override
	public void addTransaction(Transaction transaction) {
		String query = "insert into transactions (owner, date, amount, category, account, archived) values (?, ?, ?, ?, ?, '0')";
		jdbcTemplateObject.update(query, transaction.getOwner(), transaction.getDate(), transaction.getAmount(), transaction.getCategory(), transaction.getAccount());
	}

	@Override
	public String getTransactionCategory(int categoryId) {
		String query = "select title from budgetCategories where id = ?";
		String category =  jdbcTemplateObject.queryForObject(query, new Object[] { categoryId }, String.class);
		return category;	
	}

	@Override
	public List<Transaction> getTotalSpentByCategory(String username) {

		String query = "select id, owner, archived, date, category, account, sum(amount) as amount from transactions where owner= ? and archived='0' group by category order by category";
		List<Transaction> transactions = jdbcTemplateObject.query(query, new Object[] { username }, new TransactionMapper());
		return transactions;
	}

	@Override
	public void addBudgetItem(String username, Budget budgetItem) {
		String categoryQuery = "select title from budgetCategories where id=?";
		String category = jdbcTemplateObject.queryForObject(categoryQuery, new Object[] { budgetItem.getBudgetId() }, String.class);
		String budgetItemQuery = "select id from budgets where category = ? and owner = ?";
		
		int budgetId;
		
		try 
		{
			// If the projected expense already exists, update the budget amount.
			budgetId = jdbcTemplateObject.queryForObject(budgetItemQuery, new Object[] { category, username }, Integer.class);
			System.out.println(budgetId);
			String updateBudgetQuery = "update budgets set amount = ? where id = ? and owner=?";
			jdbcTemplateObject.update(updateBudgetQuery, budgetItem.getAmount(), budgetId, username);
		}
		catch(Exception e)
		{
			System.out.println(e);
			String query = "insert into budgets (owner, category, archived, startDate, endDate, amount) values (?, ?, ?, ?, ?, ?)";
			jdbcTemplateObject.update(query, username, category, budgetItem.getArchived(), budgetItem.getStartDate(), budgetItem.getEndDate(), budgetItem.getAmount());
		}
	}

	@Override
	public List<Budget> getTotalBudgeted(String username) {
		
		String query = "select id, owner, category, archived, startDate, endDate, sum(amount) as amount from budgets where owner = ? and archived = '0' group by category";
		List<Budget> budgetItemTotals = jdbcTemplateObject.query(query, new Object[] { username }, new  BudgetMapper());
		return budgetItemTotals;
		
	}

	@Override
	public void deleteBudgetItem(int budgetId) {
		String categoryQuery = "select category from budgets where id = ?";
		String category = jdbcTemplateObject.queryForObject(categoryQuery, new Object[] { budgetId }, String.class);
		String matchingCategories = "select * from budgets where category = ?";
		List<Budget> budgetItems = jdbcTemplateObject.query(matchingCategories, new Object[] { category }, new BudgetMapper());
		
		for (Budget budgetItem : budgetItems)
		{
			jdbcTemplateObject.update("delete from budgets where id= ? and archived = '0'", budgetItem.getBudgetId());
		}
	}

	@Override
	public List<BudgetStatus> getbudgetStatus(String username) {

		String query = "select budgets.category , sum(transactions.amount) as budgetSpent, budgets.amount as budgetAmount from budgets \n" + 
				"left join\n" + 
				"transactions on budgets.category = transactions.category where budgets.owner = ? and budgets.archived = 0 \n" + 
				"group by budgets.category";
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


}
