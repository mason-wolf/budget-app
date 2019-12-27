package com.projectbudget.budgetapp.controller;


import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
		
		AccountJdbc.query.addIncome(currentUser(), Double.parseDouble(amount));
		populateDashboard(model);
		return "Dashboard";
	}
	
    @GetMapping({"/Dashboard"})
    public String dashoard(Model model) {
    	
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
    
    public void populateDashboard(Model model)
    {
		double totalBudget = 0;
		
		Account account = AccountJdbc.query.getAccount(currentUser());
		
		// Items in the budget
		List<Budget> budgetList = AccountJdbc.query.getBudgetByCategory(currentUser());
		
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
		
		List<Transaction> transactionCategories = AccountJdbc.query.getTransactionsByCategory(currentUser());
		List<Transaction> categorySpending = AccountJdbc.query.getTotalSpentByCategory(currentUser());
		List<BudgetStatus> budgetStatusItems = AccountJdbc.query.getbudgetStatus(currentUser());
		
 		model.addAttribute("balance", "$" + account.getBalance());
		model.addAttribute("projectedSavings", "$" + projectedSavings);
		model.addAttribute("projectedSpending", "$" + totalBudget);
	
		if (transactionCategories.size() == 0)
		{
			model.addAttribute("noAccountActivity", "No account activity.");
		}
		else 
		{
			model.addAttribute("transactionCategories", transactionCategories);
			model.addAttribute("categorySpending", categorySpending);
			model.addAttribute("budgetStatus", budgetStatusItems);
		}
		
    }
    
    public String currentUser()
    {
    	Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	String username = ((UserDetails)principal).getUsername();
    	return username;
    }
}
