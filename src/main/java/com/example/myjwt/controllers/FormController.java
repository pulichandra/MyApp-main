package com.example.myjwt.controllers;

import java.net.URI;
import java.sql.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.myjwt.models.BillablePlan;
import com.example.myjwt.models.Category;
import com.example.myjwt.models.EvaluationResult;
import com.example.myjwt.models.EvaluationResultCategory;
import com.example.myjwt.models.Feedback;
import com.example.myjwt.models.Profile;
import com.example.myjwt.models.Skill;
import com.example.myjwt.payload.request.BillabilityPlanRequest;
import com.example.myjwt.payload.request.CreateProfileRequest;
import com.example.myjwt.payload.request.ProfileFeedbackRequest;
import com.example.myjwt.payload.response.ApiResponse;
import com.example.myjwt.payload.response.FileUploadResponse;
import com.example.myjwt.repo.BillablePlanRepository;
import com.example.myjwt.repo.CategoryRepository;
import com.example.myjwt.repo.EvaluationResultCategoryRepository;
import com.example.myjwt.repo.EvaluationResultRepository;
import com.example.myjwt.repo.ProfileFeedbackRepository;
import com.example.myjwt.repo.ProfileRepository;
import com.example.myjwt.repo.SkillRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class FormController {
	@Autowired
	SkillRepository skillRepository;

	@Autowired
	ProfileRepository profileRepository;

	@Autowired
	EvaluationResultRepository evaluationResultRepository;

	@Autowired
	EvaluationResultCategoryRepository evaluationResultCategoryRepository;

	@Autowired
	ProfileFeedbackRepository profileFeedbackRepository;

	@Autowired
	BillablePlanRepository billablePlanRepository;

	@Autowired
	CategoryRepository categoryRepository;

	@PostMapping("/forms/createProfileRequest")
	public ResponseEntity<FileUploadResponse> createProfileRequest(@RequestParam("file") MultipartFile[] files,
			@RequestParam("createProfileRequest") String createProfileRequestStr) throws Exception {

		CreateProfileRequest createProfileRequest = null;

		System.out.println("createProfileRequestStr = " + createProfileRequestStr);

		try {
			createProfileRequest = new ObjectMapper().readValue(createProfileRequestStr.toString(),
					CreateProfileRequest.class);

			byte[] fileBytes = null;

			for (int i = 0; i < files.length; i++) {
				fileBytes = files[i].getBytes();
				break;
			}

			Profile profile = null;

			if (createProfileRequest.getIsInternal()) {
				profile = profileRepository.findByAssociateId(createProfileRequest.getAssociateId());
			} else {
				profile = profileRepository.findByCandidateId(createProfileRequest.getCandidateId());
			}

			if (profile == null) {
				profile = new Profile();
			} else if (!createProfileRequest.getUpdateIfAlreadyExists()) {
				return ResponseEntity.status(HttpStatus.OK).body(new FileUploadResponse("Profile already exists ! "));
			}
			profile.setAssociateId(createProfileRequest.getAssociateId());
			profile.setCandidateId(createProfileRequest.getCandidateId());
			profile.setCity(createProfileRequest.getCity());
			profile.setData(fileBytes);
			profile.setEmail(createProfileRequest.getEmail());
			profile.setEntryDate(new Date(new java.util.Date().getTime()));
			profile.setFullName(createProfileRequest.getFullName());

			System.out.println("createProfileRequest.getIsOnsite() = " + createProfileRequest.getIsOnsite());
			profile.setIsOnsite(createProfileRequest.getIsOnsite());
			profile.setPhone(createProfileRequest.getPhone());
			profile.setIsInternal(createProfileRequest.getIsInternal());

			Skill skill = skillRepository.findById(createProfileRequest.getSkillId())
					.orElseThrow(() -> new UsernameNotFoundException("Skill not found"));

			profile.setSkill(skill);

			profileRepository.save(profile);

			return ResponseEntity.status(HttpStatus.OK).body(new FileUploadResponse("Files uploaded successfully: "));

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
					.body(new FileUploadResponse("Exception to upload files!"));
		}
	}

	@PostMapping("/forms/addProfilefeedback")
	public ResponseEntity<?> addProfilefeedback(@Valid @RequestBody ProfileFeedbackRequest profileFeedbackRequest,
			HttpServletRequest request) throws Exception {

		System.out.println("profileFeedbackRequest.getProfileId()=" + profileFeedbackRequest.getProfileId());
		Profile profile = profileRepository.findById(profileFeedbackRequest.getProfileId())
				.orElseThrow(() -> new UsernameNotFoundException("Profile not found"));

		System.out.println("profile id" + profile.getId());

		Feedback feedback = new Feedback();
		try {

			feedback.setComments(profileFeedbackRequest.getDetailedFeedback());
			feedback.setEvaluationDate(profileFeedbackRequest.getEvaluationDate());
			feedback.setPanelName(profileFeedbackRequest.getPanelName());
			feedback.setProfile(profile);

			EvaluationResult evaluationResult = evaluationResultRepository
					.findById(profileFeedbackRequest.getEvaluationResult())
					.orElseThrow(() -> new UsernameNotFoundException("Evaluation Result not found"));
			feedback.setResult(evaluationResult);

			if (profileFeedbackRequest.getEvaluationResultCategory() != null
					&& profileFeedbackRequest.getEvaluationResultCategory() > 0) {
				EvaluationResultCategory evaluationResultCategory = evaluationResultCategoryRepository
						.findById(profileFeedbackRequest.getEvaluationResultCategory())
						.orElseThrow(() -> new UsernameNotFoundException("Evaluation Result Category not found"));
				feedback.setResultCategory(evaluationResultCategory);
			}

			profileFeedbackRepository.save(feedback);

			URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
					.path("/api/sbu/{profileFeedbackRequest.getId()}").buildAndExpand(feedback.getId()).toUri();

			return ResponseEntity.created(location).body(new ApiResponse(true, "Feedback added successfully!!"));

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
					.body(new FileUploadResponse("Exception to upload files!"));
		}
	}

	@PostMapping("/forms/addbillabilityplan")
	public ResponseEntity<?> addBillabilityPlan(@Valid @RequestBody BillabilityPlanRequest objBillabilityPlan,
			HttpServletRequest request) throws Exception {

		System.out.println("objBillabilityPlan.getAssociateId() =" + objBillabilityPlan.getAssociateId());

		try {

			//BillablePlan billablePlan = billablePlanRepository
				//	.findFirstByAssociateIdByOrderByIdDesc(objBillabilityPlan.getAssociateId());

			BillablePlan billablePlan = new BillablePlan();

			Category category = categoryRepository.findById(objBillabilityPlan.getBillabilityCategory())
					.orElseThrow(() -> new UsernameNotFoundException("Billable Category doesn't exist"));

			billablePlan.setBillableCategory(category);
			billablePlan.setAssociateId(objBillabilityPlan.getAssociateId());
			billablePlan.setRemarks(objBillabilityPlan.getRemarks());
			billablePlan.setOwner(objBillabilityPlan.getOwner());
			billablePlan.setEta(objBillabilityPlan.getEtaDate());
			billablePlan.setIsActive(true);
			billablePlanRepository.save(billablePlan);

			URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
					.path("/api/sbu/{profileFeedbackRequest.getId()}")
					.buildAndExpand(objBillabilityPlan.getAssociateId()).toUri();

			return ResponseEntity.created(location)
					.body(new ApiResponse(true, "Billability plan added successfully!!"));

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
					.body(new FileUploadResponse("Exception to upload files!"));
		}
	}

	@GetMapping("/forms/getAllSkillFamilies")
	public List<Skill> getAllSkillFamilies() {

		List<Skill> skillFamilies = skillRepository.findAll();

		return skillFamilies;
	}

}
