package com.projectbudget.budgetapp.controller;


import java.text.DecimalFormat;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.projectbudget.budgetapp.dao.AccountJdbc;
import com.projectbudget.budgetapp.dao.UserJdbc;
import com.projectbudget.budgetapp.model.Account;
import com.projectbudget.budgetapp.model.BudgetItem;
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
			@RequestParam("income") String balance, @RequestParam("dateRecieved") String dateRecieved, @ModelAttribute("userName") String userName) throws Exception {

		Account account = new Account();
		account.setAccountName(accountName);
		account.setAccountType(accountType);
		account.setBalance(Double.parseDouble(balance));
		
		// Create a new transaction to track income history.
		Transaction transaction = new Transaction();
		transaction.setOwner(currentUser());
		transaction.setDate(dateFormatter(dateRecieved));
		transaction.setAmount(Double.parseDouble(balance));
		transaction.setCategory("Income");
		
		AccountJdbc.query.addTransaction(transaction);
		
		// Format and update the account budget date.
		LocalDate currentDate = LocalDate.now();
		int currentMonth = currentDate.getMonthValue();
		int currentDay = currentDate.getDayOfMonth();
		int currentYear = currentDate.getYear();
		String budgetDate = currentMonth + "/" + currentDay + "/" + currentYear;
		
		account.setBudgetStartDate(budgetDate);
		account.isPrimary(true);

		// Create default budget categories for the user.
		AccountJdbc.query.addTransactionCategory(currentUser(), "Auto & Transport");
		AccountJdbc.query.addTransactionCategory(currentUser(), "Food & Dining");
		AccountJdbc.query.addTransactionCategory(currentUser(), "Entertainment");
		AccountJdbc.query.addTransactionCategory(currentUser(), "Other");
		AccountJdbc.query.addTransactionCategory(currentUser(), "Personal Care");
		AccountJdbc.query.addAccount(currentUser(), account);
		
		populateDashboard(model);
		return "Dashboard";
	}
	

	@RequestMapping(value = "/AddIncome", method = RequestMethod.POST)
	public String addIncome(Model model, @RequestParam("amount") String amount, @RequestParam("date") String date) throws Exception {
		
		try
		{
			// Update the account balance.
			Account account = AccountJdbc.query.getAccount(currentUser());
			double currentBalance = account.getBalance();
			double newBalance = currentBalance + Double.parseDouble(amount);
			AccountJdbc.query.updateBalance(currentUser(), newBalance);	
			
			// Create a new transaction to track income history.
			Transaction transaction = new Transaction();
			transaction.setOwner(currentUser());
			transaction.setDate(dateFormatter(date));
			transaction.setAmount(Double.parseDouble(amount));
			transaction.setCategory("Income");
			AccountJdbc.query.addTransaction(transaction);
		}
		catch (Exception validationException)
		{
			model.addAttribute("invalidIncomeAmount", validationException);
		}
		populateDashboard(model);
		return "Dashboard";
	}
	
	// Show a list of recent transactions.
	@RequestMapping(value = "/AccountActivity", method = RequestMethod.GET)
	public String viewTransactions(Model model)
	{
		List<Transaction> accountActivity = AccountJdbc.query.getTransactionHistory(currentUser());
		
		if (accountActivity.size() == 0)
		{
			model.addAttribute("noAccountActivity", accountActivity);
		}
		else 
		{
			model.addAttribute("accountActivity", accountActivity);
		}
		return "AccountActivity";
	}
	
	// Remove a transaction.
	@RequestMapping(value = "RemoveTransaction/{transactionId}", method = RequestMethod.GET)
	public String removeExpense(@PathVariable(value="transactionId") Integer transactionId, Model model) throws Exception {


		Account account = AccountJdbc.query.getAccount(currentUser());
		Transaction transaction = AccountJdbc.query.getTransaction(transactionId);
		
		// If the transaction is a source of income and the user removes it, update the account balance.		
		if (transaction.getCategory().contains("Income"))
		{
			double currentBalance = account.getBalance();
			double newBalance = currentBalance - transaction.getAmount();
			AccountJdbc.query.updateBalance(currentUser(), newBalance);
		}
		
		AccountJdbc.query.deleteTransasction(transactionId);
		populateDashboard(model);
		return "redirect:/AccountActivity";
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

    	// Retrieve the user's account details.
		Account account = AccountJdbc.query.getAccount(currentUser());
		
		// Get the current month.
		LocalDate currentDate = LocalDate.now();
		String currentMonth = currentDate.getMonth().toString().toLowerCase();
		String budgetTimeFrame = currentMonth.substring(0, 1).toUpperCase() + currentMonth.substring(1);
		
		// Retrieve the user's transaction list.
		List<Transaction> transactionList = AccountJdbc.query.getTotalSpentByCategory(currentUser());
		
		
		// If a new month has lapsed, archive the previous month's budget.
		if (budgetLapsed(account))
		{
			AccountJdbc.query.archiveAccount(account);
			
			double totalSpent = 0;
			
			// Calculate total spent last month.
			for (Transaction t : transactionList)
			{
				totalSpent += t.getAmount();
			}
			
			// Update the account balance with last month's remaining funds.
			AccountJdbc.query.updateBalance(currentUser(), account.getBalance() - totalSpent);
			account.setBalance(account.getBalance() - totalSpent);
		}
		
		// Retrieve the user's budget list.
		List<BudgetItem> budgetList = AccountJdbc.query.getBudgetByCategory(currentUser());
			
		// Get total spent and other spent that was not on the budget.

		double totalSpent = 0;
		double otherSpent = AccountJdbc.query.getTotalNotBudgeted(currentUser());

		
		if (budgetList.size() == 0)
		{
			model.addAttribute("noBudget" , "No budget");
		}
		else
		{
			// Calculate total spent this month.
			for (Transaction t : transactionList)
			{
				totalSpent += t.getAmount();
			}
			
		}
		
		DecimalFormat dFormat = new DecimalFormat("0.00");

		double remainingFunds = account.getBalance() - totalSpent;
		
		// Budget alloted and spent: compares budget items with transactions
		List<BudgetStatus> budgetStatusItems = AccountJdbc.query.getBudgetStatus(currentUser());
		List<Transaction> transactionHistory = AccountJdbc.query.getTransactionHistory(currentUser());
		
 		model.addAttribute("income", "$" + dFormat.format(account.getBalance()));
 		model.addAttribute("totalSpent", "$" + dFormat.format(totalSpent));
 		model.addAttribute("remainingFunds", "$" + dFormat.format(remainingFunds));

 		// If transactions exist that aren't on the budget, show them.
 		if (otherSpent != 0)
 		{
	 		model.addAttribute("otherSpent", "$" + dFormat.format(otherSpent));
 		}
 		
		if (transactionHistory.size() != 0)
		{
			model.addAttribute("transactionHistory", transactionHistory);
		}
		
		// Show budget activity if a budget exists.
		if (budgetList.size() == 0)
		{
			model.addAttribute("noAccountActivity", "No account activity.");
		}
		else 
		{
		    model.addAttribute("budgetStatus", budgetStatusItems);
		}
		
		model.addAttribute("budgetTimeFrame", budgetTimeFrame);
		

		List<BudgetItem> budgetArchive = AccountJdbc.query.getBudgetArchive(currentUser());
		List<String> dateList = new ArrayList<String>();
		
		for (BudgetItem b : budgetArchive)
		{
			if (b.getArchived())
			{
				if (!dateList.contains(b.getStartDate()))
				{
					dateList.add(b.getStartDate());
				}
			}
		}
		
		for (String s : dateList)
		{
			System.out.println(budgetTimeframe(s));
		}
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
		// int currentYear = localDate.getYear();
		return currentMonthFormatted;
	}
	
	public String dateFormatter(String date) throws ParseException
	{
		 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		 Date dateFormatted = new Date();
		 dateFormatted = dateFormat.parse(date);
		 LocalDate localDate = dateFormatted.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		 
		int month = localDate.getMonth().getValue();
		int day = localDate.getDayOfMonth();
		int year = localDate.getYear();
	    date = month + "/" + day + "/" + year;
		return date;
	}
	
	// Returns true if a new month has lapsed.
	public boolean budgetLapsed(Account account) throws ParseException
	{
		boolean budgetLapsed = true;
		
		LocalDate currentDate = LocalDate.now();
		String currentMonth = currentDate.getMonth().toString().toLowerCase();
		String accountDate = budgetTimeframe(account.getBudgetStartDate()).toString().toLowerCase();

		if (currentMonth.equals(accountDate))
		{
			budgetLapsed = false;
		}
		
		return budgetLapsed;
	}
	
    public String currentUser()
    {
    	Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	String username = ((UserDetails)principal).getUsername();
    	return username;
    }
}
