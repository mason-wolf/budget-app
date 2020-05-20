package com.projectbudget.budgetapp.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import com.projectbudget.budgetapp.model.Transaction;

public class TransactionMapper  implements RowMapper<Transaction>{
	
	@Override
	public Transaction mapRow(ResultSet rs, int rowNum) throws SQLException {

		Transaction transaction = new Transaction();
		transaction.setTransactionId(rs.getInt("id"));
		transaction.setOwner(rs.getString("owner"));
		transaction.setDate(rs.getString("date"));
		transaction.setAmount(Double.parseDouble(rs.getString("amount")));
		transaction.setCategory(rs.getString("category"));
		transaction.setAccount(rs.getString("account"));
		
		return transaction;
	}

}
