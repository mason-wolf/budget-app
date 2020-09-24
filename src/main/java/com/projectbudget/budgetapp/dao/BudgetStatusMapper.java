package com.projectbudget.budgetapp.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.mysql.cj.jdbc.result.ResultSetMetaData;
import com.projectbudget.budgetapp.model.BudgetStatus;

public class BudgetStatusMapper implements RowMapper<BudgetStatus> {

	@Override
	public BudgetStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		BudgetStatus budgetStatus = new BudgetStatus();
		budgetStatus.setCategory(rs.getString("category"));
		budgetStatus.setBudgetAmount(rs.getDouble("budgetAmount"));
		budgetStatus.setBudgetSpent(rs.getDouble("budgetSpent"));
		return budgetStatus;
	}

}
