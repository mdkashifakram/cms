

  package com.example.clinicapp;
  
  import org.springframework.boot.SpringApplication; 
  import org.springframework.boot.autoconfigure.SpringBootApplication;
  import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
  

  @SpringBootApplication(exclude = {MailSenderAutoConfiguration.class})
  @EnableCaching
  public class ClinicappApplication {
  
  public static void main(String[] args) {
	  SpringApplication.run(ClinicappApplication.class, args); 
	  }
  
 }
 