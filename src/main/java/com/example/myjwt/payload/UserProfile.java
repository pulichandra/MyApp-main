package com.example.myjwt.payload;

import java.time.Instant;

import com.example.myjwt.payload.response.JwtAuthenticationResponse;

public class UserProfile {
	private Long id;
    private String userName;
    private Instant joinedAt;
    private JwtAuthenticationResponse jwt;

    public UserProfile(Long id, String userName, Instant joinedAt) {
        this.id = id;
        this.userName = userName;
        this.joinedAt = joinedAt;
    }
    
    public UserProfile(Long id, String userName, JwtAuthenticationResponse jwt) {
        this.id = id;
        this.userName = userName;
        this.jwt = jwt;
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

	public Instant getJoinedAt() {
		return joinedAt;
	}

	public void setJoinedAt(Instant joinedAt) {
		this.joinedAt = joinedAt;
	}

	public JwtAuthenticationResponse getJwt() {
		return jwt;
	}

	public void setJwt(JwtAuthenticationResponse jwt) {
		this.jwt = jwt;
	}

}
