const toggleSidebar = () => {

	if ($(".sidebar").is(":visible")) {

		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");
		
		

	} else {
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
	}

};

const search=()=>{

	let query = $("#search-input").val();

	if(query=='')
	{
		$(".search-result").hide();
	}else
	{

		let url=`http://localhost:8080/search/${query}`;

		fetch(url).then((response)=>{
			return response.json();
		}).then(data=>{

			let text=`<div class='list-group'>`;

			data.forEach(contact => {
				text+=`<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'>${contact.name}</a>`
			});

			text+=`</div>`;

			$(".search-result").html(text);
			$(".search-result").show();

		});


		
	}

};


//first request to create order

const paymentStart=()=>{

let amount=$("#payment_field").val();
console.log("amount",amount);
if(amount==''||amount==null)
{
	
	swal("Failed","amount is require !!", "error");
	return;
}

//code....
//we will use ajax for create order


$.ajax({
	url:'/user/create_order',
	data:JSON.stringify({amount:amount,info:'order_request'}),
	contentType:'application/json',
	type:'POST',
	dataType:'json',
	success:function(response){

		console.log(response);
		if(response.status=='created')
		{
			//open payment form
			let option={
				key:'rzp_test_eH3R6ECpcybM8O',
				amount:response.amount,
				currency: "INR",
				name:'Smart Contact Manager',
				description:'For subscription',
				image:'https://previews.123rf.com/images/keath/keath1609/keath160900267/63404714-payment-icon-money-and-payment-red-button-badge-illustration.jpg',
				order_id:response.id,
				handler:function(response){
					console.log(response.razorpay_payment_id)
					console.log(response.razorpay_order_id)
					console.log(response.razorpay_signature)
					console.log("Payment successfull !!")
					
					updatePaymentOnServer(response.razorpay_payment_id,response.razorpay_order_id,"paid");
					
					

				},
				prefill: {
					name: "",
					email: "",
					contact: ""
				},
				notes: {
					address: "Smart Contact Manager",
				},
				theme: {
					color: "#3399cc"
				},
			};

			let rzp=new Razorpay(option);

			rzp.on('payment.failed', function (response){
				console.log(response.error.code);
				console.log(response.error.description);
				console.log(response.error.source);
				console.log(response.error.step);
				console.log(response.error.reason);
				console.log(response.error.metadata.order_id);
				console.log(response.error.metadata.payment_id);
				swal("Failed","Oops Payment failed...", "error");
		});

			rzp.open();

		}

	},
	error:function(error)
	{
		console.log(error);
		alert("somthing went wrong!!")
	}
})



};

//

function updatePaymentOnServer(payment_id,order_id,status)
{
	$.ajax({
	url:'/user/update_order',
	data:JSON.stringify({payment_id:payment_id,order_id:order_id,status:status}),
	contentType:'application/json',
	type:'POST',
	dataType:'json',
	success:function(response){
		swal("Good job!", "congretulation !! Payment Successfull", "success");
	},
	error:function(error)
	{
		swal("Failed","Your payment is successful , but we did not cget on server...", "error");
	}
	})
}


