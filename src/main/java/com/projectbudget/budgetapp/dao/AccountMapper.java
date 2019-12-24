package com.projectbudget.budgetapp.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import com.projectbudget.budgetapp.model.Account;



public class AccountMapper implements RowMapper<Account>
{

	@Override
	public Account mapRow(ResultSet rs, int rowNum) throws SQLException {	
		Account account = new Account();
		account.setId(rs.getInt("id"));
		account.setAccountOwner(rs.getString("accountOwner"));
		account.setAccountName(rs.getString("accountName"));
		account.setAccountType(rs.getString("accountType"));
		account.isPrimary(rs.getBoolean("isPrimary"));
		account.setBalance(Double.parseDouble(rs.getString("balance")));
		account.setBudgetStartDate(rs.getString("budgetStartDate"));
		account.setBudgetEndDate(rs.getString("budgetEndDate"));
		
		return account;
	}
}

