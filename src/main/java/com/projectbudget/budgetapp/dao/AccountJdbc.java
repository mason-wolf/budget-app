package com.projectbudget.budgetapp.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import com.projectbudget.budgetapp.model.Account;
import com.projectbudget.budgetapp.model.Budget;
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
	public List<Budget> getBudget(String username) {
		
		String query = "select * from budgets where owner = ? and archived = 0";
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
	public List<Transaction> getTransactions(String username) {
		
		String query = "select * from transactions where owner = ?";
		
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
	public List<Category> getTransactionCategories(String username) {
		
		String query = "select * from budgetCategories where owner = ?";
		
		List<Category> categories = jdbcTemplateObject.query(query, new Object[] { username}, new CategoryMapper());
		categories.add(new Category("Auto & Transport"));
		categories.add(new Category("Entertainment"));
		categories.add(new Category("Food & Dining"));
		categories.add(new Category("Insurance"));
		categories.add(new Category("Utilities"));
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


}
