package com.projectbudget.budgetapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.projectbudget.budgetapp.dao.UserJdbc;

@SpringBootApplication
public class BudgetAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(BudgetAppApplication.class, args);
	}

}
