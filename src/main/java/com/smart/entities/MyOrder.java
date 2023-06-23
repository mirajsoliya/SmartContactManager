package com.smart.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class MyOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long myorderId;
	
	private String oredrId;
	private String amount;
	private String receipt;
	private String status;
	
	@ManyToOne
	private User user;
	
	private String paymentId;

	public Long getMyorderId() {
		return myorderId;
	}

	public void setMyorderId(Long myorderId) {
		this.myorderId = myorderId;
	}

	public String getOredrId() {
		return oredrId;
	}

	public void setOredrId(String oredrId) {
		this.oredrId = oredrId;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getReceipt() {
		return receipt;
	}

	public void setReceipt(String receipt) {
		this.receipt = receipt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}
	
	
	
	
	
}
