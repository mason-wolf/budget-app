package com.projectbudget.budgetapp.controller;


import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import javax.validation.Valid;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.projectbudget.budgetapp.dao.AccountJdbc;
import com.projectbudget.budgetapp.model.Account;
import com.projectbudget.budgetapp.model.BudgetItem;
import com.projectbudget.budgetapp.model.Category;

@Controller
public class BudgetManagerController {

	@GetMapping({"/ManageBudget"})
	public String manageBudget(Model model) throws ParseException
	{
		populateBudget(model);
		return "ManageBudget";
	}
	
	public void populateBudget(Model model)
	{
		// Checks if the user has any active budget items.
		List<BudgetItem> budgetItems = AccountJdbc.query.getBudgetByCategory(currentUser(), DashboardController.selectedBudgetMonth,
				DashboardController.selectedBudgetYear);
		
		// Retrieves all user categories for category management.
		List<Category> budgetCategories = AccountJdbc.query.getBudgetCategories(currentUser());
		
		// Get total budget for each category.
		List<BudgetItem> budgetTotals = AccountJdbc.query.getTotalBudgeted(currentUser());
		
		double totalBudget = 0;
		double projectedSavings = 0;
		Account account = AccountJdbc.query.getAccount(currentUser());
		
		String budgetTimeframe = budgetTimeframe();

		DecimalFormat dFormat = new DecimalFormat("#,##0.00");
		
		if (budgetItems.size() == 0)
		{
			model.addAttribute("emptyBudget", "No projected expenses.");
		}
		else
		{
			for (BudgetItem budget : budgetTotals)
			{
				totalBudget += budget.getAmount();
			}
			model.addAttribute("budgetItems", budgetItems);
		}
		
		projectedSavings = account.getBalance() - totalBudget;

		model.addAttribute("budgetTimeframe", budgetTimeframe);
		model.addAttribute("accountBalance", "$" + dFormat.format(account.getBalance()));
		model.addAttribute("categories", budgetCategories);
		model.addAttribute("budgetTotals", budgetTotals);
		model.addAttribute("projectedSavings", "$" + dFormat.format(projectedSavings));
		model.addAttribute("totalBudget", "$" + dFormat.format(totalBudget));
		model.addAttribute("loggedIn", account.getAccountOwner());
	}
	
	@RequestMapping(value = "/ManageBudget", method = RequestMethod.POST)
	public String addProjectedExpense(@RequestParam("amount") String amount, Model model, @Valid String category)
	{
			try 
			{
				double budgetAmount = Double.parseDouble(amount);
					
				BudgetItem newBudgetItem = new BudgetItem();
				newBudgetItem.setOwner(currentUser());
				newBudgetItem.setAmount(budgetAmount);
				newBudgetItem.setStartDate(LocalDate.now().toString());
				newBudgetItem.setArchived(false);
	
				List<Category> categoryList = AccountJdbc.query.getBudgetCategories(currentUser());
				
				for (Category c : categoryList)
				{
					if (c.getTitle().contains(category))
					{
						newBudgetItem.setBudgetId(c.getCategoryId());
					}
				}
				
				AccountJdbc.query.addBudgetItem(currentUser(), newBudgetItem);
				}
			
			catch (Exception e)
			{
				model.addAttribute("invalidAmount", "Please enter a valid amount.");
			}
			
		populateBudget(model);
		return "ManageBudget";
	}
	
	@RequestMapping(value = "ManageBudget/AddCategory", method = RequestMethod.POST)
	public String addBudgetCategory(Model model, @RequestParam("category") String categoryTitle) throws Exception {

		AccountJdbc.query.addTransactionCategory(currentUser(), categoryTitle);
		populateBudget(model);
		return "ManageBudget";
	}
	
	@RequestMapping(value = "ManageBudget/DeleteCategory", method = RequestMethod.POST)
	public String deleteBudgetCategory(@Valid String category, Model model) throws Exception {

		AccountJdbc.query.deleteTransactionCategory(Integer.parseInt(category));
		populateBudget(model);
		return "ManageBudget";
	}
	
	@RequestMapping(value = "RemoveExpense/{budgetId}", method = RequestMethod.GET)
	public String removeExpense(@PathVariable(value="budgetId") Integer budgetId, Model model) throws Exception {

		AccountJdbc.query.deleteBudgetItem(budgetId);
		populateBudget(model);
		return "ManageBudget";
	}
	
	public String budgetTimeframe()
	{
		LocalDate currentDate = LocalDate.now();
		String currentMonth = currentDate.getMonth().toString();
		String currentMonthLowerCase = currentMonth.toLowerCase();
		String currentMonthFormatted = currentMonthLowerCase.substring(0, 1).toUpperCase() + currentMonthLowerCase.substring(1);
		
		String firstDayOfMonth = currentMonthFormatted + " 1, " + currentDate.getYear();
	//	String fifteenthOfMonth = currentMonthFormatted + " 15, " + currentDate.getYear();
		int lengthOfMonth = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), 1).getMonth().length(Year.of(currentDate.getYear()).isLeap());
		String lastDayOfMonth = currentMonthFormatted + " " + lengthOfMonth + ", " + currentDate.getYear();
				
		String budgetTimeframe;


		budgetTimeframe = firstDayOfMonth + " - " + lastDayOfMonth;

		return budgetTimeframe;
	}
	
	public String budgetStartDate()
	{
		String budgetStartDate = "";
		
		LocalDate currentDate = LocalDate.now();
		int currentMonth = currentDate.getMonthValue();
		int currentYear = currentDate.getYear();
		
//		if (currentDate.getDayOfMonth() <= 15)
//		{
			budgetStartDate = currentMonth + "/01/" + currentYear;
//		}
//		else
//		{
//			budgetStartDate = currentMonth + "/15/" + currentYear;
//		}
//		
		return budgetStartDate;	
	}
	
	public String budgetEndDate()
	{
		String budgetEndDate = "";
		
		LocalDate currentDate = LocalDate.now();
		int currentMonth = currentDate.getMonthValue();
		int currentYear = currentDate.getYear();
		int lengthOfMonth = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), 1).getMonth().length(Year.of(currentDate.getYear()).isLeap());
		
//		if (currentDate.getDayOfMonth() <= 15)
//		{
//			budgetEndDate = currentMonth + "/15/" + currentYear;
//		}
//		else
//		{
			budgetEndDate = currentMonth + "/" + lengthOfMonth + "/" + currentYear;
//		}
//		
		return budgetEndDate;	
	}
	
    public String currentUser()
    {
    	Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	String username = ((UserDetails)principal).getUsername();
    	return username;
    }
}
