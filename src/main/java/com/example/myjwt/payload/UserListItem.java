package com.example.myjwt.payload;

import com.example.myjwt.models.User;
import com.example.myjwt.models.enm.EGrade;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

public class UserListItem {
	private Long id;
	private String userName;
	private String grade;
	private String gradeId;
	private String fullName;
	private String email;
	private Boolean isVerified;
	private String manager;
	private Boolean isApproved;
	private String accountName;
	private String projectName;
	private Long managerId;

	public UserListItem(User user) {
		this.id = user.getId();
		this.userName = user.getUserName();
		this.fullName = user.getFullName();
		if (user.getGrade() != null) {
			this.grade = user.getGrade().getName().name();
			this.gradeId = user.getGrade().getId() + "";
		}

	}

	public UserListItem(User user, Boolean loadManager) {
		this.id = user.getId();
		this.userName = user.getUserName();
		this.fullName = user.getFullName();
		System.out.println("fullName = " + fullName);
		System.out.println("user.getGrade() = " + user.getGrade());
		if (user.getGrade() != null) {
			this.grade = user.getGrade().getDescription();
			this.gradeId = user.getGrade().getId() + "";
			System.out.println("user.getGrade().getDescription(); ----------------------- "
					+ user.getGrade().getDescription() + ":" + this.gradeId);
		}
		this.email = user.getEmail();
		System.out.println("user.getIsVerified() ====== " + user.getIsVerified());
		this.isVerified = user.getIsVerified();
		this.isApproved = user.getIsApproved();

		
	}

	public UserListItem(NativeQueryUser user) {
		this.id = user.getId();
		this.userName = user.getUserName();
		this.fullName = user.getFullName();
		System.out.println("fullName = " + fullName);
		if (user.getGradeId() != null) {
			int index = (int) (user.getGradeId()).longValue();
			this.grade = (EGrade.values()[index - 1]).name();
			this.gradeId = user.getGradeId() + "";
		}
		this.managerId = user.getManagerId();

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Boolean getIsVerified() {
		return isVerified;
	}

	public void setIsVerified(Boolean isVerified) {
		this.isVerified = isVerified;
	}

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public Boolean getIsApproved() {
		return isApproved;
	}

	public void setIsApproved(Boolean isApproved) {
		this.isApproved = isApproved;
	}

	public String getGradeId() {
		return gradeId;
	}

	public void setGradeId(String gradeId) {
		this.gradeId = gradeId;
	}

	public Long getManagerId() {
		return managerId;
	}

	public void setManagerId(Long managerId) {
		this.managerId = managerId;
	}

}
