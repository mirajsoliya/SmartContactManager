package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import com.razorpay.*;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private MyOrderRepository myOrderRepository;

	// adding common data
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println(userName);

		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER :" + user);
		model.addAttribute("user", user);

	}

	// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {

		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	// open add from handler

	@RequestMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// processing add contact

	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute Contact contact,BindingResult rs , @RequestParam("profileimage") MultipartFile file,
			Principal principal, HttpSession session , Model model) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			if (rs.hasErrors()){
				System.out.println("ERROR :" + rs.toString());
				model.addAttribute("contact",contact);
				return "normal/add_contact_form";
			}
			//processing and uploading file....
			
			if (file.isEmpty()) {
				
				System.out.println("file is Empty");
				contact.setImage("contact.png");
				
			}else
			{
				contact.setImage(file.getOriginalFilename());
				
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING );
				System.out.println("file uploaded");
			}
			
			user.getContacts().add(contact);
			contact.setUser(user);
			this.userRepository.save(user);

			System.out.println("Contact :" + contact);

			System.out.println("ADDED to data base");
			
			//message success
			session.setAttribute("message",new Message("Your Contact is added !! Add more.","success"));
			
			
		} catch (Exception e) {
			System.out.println("ERROR :" + e.getMessage());
			e.printStackTrace();
			//message error
			
			session.setAttribute("message",new Message("Something went wrong!!","danger"));
		}
		return "normal/add_contact_form";
	}

	//show contact handller
	//per page - 5[n]
	//current page =0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page")Integer page,Model model,Principal principal)
	{
		model.addAttribute("title", "Show User Contacts");
		String username = principal.getName();
		
		User user = this.userRepository.getUserByUserName(username);
		
		Pageable pageable = PageRequest.of(page, 5); 
		
		Page<Contact> contacts  = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentpage",page);
		model.addAttribute("totalpages",contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	//showing perticular contact details.
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId , Model model , Principal principal)
	{
		Optional<Contact> contactoptional = this.contactRepository.findById(cId);
		Contact contact = contactoptional.get();
		
		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);
		
		if(user.getId()==contact.getUser().getId())
		{
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());
		}
		
		
		return "normal/contact_detail";
	}
	
	//delete contact
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId")Integer cId,Model model,Principal principal,HttpSession session) 
	{
		
		Contact contact = this.contactRepository.findById(cId).get();
		//

		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		if(user.getId()==contact.getUser().getId())
		{
			
			user.getContacts().remove(contact);
			this.userRepository.save(user);
			session.setAttribute("message",new Message("Contact delete successfully...","success")); 
		}
		
		return "redirect:/user/show-contacts/0";
	}
	
	//update contact
	@PostMapping("/update-contact/{cid}")
	public String updateContact(@PathVariable("cid")Integer cid,Model model)
	{
		model.addAttribute("title","Update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	//update contact handler process-update
	@RequestMapping(value = "/process-update" , method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact , @RequestParam("profileimage") MultipartFile file , 
			Model model , HttpSession session , Principal principal)
	{
		
		//old contact detail
		
		Contact oldeContactDetail = this.contactRepository.findById(contact.getcId()).get();
		
		try {
			if(!file.isEmpty())
			{
				//delete old photo
				
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile,oldeContactDetail.getImage());
				file1.delete();
				
				
				//update new photo
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING );
				contact.setImage(file.getOriginalFilename());
			}
			else
			{
				contact.setImage(oldeContactDetail.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your Contact is Updated...","success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	//Your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title","Profile page");
		return "normal/profile";
	}
	
	@RequestMapping("/setting")
	public String setting(Model model) {

		model.addAttribute("title", "setting");
		return "normal/setting";
	}
	
	@PostMapping("/chnage-password")
	public String changepassword(@RequestParam("oldpass")String oldpass , @RequestParam("newpass") String newpass,Principal principal,HttpSession session)
	{
		String userName = principal.getName();
		User currentuser = this.userRepository.getUserByUserName(userName);
		
	   if(this.bCryptPasswordEncoder.matches(oldpass, currentuser.getPassword()))
	   {
		   currentuser.setPassword(this.bCryptPasswordEncoder.encode(newpass));
		   this.userRepository.save(currentuser);
		   session.setAttribute("message", new Message("Your password is successfully changed!!","success"));
	   }
	   else
	   {
		   session.setAttribute("message", new Message("Please Enter correct old password!!","danger"));
		   return "redirect:/user/setting";
		   
	   }
		
		return "redirect:/user/index";
	}
	
	
	
	//createing order for payment
	@PostMapping("create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object>data , Principal principal) throws Exception
	{
		System.out.println(data);
		
		int amt = Integer.parseInt(data.get("amount").toString());
	    var client=new RazorpayClient("rzp_test_eH3R6ECpcybM8O", "GJTFii897hSzW46LA53Q37y6");
	    
	    JSONObject ob= new JSONObject();
	    ob.put("amount", amt*100);//transfer paisa to rupeeya
	    ob.put("currency", "INR");
	    ob.put("receipt", "txn_235425");
	    
	    //creating new order
	    
	    Order order = client.Orders.create(ob);
	    System.out.println(order);
	    
	    //if you can save all data in databased
	    
	    MyOrder myOrder = new MyOrder();
	    myOrder.setAmount(order.get("amount")+"");
	    myOrder.setOredrId(order.get("id")); 
	    myOrder.setPaymentId(null);
	    myOrder.setStatus("created");
	    myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
	    myOrder.setReceipt(order.get("receipt"));
	    
	    this.myOrderRepository.save(myOrder);
	    
		
		return order.toString();
	}
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object>data)
	{
		MyOrder myOrder = this.myOrderRepository.findByOredrId(data.get("order_id").toString());
		myOrder.setPaymentId(data.get("payment_id").toString());
		myOrder.setStatus(data.get("status").toString());
		this.myOrderRepository.save(myOrder);
		System.out.println(data);
		return ResponseEntity.ok(Map.of("msg","updated"));
	}
	
}
