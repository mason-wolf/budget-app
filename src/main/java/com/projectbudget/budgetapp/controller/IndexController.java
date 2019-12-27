package com.projectbudget.budgetapp.controller;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.projectbudget.budgetapp.dao.UserJdbc;
import com.projectbudget.budgetapp.model.User;

@Controller
public class IndexController {


    @GetMapping({"/login"})
    public String login() {
    	
    	return "index";
    }
    
    @GetMapping({"/CreateAccount"})
    public String createAccount() {
    	return "CreateAccount";
    }
   
    @GetMapping({"/error"})
    public String error()
    {
    	return "index";
    }
   
	@RequestMapping(value = "/CreateAccount", method = RequestMethod.POST)
	public String createAccount(Model model, @RequestParam("username") String username, @RequestParam("password") String password) throws Exception {
		
		User user = new User(username, password);

		User newUser = UserJdbc.query.getUser(user.getUserName());

		if (!validEmail(user.getUserName()))
		{
			model.addAttribute("invalidEmail", "Please enter a valid email.");
			return "CreateAccount";
		}
		
		if (newUser == null)
		{
		    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		    String encodedPassword = encoder.encode(password);
			user.setPassword(encodedPassword);	
			UserJdbc.query.create(user.getUserName(), user.getPassword());
		}
		else
		{
			model.addAttribute("accountError", "Account already exists.");
			return "CreateAccount";
		}

		return "Dashboard";
	}
	
	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = 
		    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

		public static boolean validEmail(String emailStr) {
		        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
		        return matcher.find();
		}
 }
