package com.example.clinicapp.service;

import com.example.clinicapp.config.TwilioConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class MobileService {

	@Autowired
	private TwilioConfig twilioConfig;
	
	public void MobileNotification(String phoneNo,String name, String userMessage){
	        PhoneNumber to = new PhoneNumber(phoneNo);
	        PhoneNumber from = new PhoneNumber(twilioConfig.getTrialNumber());
	        String notification = "Dear"+name+"your"+userMessage;
	        Message message = Message
	                .creator(to, from,
	                		notification)
	                .create();
	  }
	
}
