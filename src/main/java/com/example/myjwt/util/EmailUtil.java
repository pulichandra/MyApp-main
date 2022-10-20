package com.example.myjwt.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.example.myjwt.beans.AsgnmtAssociate;

public class EmailUtil {
	public static void sendEmailAssignmentsAndProjectsEnding(JavaMailSender mailSender,
			List<AsgnmtAssociate> asgnmtEndingList, HashMap<Long, AsgnmtAssociate> projectsEndingList)
			throws MessagingException, UnsupportedEncodingException {
		sendAssignmentEndDateEmail(mailSender, asgnmtEndingList);
		sendProjectEndDateEmail(mailSender, projectsEndingList);
	}

	public static void sendAssignmentEndDateEmail(JavaMailSender mailSender, List<AsgnmtAssociate> asgnmtEndingList)
			throws MessagingException, UnsupportedEncodingException {
		String toAddress = "narenkgcts@outlook.com";
		String fromAddress = "narenkgcts@outlook.com"; // ; password --> @DevTeam
		String senderName = "Account Admin";
		String subjectAssignmentsEnding = "!! Alert !! - Assignment ending in next 60 days";
		StringBuffer email = new StringBuffer(
				"Dear All,<br>" + "Assignment for following associates are ending in next 60 days:<br>");

		email.append("<html><body>" + "<table style='border:2px solid black'>");
		Collections.sort(asgnmtEndingList, AsgnmtAssociate.AssignmentEndComparator);

		email.append("<tr bgcolor=\"#FFEDB9\">");
		email.append("<td>");
		email.append("Associate ID");
		email.append("</td>");

		email.append("<td>");
		email.append("Associate Name");
		email.append("</td>");

		email.append("<td>");
		email.append("Project ID");
		email.append("</td>");

		email.append("<td>");
		email.append("Project Name");
		email.append("</td>");

		email.append("<td>");
		email.append("LOB");
		email.append("</td>");

		email.append("<td>");
		email.append("Current assignment End Date");
		email.append("</td>");

		email.append("<tr>");

		for (int i = 0; i < asgnmtEndingList.size(); i++) {
			AsgnmtAssociate asgnmtAssociate = asgnmtEndingList.get(i);

			if (i % 2 != 0)
				email.append("<tr bgcolor=\"#DCFBFF\">");
			else
				email.append("<tr>");
			email.append("<td>");
			email.append(/* asgnmtAssociate.getAssociateID() */"XXXXX");
			email.append("</td>");

			email.append("<td>");
			email.append(/* asgnmtAssociate.getAssociateName() */"XXXXX");
			email.append("</td>");

			email.append("<td>");
			email.append(/* asgnmtAssociate.getProjectID() */"XXXXX");
			email.append("</td>");

			email.append("<td>");
			email.append(/* asgnmtAssociate.getProjectDescription() */"XXXXX");
			email.append("</td>");

			email.append("<td>");
			email.append(/* asgnmtAssociate.getlOB() */"XXXXX");
			email.append("</td>");

			email.append("<td>");
			email.append(asgnmtAssociate.getAssignmentEnddate());
			email.append("</td>");

			email.append("<tr>");

		}
		email.append("</table>");
		email.append("<br>" + "Thank you,<br>" + "Account Team");

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subjectAssignmentsEnding);

		helper.setText(email.toString(), true);

		mailSender.send(message);
	}

	public static void sendProjectEndDateEmail(JavaMailSender mailSender,
			HashMap<Long, AsgnmtAssociate> projectsEndingList) throws MessagingException, UnsupportedEncodingException {
		String toAddress = "narenkgcts@outlook.com";
		String fromAddress = "narenkgcts@outlook.com"; // ; password --> @DevTeam
		String senderName = "Account Admin";
		String subjectAssignmentsEnding = "!! Alert !! - Projects ending in next 60 days";
		StringBuffer email = new StringBuffer("Dear All,<br>" + "Following projects are ending in next 60 days:<br>");

		email.append("<html><body>" + "<table style='border:2px solid black'>");

		List<Map.Entry<Long, AsgnmtAssociate>> entryList = new ArrayList<Map.Entry<Long, AsgnmtAssociate>>(
				projectsEndingList.entrySet());

		Collections.sort(entryList, AsgnmtAssociate.ProjectEndComparator);

		email.append("<tr bgcolor=\"#FFEDB9\">");
		email.append("<td>");
		email.append("Project ID");
		email.append("</td>");

		email.append("<td>");
		email.append("Project Name");
		email.append("</td>");

		email.append("<td>");
		email.append("LOB");
		email.append("</td>");

		email.append("<td>");
		email.append("Current Project End Date");
		email.append("</td>");

		email.append("<tr>");
		int i=0;
		for (Map.Entry<Long, AsgnmtAssociate> entry : projectsEndingList.entrySet()) {
			
			AsgnmtAssociate asgnmtAssociate = entry.getValue();
			if (i % 2 != 0)
				email.append("<tr bgcolor=\"#DCFBFF\">");
			else
				email.append("<tr>");

			email.append("<td>");
			email.append(/* asgnmtAssociate.getProjectID() */"XXXXX");
			email.append("</td>");

			email.append("<td>");
			email.append(/* asgnmtAssociate.getProjectDescription() */"XXXXX");
			email.append("</td>");

			email.append("<td>");
			email.append(/* asgnmtAssociate.getlOB() */"XXXXX");
			email.append("</td>");

			email.append("<td>");
			email.append(asgnmtAssociate.getProjectEndDate());
			email.append("</td>");

			email.append("<tr>");
			
			i++;
		}


		email.append("</table>");
		email.append("<br>" + "Thank you,<br>" + "Account Team");

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subjectAssignmentsEnding);

		helper.setText(email.toString(), true);

		mailSender.send(message);
	}
}
