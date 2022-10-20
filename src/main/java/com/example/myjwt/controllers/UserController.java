package com.example.myjwt.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.myjwt.exception.ResourceNotFoundException;
import com.example.myjwt.models.Grade;
import com.example.myjwt.models.User;
import com.example.myjwt.payload.IdentityExists;
import com.example.myjwt.payload.NativeQueryUser;
import com.example.myjwt.payload.UserIdentityAvailability;
import com.example.myjwt.payload.UserListItem;
import com.example.myjwt.payload.UserProfile;
import com.example.myjwt.payload.UserSummary;
import com.example.myjwt.payload.response.ApiResponse;
import com.example.myjwt.repo.GradeRepository;
import com.example.myjwt.repo.UserRepository;
import com.example.myjwt.security.CurrentUser;
import com.example.myjwt.security.services.UserPrincipal;
import com.example.myjwt.security.services.UserService;
import com.example.myjwt.util.PMUtils;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class UserController extends BaseController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;


	@Autowired
	private GradeRepository gradeRepository;

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@GetMapping("/user/me")
	public UserSummary getCurrentUser(@CurrentUser UserPrincipal currentUser) {
		UserSummary userSummary = new UserSummary(currentUser.getId(), currentUser.getUsername(),
				currentUser.getAuthorities());
		System.out.println("userSummary -"+userSummary.getUserName());
		return userSummary;
	}

	@GetMapping("/user/checkUserNameAvailability")
	public UserIdentityAvailability checkUserNameAvailability(@RequestParam(value = "userName") String userName) {
		Boolean isAvailable = !userRepository.existsByUserName(userName);
		return new UserIdentityAvailability(isAvailable);
	}

	@GetMapping("/user/checkEmailAvailability")
	public UserIdentityAvailability checkEmailAvailability(@RequestParam(value = "email") String email) {
		Boolean isAvailable = !userRepository.existsByEmail(email);
		return new UserIdentityAvailability(isAvailable);
	}

	@GetMapping("/user/checkManagerEmailAvailability")
	public UserIdentityAvailability checkManagerEmailAvailability(
			@RequestParam(value = "managerEmail") String managerEmail) {
		Boolean isAvailable = userRepository.existsByEmail(managerEmail);
		return new UserIdentityAvailability(isAvailable);
	}

	@GetMapping("/users/{userName}")
	public UserProfile getUserProfile(@PathVariable(value = "userName") String userName) {
		User user = userRepository.findByUserName(userName)
				.orElseThrow(() -> new ResourceNotFoundException("User", "userName", userName));

		UserProfile userProfile = new UserProfile(user.getId(), user.getUserName(), user.getCreatedAt());

		return userProfile;
	}


	@GetMapping("/user/confirmPDLUserExistence")
	public IdentityExists confirmPDLUserExistence(@RequestParam(value = "pdlUserName") String pdlUserName) {
		List<Long> eligibleGrades = PMUtils.getPDLEligibleGrades();
		Boolean isAvailable = true;
		return new IdentityExists(isAvailable);
	}

	@GetMapping("/user/confirmEDLUserExistence")
	public IdentityExists confirmEDLUserExistence(@RequestParam(value = "edlUserName") String edlUserName) {
		List<Long> eligibleGrades = PMUtils.getEDLEligibleGrades();

		Boolean isAvailable = true;//userRepository
				//.getUserWithGradeOwnedByCurrentUser(getCurrentUserId(), edlUserName, eligibleGrades).size() > 0;
		return new IdentityExists(isAvailable);
	}

	@GetMapping("/user/confirmLobLeadExistenceForUser")
	public IdentityExists confirmLobLeadExistenceForUser(
			@RequestParam(value = "lobLeadUserNameValue") String lobLeadUserNameValue) {
		System.out.println("confirmLobLeadExistenceForUser lobLeadUserNameValue ----------------------- > "
				+ lobLeadUserNameValue + ":" + getCurrentUserId());

		User lobLead = userRepository.findByUserName(lobLeadUserNameValue)
				.orElseThrow(() -> new ResourceNotFoundException("User", "userName", lobLeadUserNameValue));

		Boolean isAvailable = userService.isUserReportingToManager(lobLead.getId(), getCurrentUserId());
		System.out.println("confirmLobLeadExistenceForUser isAvailable ----------------------- > " + isAvailable);
		return new IdentityExists(isAvailable);
	}

	@GetMapping("/user/confirmCustomerLeadExistenceForUser")
	public IdentityExists confirmCustomerLeadExistenceForUser(
			@RequestParam(value = "customerLeadUserName") String customerLeadUserName) {
		System.out.println("confirmLobLeadExistenceForUser customerLeadUserName ----------------------- > "
				+ customerLeadUserName + ":" + getCurrentUserId());

		User customerLead = userRepository.findByUserName(customerLeadUserName)
				.orElseThrow(() -> new ResourceNotFoundException("User", "userName", customerLeadUserName));

		Boolean isAvailable = userService.isUserReportingToManager(customerLead.getId(), getCurrentUserId());
		System.out.println("confirmLobLeadExistenceForUser isAvailable ----------------------- > " + isAvailable);
		return new IdentityExists(isAvailable);
	}

	@GetMapping("/user/confirmSubLobLeadExistenceForUser")
	public IdentityExists confirmSubLobLeadExistenceForUser(
			@RequestParam(value = "subLobLeadUserName") String subLobLeadUserName) {
		System.out.println("confirmSubLobLeadExistenceForUser subLobLeadUserName ----------------------- > "
				+ subLobLeadUserName + ":" + getCurrentUserId());

		User subLobLead = userRepository.findByUserName(subLobLeadUserName)
				.orElseThrow(() -> new ResourceNotFoundException("User", "userName", subLobLeadUserName));

		Boolean isAvailable = userService.isUserReportingToManager(subLobLead.getId(), getCurrentUserId());
		System.out.println("confirmLobLeadExistenceForUser isAvailable ----------------------- > " + isAvailable);
		return new IdentityExists(isAvailable);
	}

	@GetMapping("/user/confirmProjectManagerAvailabilityForUser")
	public IdentityExists confirmProjectManagerAvailabilityForUser(
			@RequestParam(value = "pmUserNameValue") String pmUserNameValue) {
		String pmUserName = pmUserNameValue.trim();
		System.out.println("confirmProjectManagerAvailabilityForUser pmUserNameValue ----------------------- > "
				+ pmUserNameValue + ":" + getCurrentUserId());

		User pmUser = userRepository.findByUserName(pmUserNameValue)
				.orElseThrow(() -> new ResourceNotFoundException("User", "userName", pmUserName));

		Boolean isAvailable = userService.isUserReportingToManager(pmUser.getId(), getCurrentUserId());
		System.out.println("confirmLobLeadExistenceForUser isAvailable ----------------------- > " + isAvailable);
		return new IdentityExists(isAvailable);
	}

	@GetMapping("/user/getAllReporteesOfCurrentUser")
	public List<UserListItem> getAllReporteesOfCurrentUser() {
		List<UserListItem> userList = new ArrayList<UserListItem>();
		List<NativeQueryUser> allReportees = userService.getAllReporteesOf(getCurrentUserId());
		
		System.out.println("allReportees.size()="+allReportees.size());

		for (int i = 0; i < allReportees.size(); i++) {
			userList.add(new UserListItem(allReportees.get(i)));
		}

		return userList;
	}

	@GetMapping("/user/getAllAssociates")
	public List<UserListItem> getAllAssociates() {
		List<UserListItem> userList = new ArrayList<UserListItem>();

		// TODO: Replace all find All
		List<User> allAssociates = userRepository.findByIsActive(true);

		for (int i = 0; i < allAssociates.size(); i++) {
			userList.add(new UserListItem(allAssociates.get(i), true));
		}
		System.out.println("getAllAssociates userList size----------------------- > " + userList.size());
		System.out.println("getAllAssociates userList ----------------------- > " + userList);

		return userList;
	}

	// select managerID, group_concat(userID) from hierarchy group by managerID ;
	@GetMapping("/user/getAllEDLUserNamesOwnedByUser")
	public List<UserListItem> getAllEDLUserNamesOwnedByUser() {
		List<NativeQueryUser> allReportees = userService.getAllReporteesOf(getCurrentUserId());

		List<Long> eligibleGrades = PMUtils.getEDLEligibleGrades();
		List<UserListItem> eligibleEDLs = new ArrayList<UserListItem>();

		for (NativeQueryUser nativeQueryUser : allReportees) {
			if (eligibleGrades.contains(nativeQueryUser.getGradeId())) {
				UserListItem listItem = new UserListItem(nativeQueryUser);
				eligibleEDLs.add(listItem);
			}
		}

		return eligibleEDLs;
	}

	// select managerID, group_concat(userID) from hierarchy group by managerID ;
	@GetMapping("/user/getAllPDLUserNamesOwnedByUser")
	public List<UserListItem> getAllPDLUserNamesOwnedByUser() {
		List<NativeQueryUser> allReportees = userService.getAllReporteesOf(getCurrentUserId());

		List<Long> eligibleGrades = PMUtils.getPDLEligibleGrades();
		List<UserListItem> eligiblePDLs = new ArrayList<UserListItem>();

		for (NativeQueryUser nativeQueryUser : allReportees) {
			if (eligibleGrades.contains(nativeQueryUser.getGradeId())) {
				UserListItem listItem = new UserListItem(nativeQueryUser);
				eligiblePDLs.add(listItem);
			}
		}

		System.out.println("eligiblePDLs.size() == " + eligiblePDLs.size());

		return eligiblePDLs;
	}

	// select managerID, group_concat(userID) from hierarchy group by managerID ;
	@GetMapping("/user/getNewUsersToApprove")
	public List<UserListItem> getNewUsersToApprove() {
		Long currentUserId = getCurrentUserId();

		List<UserListItem> userItemList = new ArrayList<UserListItem>();
	

		return userItemList;
	}

	@GetMapping("/user/updateGradeForAssociate")
	public ResponseEntity<?> updateGradeForAssociate(
			@RequestParam(value = "gradeSelectedValue") Long gradeSelectedValue,
			@RequestParam(value = "userName") String userName) {

		System.out.println("gradeSelectedValue --------------------> " + gradeSelectedValue);

		User user = userRepository.findByUserName(userName)
				.orElseThrow(() -> new ResourceNotFoundException("User", "userName", userName));

		Long currentUserId = getCurrentUserId();


		Grade grade = gradeRepository.findById(gradeSelectedValue)
				.orElseThrow(() -> new ResourceNotFoundException("Grade", "id", gradeSelectedValue));

		user.setGrade(grade);
		User result = userRepository.save(user);

		URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/users/{username}")
				.buildAndExpand(result.getUserName()).toUri();

		System.out.println("saved --------------------> " + gradeSelectedValue);

		return ResponseEntity.created(location).body(new ApiResponse(true, "Grade saved"));
	}

	@GetMapping("/user/updateManagerForAssociate")
	public ResponseEntity<?> updateManagerForAssociate(@RequestParam(value = "managerUsername") String managerUsername,
			@RequestParam(value = "userName") String userName) {

		System.out.println("managerUsername --------------------> " + managerUsername);

		User manager = userRepository.findByUserName(managerUsername)
				.orElseThrow(() -> new ResourceNotFoundException("User", "managerUsername", managerUsername));

		User user = userRepository.findByUserName(userName)
				.orElseThrow(() -> new ResourceNotFoundException("User", "userName", userName));

		Long currentUserId = getCurrentUserId();
	
		User result = userRepository.save(user);

		URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/users/{username}")
				.buildAndExpand(result.getUserName()).toUri();

		System.out.println("saved --------------------> " + managerUsername);

		return ResponseEntity.created(location).body(new ApiResponse(true, "Manager updated"));
	}

	@GetMapping("/user/approveAssociateLogin")
	public ResponseEntity<?> approveAssociateLogin(@RequestParam(value = "userName") String userName,
			@RequestParam(value = "flag") Boolean flag) {

		System.out.println("userName --------------------> " + flag + ":" + userName);

		User user = userRepository.findByUserName(userName)
				.orElseThrow(() -> new ResourceNotFoundException("User", "userName", userName));

		Long currentUserId = getCurrentUserId();


		user.setIsApproved(flag);
		User result = userRepository.save(user);

		URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/users/{username}")
				.buildAndExpand(result.getUserName()).toUri();

		System.out.println("saved --------------------> " + userName);

		return ResponseEntity.created(location).body(new ApiResponse(true, "Grade saved"));
	}

}