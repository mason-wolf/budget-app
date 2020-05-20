package com.projectbudget.budgetapp.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import com.projectbudget.budgetapp.model.User;



public class UserMapper implements RowMapper<User>
{

	@Override
	public User mapRow(ResultSet rs, int rowNum) throws SQLException {	
		User user = new User();
		user.setUserId(rs.getInt("id"));
		user.setUserName(rs.getString("username"));
		user.setPassword(rs.getString("password"));
		return user;
	}
	
}

