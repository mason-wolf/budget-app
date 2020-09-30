package com.projectbudget.budgetapp.controller;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.validation.Valid;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.projectbudget.budgetapp.MailSender;
import com.projectbudget.budgetapp.dao.AccountJdbc;
import com.projectbudget.budgetapp.dao.UserJdbc;
import com.projectbudget.budgetapp.model.Account;
import com.projectbudget.budgetapp.model.BudgetItem;
import com.projectbudget.budgetapp.model.BudgetStatus;
import com.projectbudget.budgetapp.model.Transaction;

@Controller
public class DashboardController {
	
	public static int selectedBudgetMonth; 
	public static int selectedBudgetYear;
	
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
		transaction.setDate(dateRecieved);
		transaction.setAmount(Double.parseDouble(balance));
		transaction.setCategory("Income");
		
		AccountJdbc.query.addTransaction(transaction, false);
		
		// Format and update the account budget date.
		LocalDate currentDate = LocalDate.now();
		account.setBudgetStartDate(currentDate.toString());
		account.isPrimary(true);

		// Create default budget categories for the user.
		AccountJdbc.query.addTransactionCategory(currentUser(), "Auto & Transport");
		AccountJdbc.query.addTransactionCategory(currentUser(), "Food & Dining");
		AccountJdbc.query.addTransactionCategory(currentUser(), "Entertainment");
		AccountJdbc.query.addTransactionCategory(currentUser(), "Internet");
		AccountJdbc.query.addTransactionCategory(currentUser(), "Phone");
		AccountJdbc.query.addTransactionCategory(currentUser(), "Gas");
		AccountJdbc.query.addTransactionCategory(currentUser(), "Insurance");
		AccountJdbc.query.addTransactionCategory(currentUser(), "Utilities");
		AccountJdbc.query.addTransactionCategory(currentUser(), "Rent");
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
			transaction.setDate(date);
			transaction.setAmount(Double.parseDouble(amount));
			transaction.setCategory("Income");
			
			// Determine if the date has lapsed for this transaction,
			// if it's part of an older budget, archive the transaction.
			if (monthLapsed(date))
			{
				AccountJdbc.query.addTransaction(transaction, true);
			}
			else
			{
				AccountJdbc.query.addTransaction(transaction, false);
			}

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
	public String viewTransactions(Model model) throws ParseException
	{
		List<Transaction> accountActivity = AccountJdbc.query.getTransactionHistory(currentUser());
		
		for (Transaction t : accountActivity)
		{
			t.setDate(getBudgetDate(t.getDate()));
		}
		
		if (accountActivity.size() == 0)
		{
			model.addAttribute("noAccountActivity", accountActivity);
		}
		else 
		{
			model.addAttribute("accountActivity", accountActivity);
		}
		
		model.addAttribute("loggedIn", currentUser());
		return "AccountActivity";
	}
	
	// Remove a transaction.
	@RequestMapping(value = "RemoveTransaction/{transactionId}", method = RequestMethod.GET)
	public String removeExpense(@PathVariable(value="transactionId") Integer transactionId, Model model) throws Exception {


		Account account = AccountJdbc.query.getAccount(currentUser());
		Transaction transaction = AccountJdbc.query.getTransaction(transactionId);
		
		// If the transaction is a source of income and the user removes it, update the account balance.		
		
		double currentBalance = account.getBalance();
		
		if (transaction.getCategory().contains("Income"))
		{
			double newBalance = currentBalance - transaction.getAmount();
			AccountJdbc.query.updateBalance(currentUser(), newBalance);
		}
		else
		{
			double newBalance = currentBalance + transaction.getAmount();
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
    		// Get the current month.
    		LocalDate currentDate = LocalDate.now();
    		selectedBudgetMonth = currentDate.getMonthValue();
    		selectedBudgetYear = currentDate.getYear();
    		populateDashboard(model);
    		
            return "Dashboard";	
    	}
    }
   	
    public void populateDashboard(Model model) throws ParseException 
    {

    	// Retrieve the user's account details.
		Account account = AccountJdbc.query.getAccount(currentUser());

		// Retrieve the user's transaction list.
		List<Transaction> transactionList = AccountJdbc.query.getTotalSpentByCategory(currentUser(), selectedBudgetMonth, selectedBudgetYear);
			
		// If a new month has lapsed, archive the previous month's budget.
		if (monthLapsed(account))
		{
			AccountJdbc.query.archiveAccount(account);
		}
		
		// Retrieve the user's budget list.
		List<BudgetItem> budgetList = AccountJdbc.query.getBudgetByCategory(currentUser(), selectedBudgetMonth, selectedBudgetYear);
			
		// Get total spent and other spent that was not on the budget.
		double totalSpent = 0;
		double otherSpent = AccountJdbc.query.getTotalNotBudgeted(currentUser(), selectedBudgetMonth, selectedBudgetYear);

		
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
		
		DecimalFormat dFormat = new DecimalFormat("###,##0.00");
		
		double remainingFunds = account.getBalance() + totalSpent - totalSpent;
		
		// Budget alloted and spent: compares budget items with transactions
		List<BudgetStatus> budgetStatusItems = AccountJdbc.query.getBudgetStatus(currentUser());
		
		// Sort the items by descending order (highest budgets).
		budgetStatusItems.sort((firstAmount , secondAmount) -> Double.compare(secondAmount.getBudgetAmount(), firstAmount.getBudgetAmount()));

		List<Transaction> transactionHistory = AccountJdbc.query.getTransactionHistory(currentUser());
		
 		model.addAttribute("income", "$" + dFormat.format(account.getBalance() + totalSpent));
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
		
		LocalDate currentDate = LocalDate.now();
		String currentMonth = currentDate.getMonth().toString().toLowerCase();
		String budgetTimeFrame = currentMonth.substring(0, 1).toUpperCase() + currentMonth.substring(1);
		
		model.addAttribute("budgetTimeFrame", budgetTimeFrame);
		
		List<BudgetItem> budgetArchive = AccountJdbc.query.getAllBudgetArchives(currentUser());
		List<String> dateList = new ArrayList<String>();
		
		if (budgetArchive.size() != 0)
		{
			// Populate a list with budgets by month and year.
			for (BudgetItem budgetItem : budgetArchive)
			{			
				if (!dateList.contains(getBudgetMonthYear(budgetItem.getStartDate())))
				{
					dateList.add(getBudgetMonthYear(budgetItem.getStartDate()));
				}
			}	
			
			model.addAttribute("budgetArchive", dateList);
		}	
		
		model.addAttribute("loggedIn", account.getAccountOwner());
    }
    
	@RequestMapping(value = "/Dashboard", method = RequestMethod.POST)
	public String showArchivedBudget(@RequestParam("budgetMonthYear") String budgetMonthYear, Model model)
	{	
		// Strip the month from the budgetMonthYear string.
		char[] timeFrameChars = budgetMonthYear.toCharArray();
		String budgetMonth = "";
		
		for (int i = 0; i < timeFrameChars.length; i++)
		{
			if (!Character.isDigit(timeFrameChars[i]) && timeFrameChars[i] != ' ')
			{
				budgetMonth = budgetMonth + timeFrameChars[i]; 
			}
		}
		
		// Strip the year from the budgetMonthString
	    int budgetYear = Integer.parseInt(budgetMonthYear.replaceAll("[^0-9]", ""));		
		DateTimeFormatter parser = DateTimeFormatter.ofPattern("MMMM").withLocale(Locale.ENGLISH);
		TemporalAccessor accessor = parser.parse(budgetMonth);
		List<BudgetStatus> budgetArchive = AccountJdbc.query.getBudgetArchive(currentUser(), accessor.get(ChronoField.MONTH_OF_YEAR), budgetYear);

		double amountEarned = 0;
		
		try 
		{
			amountEarned = AccountJdbc.query.getAmountEarned(currentUser(), accessor.get(ChronoField.MONTH_OF_YEAR), budgetYear);
		}
		catch(Exception e)
		{
			amountEarned = 0;
		}
		
		double totalBudget = 0;
		double totalSpent = 0;

		for(BudgetStatus budgetItem : budgetArchive)
		{
			totalBudget += budgetItem.getBudgetAmount();
			totalSpent += budgetItem.getBudgetSpent();
		}
		
		double remaining = amountEarned - totalSpent;
		DecimalFormat dFormat = new DecimalFormat("#,##0.00");
		String formattedTotalBudget = dFormat.format(totalBudget);
		model.addAttribute("amountEarned", amountEarned);
		model.addAttribute("totalBudget", formattedTotalBudget);
		model.addAttribute("totalSpent", dFormat.format(totalSpent));
		model.addAttribute("remaining", dFormat.format(remaining));
		model.addAttribute("budgetMonth", budgetMonth);
		model.addAttribute("budgetYear", budgetYear);
		model.addAttribute("budgetArchive", budgetArchive);		
		model.addAttribute("loggedIn", currentUser());
		
		return "BudgetHistory";
	}
	
    // Formats the account date into from yyyy-MM-dd format into the budget's month.
	public static String getBudgetMonth(String budgetDate) throws ParseException
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		date = dateFormat.parse(budgetDate);
	    LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		String month = localDate.getMonth().toString();
		String monthLowerCase = month.toLowerCase();
		String monthFormatted = monthLowerCase.substring(0, 1).toUpperCase() + monthLowerCase.substring(1);
		return monthFormatted;
	}
	
	// Formats a date string from yyyy-MM-dd format into the month and year.
	public String getBudgetMonthYear(String dateString) throws ParseException
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		date = dateFormat.parse(dateString);
	    LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		String month = localDate.getMonth().toString();
		String monthLowerCase = month.toLowerCase();
		String monthFormatted = monthLowerCase.substring(0, 1).toUpperCase() + monthLowerCase.substring(1);
		String dateFormatted = monthFormatted + " " + localDate.getYear();
		return dateFormatted;
	}
	
	// Formats a date string from yyyy-MM-dd format into mm/dd/yyy format.
	public static String getBudgetDate(String dateString) throws ParseException
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		date = dateFormat.parse(dateString);
	    LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		int month = localDate.getMonthValue();
		int day = localDate.getDayOfMonth();
		String dateFormatted = month + "/" + day + "/" + localDate.getYear();
		return dateFormatted;
	}
	
	// Returns true if the month of a date has lapsed.
	public boolean monthLapsed(Account account) throws ParseException
	{
		boolean budgetLapsed = false;
		
		LocalDate currentDate = LocalDate.now();
		String currentMonth = currentDate.getMonth().toString().toLowerCase();
		String accountDate = getBudgetMonth(account.getBudgetStartDate()).toString().toLowerCase();

		if (!currentMonth.equals(accountDate))
		{
			budgetLapsed = true;
		}		
		
		return budgetLapsed;
	}
	
	// Returns true if the month of a date has lapsed.
	public static boolean monthLapsed(String date) throws ParseException
	{
		boolean lapsedMonth = false;
		
		LocalDate currentDate = LocalDate.now();
		String currentMonth = currentDate.getMonth().toString().toLowerCase();
		
		if (!currentMonth.equals(getBudgetMonth(date).toLowerCase()))
		{
			lapsedMonth = true;
		}
		
		return lapsedMonth;
	}
	
    public String currentUser()
    {
    	Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	String username = ((UserDetails)principal).getUsername();
    	return username;
    }
    
	@GetMapping({"/Contact"})
	public String contactPage(Model model)
	{
		model.addAttribute("formNotSent", "formNotSent");
		return "Contact";
	}
	
	@RequestMapping(value = "/ContactForm", method = RequestMethod.POST)
	public String submitContactForm(Model model, @RequestParam("contactForm") String contactForm, @RequestParam("email") String email) throws Exception
	{
		if (!IndexController.validEmail(email))
		{
			model.addAttribute("invalidEmail", "Please enter a valid email.");
			model.addAttribute("formNotSent", "formNotSent");
			return "Contact";
		}
		else
		{
			MailSender mailSender = new MailSender();
			mailSender.SendContactForm(email, contactForm);
			model.addAttribute("formSent", "formSent");
			return "Contact";
		}
	}
}
