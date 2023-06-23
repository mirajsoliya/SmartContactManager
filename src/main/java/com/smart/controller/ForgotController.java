package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {

	Random random = new Random(1000);

	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}

	@PostMapping("/send-otp")
	public String changepass(@RequestParam("email") String email, HttpSession session) {

		int otp = random.nextInt(999999);

		// write sent otp on email...

		String subject = "OTP FROM Smart Contact Manager";
		String message = ""
				+"<div style='border:1px solid #e2e2e2 ;padding:20px'>"
				+"<h1>"
				+"OTP is "
				+"<b>"+otp
				+"</b>"
				+"</h1>"
				+"</div>";
		String to = email;

		boolean flag = this.emailService.sendEmail(subject, message, to);

		if (flag) {
			session.setAttribute("myotp",otp);
			session.setAttribute("email",email);
			
			
			return "varify_otp";

		} else {
			session.setAttribute("message", "Check your email id!!");
			return "forgot_email_form";
		}

	}
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp")int otp,HttpSession session) {
		
		int myotp=(int)session.getAttribute("myotp");
		String email=(String)session.getAttribute("email");
		
		if(myotp==otp)
		{
			//password change
			User user = this.userRepository.getUserByUserName(email);	
			if(user==null)
			{
				//send error message
				session.setAttribute("message", "User does not exits with this email !!");
				return "forgot_email_form";
			}
			else
			{
				//send change password form
				return "password_change_form";
			}
			
			
		}
		else
		{
			session.setAttribute("message", "you have entered wrong otp");
			return "varify_otp";
		}
		
		
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpass")String newpass,HttpSession session)
	{
		String email=(String)session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newpass));
		this.userRepository.save(user);
		session.setAttribute("message", "you have entered wrong otp");
		
		
		return "redirect:/signin?change=password change successfully..";
	}

}
