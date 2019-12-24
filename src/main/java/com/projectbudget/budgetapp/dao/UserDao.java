package com.projectbudget.budgetapp.dao;

import java.util.List;

import com.projectbudget.budgetapp.model.Account;
import com.projectbudget.budgetapp.model.User;

public interface UserDao {
	
	public void create(String username, String password);
	public void delete(String username);
	public void update(String username);
	public User getUser(String username);
	public List<User> getUsers();
	public List<Account> getUserAccounts(String username);
}