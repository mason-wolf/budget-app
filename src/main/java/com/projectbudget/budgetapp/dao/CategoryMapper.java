package com.projectbudget.budgetapp.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.projectbudget.budgetapp.model.Category;

public class CategoryMapper implements RowMapper<Category>{

	@Override
	public Category mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Category category = new Category();
		category.setCategoryId(rs.getInt("id"));
		category.setOwner(rs.getString("owner"));
		category.setTitle(rs.getString("title"));
		return category;
	}

}
