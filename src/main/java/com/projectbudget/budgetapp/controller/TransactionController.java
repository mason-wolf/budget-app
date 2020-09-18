package com.projectbudget.budgetapp.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.validation.Valid;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


import com.projectbudget.budgetapp.dao.AccountJdbc;
import com.projectbudget.budgetapp.model.Account;
import com.projectbudget.budgetapp.model.Category;
import com.projectbudget.budgetapp.model.Transaction;


@Controller
public class TransactionController {
	

	@GetMapping({"/AddExpense"})
	public String addExpense(Model model)
	{
		showAccountDetails(model);
		return "AddExpense";
	}
	
	@RequestMapping(value = "/AddExpense", method = RequestMethod.POST)
	public String addExpense(@RequestParam("amount") String amount, Model model, @Valid String category, @RequestParam("date") String date) throws ParseException
	{
		if (date == "")
		{
			LocalDate currentDate = LocalDate.now();
			date = currentDate.toString();
		}
		
		double expense = 0;
		
		try 
		{
			expense = Double.parseDouble(amount);
		}
		catch (Exception e)
		{
			model.addAttribute("invalidAmount", "Please enter a valid amount.");
			showAccountDetails(model);
		}
		
		
		String categoryTitle = AccountJdbc.query.getTransactionCategory(Integer.parseInt(category));
		
		Transaction transaction = new Transaction();

		Account account = AccountJdbc.query.getAccount(currentUser());
		
		transaction.setOwner(currentUser());
		transaction.setAccount(account.getAccountName());
		transaction.setCategory(categoryTitle);
		
		transaction.setDate(date);
		
		transaction.setAmount(expense);		
		double currentBalance = account.getBalance();
		double newBalance = currentBalance - expense;
		
		AccountJdbc.query.updateBalance(currentUser(), newBalance);

		transaction.setIncome(newBalance);
		
		AccountJdbc.query.addTransaction(transaction);
		
		return "redirect:/Dashboard";
	}
	
	@RequestMapping(value = "AddExpense/AddCategory", method = RequestMethod.POST)
	public String addBudgetCategory(Model model, @RequestParam("category") String categoryTitle) throws Exception {

		AccountJdbc.query.addTransactionCategory(currentUser(), categoryTitle);
		showAccountDetails(model);
		return "AddExpense";
	}
	
	@RequestMapping(value = "AddExpense/DeleteCategory", method = RequestMethod.POST)
	public String deleteBudgetCategory(@Valid String category, Model model) throws Exception {

		AccountJdbc.query.deleteTransactionCategory(Integer.parseInt(category));
		showAccountDetails(model);
		return "AddExpense";
	}
	
    // Formats the account date into mm/dd/yyy format. Retrieves the current month.
	public String budgetTimeframe(String budgetDate) throws ParseException
	{
		 SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
		 Date date = new Date();
		 date = dateFormat.parse(budgetDate);
		 LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		String currentMonth = localDate.getMonth().toString();
		String currentMonthLowerCase = currentMonth.toLowerCase();
		String currentMonthFormatted = currentMonthLowerCase.substring(0, 1).toUpperCase() + currentMonthLowerCase.substring(1);
		return currentMonthFormatted;
	}
	
	public void showAccountDetails(Model model)
	{
		Account account = AccountJdbc.query.getAccount(currentUser());
		List<Category> expenseCategories = AccountJdbc.query.getBudgetCategories(currentUser());
		model.addAttribute("balance", "$" + account.getBalance());	
		model.addAttribute("categories", expenseCategories);
	}
	
	   public String currentUser()
	    {
	    	Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	    	String username = ((UserDetails)principal).getUsername();
	    	return username;
	    }
}
