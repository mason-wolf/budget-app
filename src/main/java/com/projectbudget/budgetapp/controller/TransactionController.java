package com.projectbudget.budgetapp.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.projectbudget.budgetapp.dao.AccountJdbc;
import com.projectbudget.budgetapp.model.Account;
import com.projectbudget.budgetapp.model.Category;

@Controller
public class TransactionController {
	
	@GetMapping({"/AddExpense"})
	public String addExpense(Model model)
	{
		showAccountDetails(model);
		return "AddExpense";
	}
	
	@RequestMapping(value = "/AddBudgetCategory", method = RequestMethod.POST)
	public String addBudgetCategory(Model model, @RequestParam("category") String categoryTitle) throws Exception {

		AccountJdbc.query.addTransactionCategory(currentUser(), categoryTitle);
		showAccountDetails(model);
		return "AddExpense";
	}
	
	@RequestMapping(value = "/DeleteBudgetCategory", method = RequestMethod.POST)
	public String deleteBudgetCategory(@Valid String category, Model model) throws Exception {

		AccountJdbc.query.deleteTransactionCategory(Integer.parseInt(category));
		showAccountDetails(model);
		return "AddExpense";
	}
	
	
	public void showAccountDetails(Model model)
	{
		Account account = AccountJdbc.query.getAccount(currentUser());
		List<Category> expenseCategories = AccountJdbc.query.getTransactionCategories(currentUser());
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
