package com.projectbudget.budgetapp.controller;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projectbudget.budgetapp.MailSender;
import com.projectbudget.budgetapp.dao.AccountJdbc;
import com.projectbudget.budgetapp.dao.UserJdbc;
import com.projectbudget.budgetapp.model.Account;
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
    
	@GetMapping({"/ForgotPassword"})
	public String forgotPassword()
	{
		return "ForgotPassword";
	}
	
	@RequestMapping(value = "/ForgotPassword", method = RequestMethod.POST)
	public String sendTemporaryPassword(@RequestParam("username") String username, Model model)
	{
		if (!validEmail(username))
		{
			model.addAttribute("error", "Please enter a valid email.");
			return "ForgotPassword";
		}
		else
		{
			User user = UserJdbc.query.getUser(username);
			
			if (user == null)
			{
				model.addAttribute("error", "Account was not found.");
				return "ForgotPassword";
			}
			else
			{
				MailSender mailSender = new MailSender();
				mailSender.SendPasswordResetLink(username);
				model.addAttribute("accountFound", "Please check your email for a password reset link.");
				return "ForgotPassword";
			}
		}
		
	}
	
	@RequestMapping(value = "/ResetPassword/{userToken}", method = { RequestMethod.GET, RequestMethod.POST } )
	public String resetPassword(@PathVariable(value="userToken") String userToken, @RequestParam(required = false) String password, Model model) throws Exception
	{
		String user = UserJdbc.query.getUserByToken(userToken);
		
		if (user == null)
		{
			return "index";
		}
		else
		{
			model.addAttribute("userToken", userToken);
			
			if (password != null)
			{
				UserJdbc.query.changePassword(user, password);
				return "index";
			}
			
			return "ResetPassword";
		}
	}
	
	@RequestMapping(value = "/CreateAccount", method = RequestMethod.POST)
	public String createAccount(Model model, @RequestParam("username") String username, 
			@RequestParam("password") String password, RedirectAttributes redirectAttrs) throws Exception {
		
		User user = new User(username, password);

		User newUser = UserJdbc.query.getUser(user.getUserName());

		if (!validEmail(user.getUserName()))
		{
			model.addAttribute("invalidEmail", "Please enter a valid email.");
			return "CreateAccount";
		}
		
		if (newUser == null)
		{
			// Create a new user profile and encrypt password.
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
		
		redirectAttrs.addAttribute("userName", user.getUserName());
		
		return "index";
	}
	
	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = 
		    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

		public static boolean validEmail(String emailStr) {
		        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
		        return matcher.find();
		}
 }
