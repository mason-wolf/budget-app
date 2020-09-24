package com.projectbudget.budgetapp;

import javax.sql.DataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.projectbudget.budgetapp.dao.UserJdbc;


@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	DataSource dataSource;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {

    	http.authorizeRequests().antMatchers("/css/**", "/js/**", "/images/**").permitAll();
    	http.formLogin().defaultSuccessUrl("/Dashboard", true);
        http
        .authorizeRequests()
            .antMatchers("/resources/**").permitAll() 
            .antMatchers("/CreateAccount").permitAll()
            .antMatchers("/ForgotPassword").permitAll()   
            .antMatchers("/ResetPassword/{userToken}").permitAll()      
            .anyRequest().hasAnyRole("USER")
            .and()
        .formLogin()
            .loginPage("/login")
            .permitAll()
            .and()
        .logout()       
        	.logoutUrl("/index")
            .permitAll();  
        
    }
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    	
       	dataSource = UserJdbc.query.getDataSource();
        auth.jdbcAuthentication()
        .passwordEncoder(new BCryptPasswordEncoder())
        .dataSource(dataSource);
    }
    
   
    @Bean
    public PasswordEncoder passwordEncoder() {
        return passwordEncoder;
    }
}