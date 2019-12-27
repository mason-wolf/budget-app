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
	public void addIncome(String username, double amount) {
		
		String accountIdQuery = "select id from accounts where accountOwner = ? and isPrimary = 1";
		int accountId =  jdbcTemplateObject.queryForObject(accountIdQuery, new Object[] { username}, Integer.class);
		String addIncomeQuery = "update accounts set balance = balance + ? where id = ?";
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
		
		String query = "select * from transactions where owner = ? and archived='false' group by category";
		
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
		String query = "insert into transactions (owner, income, date, expense, category, account, archived) values (?, ?, ?, ?, ?, ?, '0')";
		jdbcTemplateObject.update(query, transaction.getOwner(), transaction.getIncome(), transaction.getDate(), transaction.getExpense(), transaction.getCategory(), transaction.getAccount());
	}

	@Override
	public String getTransactionCategory(int categoryId) {
		String query = "select title from budgetCategories where id = ?";
		String category =  jdbcTemplateObject.queryForObject(query, new Object[] { categoryId }, String.class);
		return category;	
	}

	@Override
	public List<Transaction> getTotalSpentByCategory(String username) {

		String query = "select id, owner, income, archived, date, category, account, sum(expense) as expense from transactions where owner= ? and archived='0' group by category";
		List<Transaction> transactions = jdbcTemplateObject.query(query, new Object[] { username }, new TransactionMapper());
		return transactions;
	}

	@Override
	public void addBudgetItem(String username, Budget budgetItem) {
		String categoryQuery = "select title from budgetCategories where id=?";
		String category = jdbcTemplateObject.queryForObject(categoryQuery, new Object[] { budgetItem.getBudgetId() }, String.class);
		
		String budgetItemQuery = "select id from budgets where category = ?";
		
		int budgetId;
		
		try 
		{
			budgetId = jdbcTemplateObject.queryForObject(budgetItemQuery, new Object[] { category }, Integer.class);
			String updateBudgetQuery = "update budgets set amount = ? where id = ?";
			jdbcTemplateObject.update(updateBudgetQuery, budgetItem.getAmount(), budgetId);
		}
		catch(Exception e)
		{
			
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
		String query = "select budgets.category, budgets.amount as budgetAmount, sum(transactions.expense) as budgetSpent from budgets \r\n" + 
				"left join transactions on transactions.category = budgets.category\r\n" + 
				"where budgets.owner= ? group by budgets.category;\r\n";
		List<BudgetStatus> budgetStatus = jdbcTemplateObject.query(query, new Object[] { username }, new BudgetStatusMapper());
		return budgetStatus;
	}


}
