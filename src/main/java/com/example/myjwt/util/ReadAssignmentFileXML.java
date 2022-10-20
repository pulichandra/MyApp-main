package com.example.myjwt.util;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.RestController;

import com.example.myjwt.beans.AsgnmtAssociate;

import javax.xml.XMLConstants;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@RestController
public class ReadAssignmentFileXML {

	private static final String FILENAME = "C:\\Users\\254395\\Downloads\\AssignmentListing_9_16_2022.xls";
	// private static final String FILENAME = "C:\\Users\\254395\\OneDrive -
	// Cognizant\\Cognizant\\Accounts\\ADM\\JPMC\\Operations\\NBL.xlsx";

	@Autowired
	private JavaMailSender mailSender;

	public static void main(String[] args) {
		ReadAssignmentFileXML xml = new ReadAssignmentFileXML();
		// xml.readAssignmentXML();
	}

	public JavaMailSender getJavaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("smtp.office365.com");
		mailSender.setPort(587);

		mailSender.setUsername("narenkgcts@outlook.com");
		mailSender.setPassword("DevTeam@101");

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.debug", "true");

		return mailSender;
	}
}
