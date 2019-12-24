package com.projectbudget.budgetapp.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import com.projectbudget.budgetapp.model.Budget;

public class BudgetMapper implements RowMapper<Budget>{

	@Override
	public Budget mapRow(ResultSet rs, int rowNum) throws SQLException {

		Budget budget = new Budget();
		budget.setBudgetId(rs.getInt("id"));
		budget.setOwner(rs.getString("owner"));
		budget.setCategory(rs.getString("category"));
		budget.setArchived(rs.getBoolean("archived"));
		budget.setStartDate(rs.getString("startDate"));
		budget.setEndDate(rs.getString("endDate"));
		budget.setAmount(Double.parseDouble(rs.getString("amount")));
		
		return budget;
	}

}

