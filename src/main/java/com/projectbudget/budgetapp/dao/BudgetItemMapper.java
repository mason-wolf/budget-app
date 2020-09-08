package com.projectbudget.budgetapp.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import com.projectbudget.budgetapp.model.BudgetItem;

public class BudgetItemMapper implements RowMapper<BudgetItem>{

	@Override
	public BudgetItem mapRow(ResultSet rs, int rowNum) throws SQLException {

		BudgetItem budget = new BudgetItem();
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

