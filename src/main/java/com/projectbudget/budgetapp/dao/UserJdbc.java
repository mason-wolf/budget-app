package com.projectbudget.budgetapp.dao;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import com.projectbudget.budgetapp.model.Account;
import com.projectbudget.budgetapp.model.User;

public class UserJdbc implements UserDao {

	private DataSource dataSource;
	private JdbcTemplate jdbcTemplateObject;
	private static ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
	public static UserJdbc query = (UserJdbc)context.getBean("UserJdbc");
	
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
	public void create(String username, String password) {
			String newUserQuery = "insert into users (username, password, enabled) values (?, ?, ?)";
			String newUserRole = "insert into authorities (username, authority) values (?, ?)";
			jdbcTemplateObject.update(newUserQuery, username, password, true);
			jdbcTemplateObject.update(newUserRole, username, "ROLE_USER");
	}

	@Override
	public void delete(String username) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(String username) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public User getUser(String username) {
		
		   String query = "select * from user where email = ?";
		   User user = new User();

		   try {
		   user = jdbcTemplateObject.queryForObject(query, new Object[] { username }, new UserMapper());
		   }
		   catch (Exception e)
		   {
			   user = null;
		   }
		   return user;
	}

	@Override
	public List<User> getUsers() {
		   String query = "select * from users";
		   List <User> users = jdbcTemplateObject.query(query, new UserMapper());
		   return users;
	}

	@Override
	public List<Account> getUserAccounts(String username) {
		String query = "select * from accounts where accountOwner = ?";
		List<Account> accounts;
		
		try {
			accounts = jdbcTemplateObject.query(query, new Object[] { username }, new AccountMapper());
			
		}
		catch (Exception e)
		{
			accounts = null;
		}
		
		return accounts;
	}

}
