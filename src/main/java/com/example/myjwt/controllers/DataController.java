package com.example.myjwt.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.http.HttpHeaders;

import com.example.myjwt.beans.AsgnmtAssociate;
import com.example.myjwt.beans.BillablePlanData;
import com.example.myjwt.beans.BillableReport;
import com.example.myjwt.beans.Mapping;
import com.example.myjwt.exception.BadRequestException;
import com.example.myjwt.exception.ResourceNotFoundException;

import com.example.myjwt.models.AssignmentReport;
import com.example.myjwt.models.AssignmentUser;
import com.example.myjwt.models.BillablePlan;
import com.example.myjwt.models.Category;
import com.example.myjwt.models.EvaluationResult;
import com.example.myjwt.models.EvaluationResultCategory;
import com.example.myjwt.models.Grade;
import com.example.myjwt.models.Profile;
import com.example.myjwt.models.Role;
import com.example.myjwt.models.User;
import com.example.myjwt.models.enm.EGrade;
import com.example.myjwt.models.enm.ERole;
import com.example.myjwt.payload.IdentityAvailability;
import com.example.myjwt.payload.PyramidItem;
import com.example.myjwt.payload.request.CreateAccountRequest;
import com.example.myjwt.payload.request.CreateSbuRequest;
import com.example.myjwt.payload.response.ApiResponse;
import com.example.myjwt.payload.response.BillablePlanHistory;
import com.example.myjwt.repo.AssignmentReportRepository;
import com.example.myjwt.repo.AssignmentUserRepository;
import com.example.myjwt.repo.BillablePlanRepository;
import com.example.myjwt.repo.CategoryRepository;
import com.example.myjwt.repo.EvaluationResultCategoryRepository;
import com.example.myjwt.repo.EvaluationResultRepository;
import com.example.myjwt.repo.ProfileRepository;
import com.example.myjwt.repo.UserRepository;
import com.example.myjwt.security.services.RoleService;
import com.example.myjwt.util.AppConstants;
import com.example.myjwt.util.PMUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class DataController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(DataController.class);

	@Autowired
	private AssignmentReportRepository assignmentReportRepository;

	@Autowired
	private ProfileRepository profileRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	EvaluationResultRepository evaluationResultRepository;

	@Autowired
	EvaluationResultCategoryRepository evaluationResultCategoryRepository;

	@Autowired
	private AssignmentUserRepository assignmentUserRepository;

	@Autowired
	private BillablePlanRepository billablePlanRepository;

	@GetMapping("/data/getAssignmentReports")
	public List<AssignmentReport> getAssignmentReports() {

		Long currentUserId = getCurrentUserId();

		List<AssignmentReport> asgnmtAssociateList = assignmentReportRepository.findAll();
		Collections.sort(asgnmtAssociateList, AssignmentReport.AssignmentReportComparator);

		System.out.println(asgnmtAssociateList);

		return asgnmtAssociateList;
	}

	@GetMapping("/data/getAssignmentReport")
	public List<AssignmentUser> getAssignmentReport(@RequestParam(value = "reportID") int reportID,
			@RequestParam(value = "paramId") String paramId) {

		Long currentUserId = getCurrentUserId();

		System.out.println("paramId=========================" + paramId);

		Long pid = Long.parseLong(paramId);

		AssignmentReport assignmentReport = assignmentReportRepository.findById(pid)
				.orElseThrow(() -> new UsernameNotFoundException("Assignment report not found"));

		List<AssignmentUser> asgnmtAssociateList = assignmentUserRepository.findByAssignmentReport(assignmentReport);

		System.out.println("asgnmtAssociateList size=========================" + asgnmtAssociateList.size());

		return asgnmtAssociateList;
	}

	@GetMapping("/data/getResume")
	public ResponseEntity<byte[]> getResume(@RequestParam(value = "resumeId") String resumeId) {
		Profile profile = profileRepository.findById(Long.parseLong(resumeId))
				.orElseThrow(() -> new UsernameNotFoundException("Resume not found"));

		String fileName = "resume.pdf";

		if (profile.getIsInternal())
			fileName = profile.getAssociateId() + "";
		else
			fileName = profile.getCandidateId() + "";

		HttpHeaders header = new HttpHeaders();

		// header.setContentType(MediaType.valueOf(profile.getData().getContentType()));
		header.setContentLength(profile.getData().length);
		header.set("Content-Disposition", "attachment; filename=" + fileName);

		return new ResponseEntity<>(profile.getData(), header, HttpStatus.OK);
	}

	@GetMapping("/data/getAllProfilesFromServer")
	public List<Profile> getAllProfilesFromServer() {

		Long currentUserId = getCurrentUserId();

		List<Profile> allProfileList = profileRepository.findAllByOrderByIdDesc();

		for (Profile proflile : allProfileList) {
			System.out.println("proflile.skill() = " + proflile.getSkill().getSkillName());
			System.out.println("proflile.getFeedbacks().size() = " + proflile.getFeedbacks().size());
		}

		return allProfileList;
	}

	@GetMapping("/data/getAllEvaluationResults")
	public List<EvaluationResult> getAllEvaluationResults() {

		List<EvaluationResult> allEvaluationResultList = evaluationResultRepository.findAll();

		return allEvaluationResultList;
	}

	@GetMapping("/data/getAllEvaluationResultCategory")
	public List<EvaluationResultCategory> getAllEvaluationResultCategory() {

		List<EvaluationResultCategory> allEvaluationResultCategoryList = evaluationResultCategoryRepository.findAll();

		return allEvaluationResultCategoryList;
	}

	@GetMapping("/data/getProfileInfo")
	public Profile getProfileInfo(@RequestParam(value = "profileId") String profileId) {

		System.out.println("proflile.getProfileInfo() = ");

		Profile profile = profileRepository.findById(Long.parseLong(profileId))
				.orElseThrow(() -> new UsernameNotFoundException("Profile not found"));

		if (profile.getFeedbacks() != null)
			System.out.println("---->" + profile.getFeedbacks().size());
		else
			System.out.println("----> profile.getFeedbacks()" + profile.getFeedbacks());

		return profile;
	}

	@GetMapping("/data/filteredbillableplans")
	public List<BillablePlanData> apiGetFilteredBillablePlans(@RequestParam(value = "selPractice") String selPractice,
			@RequestParam(value = "categoryId") Long categoryId, @RequestParam(value = "grade") String grade,
			@RequestParam(value = "location") String location) {

		System.out.println("I am here: apiGetFilteredBillablePlans:" + selPractice + ":" + categoryId + ":" + grade
				+ ":" + location);
		List<BillablePlanData> billablePlanDataList = new ArrayList<BillablePlanData>();
		List<String> grades = Mapping.getOriginalGradesFromMapped(grade);
		HashMap<Long, List<BillablePlan>> mapBillableAllPlans = new HashMap<Long, List<BillablePlan>>();

		Category noPlanCategory = categoryRepository.findByCatGroupAndGroupKeyAndGroupValue(
				AppConstants.CATEGORY_BILLABILITY, AppConstants.CATEGORY_BILLABILITY, AppConstants.NO_BILLABILITY_PLAN);

		AssignmentReport report = assignmentReportRepository.findFirstByOrderByIdDesc()
				.orElseThrow(() -> new BadRequestException("No assignment report found"));

		List<AssignmentUser> allAssociates = assignmentUserRepository
				.findByAssignmentReportAndServiceLineAndOnOff(report, selPractice, location);

		System.out.println("allAssociates.size():" + allAssociates.size());

		List<BillablePlan> billableAllPlans = billablePlanRepository.findAllByOrderByIdDesc();

		for (BillablePlan billablePlan : billableAllPlans) {
			List<BillablePlan> associateBillablePlans = mapBillableAllPlans.get(billablePlan.getAssociateId());

			if (associateBillablePlans == null) {
				associateBillablePlans = new ArrayList<BillablePlan>();
			}
			associateBillablePlans.add(billablePlan);
			mapBillableAllPlans.put(billablePlan.getAssociateId(), associateBillablePlans);
		}

		System.out.println("mapBillableAllPlans.size():" + mapBillableAllPlans.size());

		for (AssignmentUser associate : allAssociates) {

			if (grades.contains(associate.getGradeDescription())) {

				System.out.println("matched for:" + associate.getAssociateName() + ":" + associate.getAssociateID());

				BillablePlanData billablePlanData = new BillablePlanData(associate);

				List<BillablePlan> associateBillablePlans = mapBillableAllPlans.get(associate.getAssociateID());

				System.out.println("associateBillablePlans:" + associateBillablePlans);

				if (associateBillablePlans == null) {
					System.out.println(
							"noPlanCategory.getId() == categoryId" + noPlanCategory.getId() + ":" + categoryId);
					if ((noPlanCategory.getId() == categoryId)) {
						associateBillablePlans = new ArrayList<BillablePlan>();

						BillablePlan billablePlan = new BillablePlan(associate.getAssociateID(), true);
						associateBillablePlans.add(billablePlan);
					}
					System.out.println("associateBillablePlans is null");
				} else {
					System.out.println("associateBillablePlans:" + associateBillablePlans.size());
				}

				System.out.println("associate.getAssociateID() = " + associate.getAssociateID());
				boolean result = billablePlanData.setBillablePlans(associateBillablePlans, categoryId,
						noPlanCategory.getId());

				if (result) {
					billablePlanDataList.add(billablePlanData);
				}

			}
		}

		return billablePlanDataList;
	}

	@GetMapping("/data/apiGetBillablePlan")
	public List<BillablePlanData> apiGetBillablePlan(@RequestParam(value = "selPractice") String selPractice,
			@RequestParam(value = "selLOB") String selLOB) {

		System.out.println("I am here: apiGetBillablePlan");

		AssignmentReport report = assignmentReportRepository.findFirstByOrderByIdDesc()
				.orElseThrow(() -> new BadRequestException("No assignment report found"));

		List<AssignmentUser> allAssociates = assignmentUserRepository.findByAssignmentReportAndServiceLineAndLOB(report,
				selPractice, selLOB);

		List<Long> activeAssociates = new ArrayList<Long>();

		List<BillablePlanData> billablePlanDataList = new ArrayList<BillablePlanData>();

		List<BillablePlan> billableAllPlans = billablePlanRepository.findAllByOrderByIdDesc();

		HashMap<Long, List<BillablePlan>> mapBillableAllPlans = new HashMap<Long, List<BillablePlan>>();

		for (BillablePlan billablePlan : billableAllPlans) {
			List<BillablePlan> associateBillablePlans = mapBillableAllPlans.get(billablePlan.getAssociateId());

			if (associateBillablePlans == null) {
				associateBillablePlans = new ArrayList<BillablePlan>();
			}
			associateBillablePlans.add(billablePlan);
			mapBillableAllPlans.put(billablePlan.getAssociateId(), associateBillablePlans);
		}

		for (AssignmentUser associate : allAssociates) {
			BillablePlanData billablePlanData = new BillablePlanData(associate);

			billablePlanDataList.add(billablePlanData);

			if (!activeAssociates.contains(associate.getAssociateID()))
				activeAssociates.add(associate.getAssociateID());

			List<BillablePlan> associateBillablePlans = mapBillableAllPlans.get(associate.getAssociateID());

			if (associateBillablePlans == null) {
				associateBillablePlans = new ArrayList<BillablePlan>();

				BillablePlan billablePlan = new BillablePlan(associate.getAssociateID(), true);
				associateBillablePlans.add(billablePlan);

			} else {
				for (BillablePlan billablePlan : associateBillablePlans) {
					if (!billablePlan.getIsActive()) {
						billablePlan.setIsActive(true);
						billablePlanRepository.save(billablePlan);
					}
				}
			}

			billablePlanData.setBillablePlans(associateBillablePlans);
		}

		// Set active false for non active associates
		List<BillablePlan> activeBillablePlans = billablePlanRepository.findByIsActive(true);
		for (BillablePlan billablePlan : activeBillablePlans) {
			if (!activeAssociates.contains(billablePlan.getAssociateId())) {
				billablePlan.setIsActive(false);
				billablePlanRepository.save(billablePlan);
			}
		}

		billablePlanRepository.flush();

		return billablePlanDataList;
	}

	@GetMapping("/data/apiGetPracticeList")
	public SortedSet<String> apiGetPracticeList() {
		SortedSet<String> practiceList = new TreeSet<String>();

		AssignmentReport report = assignmentReportRepository.findFirstByOrderByIdDesc()
				.orElseThrow(() -> new BadRequestException("No assignment report found"));

		List<AssignmentUser> allAssociates = assignmentUserRepository.findByAssignmentReport(report);

		for (AssignmentUser user : allAssociates) {
			practiceList.add(user.getServiceLine());
		}

		return practiceList;
	}

	@GetMapping("/data/apiGetLOBList")
	public SortedSet<String> apiGetLOBList() {
		SortedSet<String> lobList = new TreeSet<String>();

		AssignmentReport report = assignmentReportRepository.findFirstByOrderByIdDesc()
				.orElseThrow(() -> new BadRequestException("No assignment report found"));

		List<AssignmentUser> allAssociates = assignmentUserRepository.findByAssignmentReport(report);

		for (AssignmentUser user : allAssociates) {
			lobList.add(user.getlOB());
		}

		return lobList;
	}

	@GetMapping("/data/apiGetBillableCategories")
	public List<Category> apiGetBillableCategories() {

		List<Category> billableCategories = categoryRepository.findByCatGroup("BillabilityPlan");

		System.out.println("billableCategories size" + billableCategories.size());

		return billableCategories;
	}

	@GetMapping("/data/apiGetBillablePlanHistory")
	public List<BillablePlanHistory> apiGetBillablePlanHistory(@RequestParam(value = "associateId") Long associateId) {

		List<BillablePlanHistory> historyOfPlans = new ArrayList<BillablePlanHistory>();
		List<BillablePlan> associatePlans = billablePlanRepository.findByAssociateId(associateId);

		for (BillablePlan plan : associatePlans) {
			BillablePlanHistory planHistory = new BillablePlanHistory(plan);

			historyOfPlans.add(planHistory);
		}
		return historyOfPlans;
	}

}