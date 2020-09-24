package com.projectbudget.budgetapp.dao;

import java.util.List;

import com.projectbudget.budgetapp.model.Account;
import com.projectbudget.budgetapp.model.User;

public interface UserDao {
	
	public void create(String username, String password);
	public void delete(String username);
	public void update(String username);
	public void addUserToken(String username, String userToken);
	public String getUserByToken(String userToken);
	public void changePassword(String username, String password);
	public User getUser(String username);
	public List<User> getUsers();
	public List<Account> getUserAccounts(String username);
}