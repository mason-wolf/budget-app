package com.projectbudget.budgetapp.dao;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
		
		   String query = "select * from users where username = ?";
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

	@Override
	public void addUserToken(String username, String userToken) {
		String query = "update users set user_token=? where username=?";
		jdbcTemplateObject.update(query, userToken, username);
	}

	@Override
	public String getUserByToken(String userToken) {

		String query = "select username from users where user_token=?";
		
		String user = "";
		
		try {
			user = jdbcTemplateObject.queryForObject(query, new Object[] { userToken }, String.class);
		}
		catch (Exception e)
		{
			user = null;
		}
		
		return user;
	}
	
	@Override
	public void changePassword(String username, String password) {
		
	    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	    String encodedPassword = encoder.encode(password);

		String passwordChangeQuery = "update users set password=? where username=?";
		String tokenRemovalQuery = "update users set user_token = null where username=?";
		jdbcTemplateObject.update(passwordChangeQuery, encodedPassword, username);
		jdbcTemplateObject.update(tokenRemovalQuery, username);
	}

}
