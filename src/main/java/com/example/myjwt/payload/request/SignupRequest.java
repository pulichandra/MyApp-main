package com.example.myjwt.payload.request;

import java.util.Set;

import javax.validation.constraints.*;
 
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String userName;
 
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;
    
    @NotBlank
	@Size(max = 50)
	private String fullName;

    @NotBlank
    @Size(max = 50)
    @Email
    private String managerEmail;
    
	private Set<String> role;
    
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
    
	/*
	 * @NotBlank
	 * 
	 * @Size(max = 64) private String verificationCode;
	 * 
	 * private boolean enabled;
	 */
  
    public String getUserName() {
        return userName;
    }
 
    public void setUserName(String userName) {
        this.userName = userName;
    }
 
    public String getEmail() {
        return email;
    }
 
    public void setEmail(String email) {
        this.email = email;
    }
 
    public String getPassword() {
        return password;
    }
 
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Set<String> getRole() {
      return this.role;
    }
    
    public void setRole(Set<String> role) {
      this.role = role;
    }
    
    public String getManagerEmail() {
		return managerEmail;
	}

	public void setManagerEmail(String managerEmail) {
		this.managerEmail = managerEmail;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	/*
	 * public String getVerificationCode() { return verificationCode; }
	 * 
	 * public void setVerificationCode(String verificationCode) {
	 * this.verificationCode = verificationCode; }
	 * 
	 * public boolean isEnabled() { return enabled; }
	 * 
	 * public void setEnabled(boolean enabled) { this.enabled = enabled; }
	 */
    
}
