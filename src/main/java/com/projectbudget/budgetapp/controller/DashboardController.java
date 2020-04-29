package com.projectbudget.budgetapp.controller;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectbudget.budgetapp.dao.AccountJdbc;
import com.projectbudget.budgetapp.dao.UserJdbc;
import com.projectbudget.budgetapp.model.Account;
import com.projectbudget.budgetapp.model.Budget;
import com.projectbudget.budgetapp.model.BudgetStatus;
import com.projectbudget.budgetapp.model.Transaction;


@Controller
public class DashboardController {
	
	@GetMapping({"/NewProfile"})
	public String newProfile()
	{
		return "NewProfile";
	}
	
	// Add account information is no account exists for user.
	@RequestMapping(value = "/NewProfile", method = RequestMethod.POST)
	public String addAccount(Model model, @RequestParam("accountName") String accountName, @RequestParam("accountType") String accountType,
			@RequestParam("income") String balance, @RequestParam("dateRecieved") String dateRecieved) throws Exception {

		Account account = new Account();
		account.setAccountName(accountName);
		account.setAccountType(accountType);
		account.setBalance(Double.parseDouble(balance));
		account.setBudgetStartDate(dateRecieved);
		account.isPrimary(true);
	
		AccountJdbc.query.addTransactionCategory(currentUser(), "Auto & Transport");
		AccountJdbc.query.addTransactionCategory(currentUser(), "Food & Dining");
		AccountJdbc.query.addTransactionCategory(currentUser(), "Entertainment");
		AccountJdbc.query.addTransactionCategory(currentUser(), "Personal Care");
		AccountJdbc.query.addAccount(currentUser(), account);
		populateDashboard(model);
		return "Dashboard";
	}
	

	@RequestMapping(value = "/AddIncome", method = RequestMethod.POST)
	public String addIncome(Model model, @RequestParam("amount") String amount, @RequestParam("date") String date) throws Exception {
		Account account = AccountJdbc.query.getAccount(currentUser());
		double currentBalance = account.getBalance();
		double newBalance = currentBalance + Double.parseDouble(amount);
		AccountJdbc.query.updateBalance(currentUser(), newBalance);
		populateDashboard(model);
		return "Dashboard";
	}
	
	@RequestMapping(value = "RemoveTransaction/{transactionId}", method = RequestMethod.GET)
	public String removeExpense(@PathVariable(value="transactionId") Integer transactionId, Model model) throws Exception {

		AccountJdbc.query.deleteTransasction(transactionId);
		populateDashboard(model);
		return "redirect:/Dashboard";
	}
	
    @GetMapping({"/Dashboard"})
    public String dashoard(Model model) throws ParseException  {
    	
    	List<Account> accounts = UserJdbc.query.getUserAccounts(currentUser());
    	
    	if (accounts.size() == 0)
    	{
    		return "NewProfile";
    	}
    	else 
    	{
    		populateDashboard(model);
            return "Dashboard";	
    	}
    }
    
    public void populateDashboard(Model model) throws ParseException 
    {
		double totalBudget = 0;
		
		Account account = AccountJdbc.query.getAccount(currentUser());
		
		// Items in the budget
		List<Budget> budgetList = AccountJdbc.query.getBudgetByCategory(currentUser());
		String budgetTimeFrame = budgetTimeframe(account);
		
		if (budgetList.size() == 0)
		{
			totalBudget = 0;
			model.addAttribute("noBudget" , "No budget");
		}
		else
		{
			for (Budget budgetItem : budgetList)
			{
				totalBudget += budgetItem.getAmount();
			}
		}
		
		double projectedSavings = account.getBalance() - totalBudget;
		
		// Budget alloted and spent: compares budget items with transactions
		List<BudgetStatus> budgetStatusItems = AccountJdbc.query.getbudgetStatus(currentUser());
		List<Transaction> transactionHistory = AccountJdbc.query.getTransactionHistory(currentUser());
		
 		model.addAttribute("balance", "$" + account.getBalance());
		model.addAttribute("projectedSavings", "$" + projectedSavings);
		model.addAttribute("projectedSpending", "$" + totalBudget);
		

		if (transactionHistory.size() != 0)
		{
			model.addAttribute("transactionHistory", transactionHistory);
		}

		if (budgetList.size() == 0)
		{
			model.addAttribute("noAccountActivity", "No account activity.");
		}
		else 
		{
			model.addAttribute("budgetTimeFrame", budgetTimeFrame);
		    model.addAttribute("budgetStatus", budgetStatusItems);
		}
		
    }
    
	public String budgetTimeframe(Account account) throws ParseException
	{
		 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		 String budgetDate = account.getBudgetStartDate();
		 Date date = new Date();
		 date = dateFormat.parse(budgetDate);
		 LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		String currentMonth = localDate.getMonth().toString();
		String currentMonthLowerCase = currentMonth.toLowerCase();
		String currentMonthFormatted = currentMonthLowerCase.substring(0, 1).toUpperCase() + currentMonthLowerCase.substring(1);
		int currentYear = localDate.getYear();
		return currentMonthFormatted + " " + currentYear + " Budget";
	}
	
    public String currentUser()
    {
    	Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	String username = ((UserDetails)principal).getUsername();
    	return username;
    }
}
