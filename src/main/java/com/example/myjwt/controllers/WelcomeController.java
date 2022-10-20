package com.example.myjwt.controllers;

import java.util.Collections;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.myjwt.models.Category;
import com.example.myjwt.models.EvaluationResult;
import com.example.myjwt.models.EvaluationResultCategory;
import com.example.myjwt.models.Hexcode;
import com.example.myjwt.models.Role;
import com.example.myjwt.models.Skill;
import com.example.myjwt.models.User;
import com.example.myjwt.models.enm.ERole;
import com.example.myjwt.repo.EvaluationResultCategoryRepository;
import com.example.myjwt.repo.EvaluationResultRepository;
import com.example.myjwt.repo.HexCodeRepository;
import com.example.myjwt.repo.RoleRepository;
import com.example.myjwt.repo.SkillRepository;
import com.example.myjwt.repo.UserRepository;
import com.example.myjwt.security.services.RoleService;
import com.example.myjwt.security.services.UserService;
import com.example.myjwt.util.AppConstants;
import com.example.myjwt.repo.CategoryRepository;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class WelcomeController {

	@Autowired
	UserRepository userRepository;

	@Autowired
	HexCodeRepository hexCodeRepository;

	@Autowired
	EvaluationResultRepository evaluationResultRepository;

	@Autowired
	EvaluationResultCategoryRepository evaluationResultCategoryRepository;

	@Autowired
	SkillRepository skillRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	private UserService userService;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@GetMapping("/welcome/all")
	public String initiateWelcomePageCall() {
		String msg = "Welcome to the App. " + "Let's Login or SignUp";
		System.out.println(msg);
		setInitialValuesInDB();
		return "";
	}

	@GetMapping("/all")
	public String allAccess() {
		return "Welcome to the App. " + "Let's Login or SignUp";
	}

	@GetMapping("/user")
	@PreAuthorize("hasRole('USER')")
	public String userAccess() {
		return "Hello user! You are authorized :) ";
	}

	@GetMapping("/verify/{vcode}")
	public String verifyUser(@PathVariable String vcode) {
		System.out.println(vcode);
		Hexcode hexCode = hexCodeRepository.findByCode(vcode);
		if (hexCode == null) {
			return "verify_failed! Verification invalid or already verified!";
		} else {

			switch (hexCode.getTableName()) {
				case AppConstants.TBL_USER:
					switch (hexCode.getAction()) {
						case AppConstants.HEXCODE_ACTION_VALIDATE:
							switch (hexCode.getSubAction()) {
								case AppConstants.HEXCODE_SUBACTION_EMAIL:

									User user = userRepository.findById(hexCode.getRefId())
											.orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

									// user.setIsVerified(true);
									updateUserAndDeleteHexCode(user, hexCode);

									return "verify_success!!!   Login to explore!!!";
							}
							break;
					}
					break;
			}

			return "Could not find relevant authentication !!!";
		}

	}

	@Transactional
	private void updateUserAndDeleteHexCode(User user, Hexcode hexCode) {
		userRepository.save(user);
		hexCodeRepository.delete(hexCode);
	}

	private void createDefaultRoles() {
		Role role = new Role(ERole.Admin);
		roleRepository.save(role);
		roleRepository.flush();
	}

	private void createDefaultCategories() {

		String[] billabilityCategories = { "Billable", "Planned Billable (NBL)", "Planned Billable (Billable)",
				"Training (NBL)", "Training (Billable)", "Planned to Release", "Release Initiated",
				AppConstants.NO_BILLABILITY_PLAN, "Duplicate Allocation" };

		for (int i = 0; i < billabilityCategories.length; i++) {
			Category category = new Category();
			category.setCatGroup(AppConstants.CATEGORY_BILLABILITY);
			category.setGroupKey(AppConstants.CATEGORY_BILLABILITY);
			category.setGroupValue(billabilityCategories[i]);
			categoryRepository.save(category);
			categoryRepository.flush();
		}

	}

	private void createDefaultUser() {
		User user = new User();

		user.setFullName("admin");
		user.setUserName("admin");
		user.setEmail("admin@gmail.com");
		user.setIsActive(true);
		user.setIsApproved(true);
		user.setIsVerified(true);
		user.setPassword(passwordEncoder.encode("Admin@123"));

		Role userRole = new Role(ERole.Admin);

		user.setRoles(Collections.singleton(userRole));

		userRepository.save(user);
		userRepository.flush();
	}

	private void createSkillTables() {
		String[] skillFamiliesArr = { "Java", "Java Springboot", "Java Springboot Microservices",
				"Java Springboot Microservices AWS", "Java Springboot AWS", "Java Springboot React" };

		String[] skillDetailsArr = { "Java", "Java Springboot", "Java Springboot Microservices",
				"Java Springboot Microservices AWS", "Java Springboot AWS", "Java Springboot React" };

		for (int i = 0; i < skillFamiliesArr.length; i++) {
			Skill skill = new Skill();
			skill.setSkillName(skillFamiliesArr[i]);
			skill.setSkillDetails(skillDetailsArr[i]);
			skillRepository.save(skill);
		}

		String[] evaluationResultArr = { "Rejected", "Selected", "Recommended for Next Round" };
		String[] evaluationResultCategoryArr = { "Rejected Remote Only", "Screen Reject", "Internal Reject",
				"Client Reject", "Less Experience", "Not Reachable", "Not Interested/Available" };

		for (int i = 0; i < evaluationResultArr.length; i++) {
			EvaluationResult result = new EvaluationResult();
			result.setResult(evaluationResultArr[i]);
			evaluationResultRepository.save(result);
		}

		for (int i = 0; i < evaluationResultCategoryArr.length; i++) {
			EvaluationResultCategory resultCategory = new EvaluationResultCategory();
			resultCategory.setResultCategory(evaluationResultCategoryArr[i]);
			evaluationResultCategoryRepository.save(resultCategory);
		}
	}

	public void setInitialValuesInDB() {
		System.out.println("Creating initial database");
		User user = userRepository.findByEmail("admin@gmail.com");
		if (user == null) {
			createDefaultRoles();
			createDefaultUser();
			createSkillTables();
			createDefaultCategories();
		}

	}
}
