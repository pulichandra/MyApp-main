package com.example.myjwt.controllers;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.net.URI;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.mail.javamail.JavaMailSender;

import com.example.myjwt.models.Hexcode;
import com.example.myjwt.models.Role;
import com.example.myjwt.models.User;
import com.example.myjwt.models.enm.ERole;
import com.example.myjwt.payload.UserProfile;
import com.example.myjwt.payload.UserSummary;
import com.example.myjwt.payload.request.LoginRequest;
import com.example.myjwt.payload.request.SignupRequest;
import com.example.myjwt.payload.response.JwtAuthenticationResponse;
import com.example.myjwt.payload.response.ApiResponse;
import com.example.myjwt.repo.HexCodeRepository;
import com.example.myjwt.repo.RoleRepository;
import com.example.myjwt.repo.UserRepository;
import com.example.myjwt.security.jwt.JwtTokenProvider;
import com.example.myjwt.security.services.UserPrincipal;
import com.example.myjwt.util.AppConstants;
import com.example.myjwt.util.PMUtils;

import net.bytebuddy.utility.RandomString;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/auth")

public class AuthController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	HexCodeRepository hexCodeRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	JwtTokenProvider tokenProvider;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

		
		System.out.println("----------------------------------I am here:1:"+loginRequest.getPassword());
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUserName(), loginRequest.getPassword()));
		System.out.println("-----------------------------I am here:8");
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = tokenProvider.generateJwtToken(authentication);
		System.out.println("I am here:7");
		User user = userRepository.findByUserName(loginRequest.getUserName()).orElseThrow(
				() -> new UsernameNotFoundException("User Not Found with username: " + loginRequest.getUserName()));
		
		System.out.println("I am here:6");
		
		if (user.getIsVerified()) {
			System.out.println("I am here:5");
			if (user.getIsApproved()) {
				System.out.println("I am here:4");
				if (user.getIsActive()) {
					System.out.println("I am here:2");
					UserProfile userProfile = new UserProfile(user.getId(), user.getUserName(), new JwtAuthenticationResponse(jwt));
					return ResponseEntity.ok(userProfile);
				} else {
					return ResponseEntity.badRequest().body(new ApiResponse(false, "Error: User not active"));
				}
			} else {
				return ResponseEntity.badRequest().body(new ApiResponse(false, "Error: Approval pending with manager"));
			}
		} else {
			System.out.println("I am here:3");
			return ResponseEntity.badRequest().body(new ApiResponse(false, "Error: Email not verified"));
		}
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest, HttpServletRequest request)
			throws UnsupportedEncodingException, MessagingException {
		if (userRepository.existsByUserName(signUpRequest.getUserName().trim())) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, "Error: Username is already exist!"));
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail().trim())) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, "Error: Email is already exist!"));
		}

		User user = new User();

		user.setIsVerified(false);
		user.setIsActive(true);
		user.setIsApproved(false);
		user.setFullName(signUpRequest.getFullName().trim());
		user.setUserName(signUpRequest.getUserName().trim());
		user.setEmail(signUpRequest.getEmail().trim());
		user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

		/*
		 * Set<String> strRoles = signUpRequest.getRole(); Set<Role> roles = new
		 * HashSet<>();
		 * 
		 * if (strRoles == null) {
		 * 
		 * } else { strRoles.forEach(role -> { switch (role) { case "admin": Role
		 * adminRole = roleRepository.findByName(ERole.Admin) .orElseThrow(() -> new
		 * RuntimeException("Error: Role is not found.")); roles.add(adminRole);
		 * 
		 * break; default: Role userRole = roleRepository.findByName(ERole.Associate)
		 * .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
		 * roles.add(userRole); } }); }
		 */

		Role userRole = roleRepository.findByName(ERole.Associate);

		user.setRoles(Collections.singleton(userRole));

		System.out.println("signUpRequest.getManagerEmail()=" + signUpRequest.getManagerEmail().trim());

		User manager = userRepository.findByEmail(signUpRequest.getManagerEmail().trim());

		if (manager == null) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, "Error: Manager email doesn't exist!"));
		}

	

		// String siteURL = request.getRequestURL().toString();

		Hexcode hexCode = new Hexcode();
		hexCode.setTableName(AppConstants.TBL_USER);
		hexCode.setAction(AppConstants.HEXCODE_ACTION_VALIDATE);
		hexCode.setSubAction(AppConstants.HEXCODE_SUBACTION_EMAIL);
		String randomCode = RandomString.make(64);
		hexCode.setCode(randomCode);

		User result = registerTransaction(user, hexCode);
		
		URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(result.getUserName()).toUri();

		return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully! Please verify the mail that has sent to you!!"));
	}

	@Transactional
	private User registerTransaction(User user, Hexcode hexCode)
			throws UnsupportedEncodingException, MessagingException {
		//TODO: Remove this
		user.setIsVerified(true);
		User result = userRepository.save(user);
		hexCode.setRefId(user.getId());
		hexCodeRepository.save(hexCode);
		//TODO: Umcomment this
		//sendVerificationEmail(user, AppConstants.UI_URL, hexCode.getCode());
		return result;
	}
	
	//TODO: Email exception not reflecting on UI

	private void sendVerificationEmail(User user, String siteURL, String hexCode)
			throws MessagingException, UnsupportedEncodingException {
		String toAddress = user.getEmail();
		String fromAddress = "rapplicationdevelopment@gmail.com"; // ; password --> @DevTeam
		String senderName = "App Develop";
		String subject = "Please verify your registration";
		String content = "Dear [[name]],<br>" + "Please click the link below to verify your registration:<br>"
				+ "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>" + "Thank you,<br>" + "App Dev Team";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);

		content = content.replace("[[name]]", user.getUserName());
		String verifyURL = siteURL + "/verify?code=" + hexCode;

		content = content.replace("[[URL]]", verifyURL);

		helper.setText(content, true);

		mailSender.send(message);

	}
}
