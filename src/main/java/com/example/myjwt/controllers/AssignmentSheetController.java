package com.example.myjwt.controllers;

import com.example.myjwt.beans.AsgnmtAssociate;
import com.example.myjwt.models.AssignmentReport;
import com.example.myjwt.models.AssignmentUser;
import com.example.myjwt.payload.response.FileUploadResponse;
import com.example.myjwt.repo.UserRepository;
import com.example.myjwt.util.EmailUtil;
import com.example.myjwt.util.FileUtil;
import com.example.myjwt.util.PMUtils;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.example.myjwt.repo.AssignmentReportRepository;
import com.example.myjwt.repo.AssignmentUserRepository;

import javax.transaction.Transactional;
import javax.xml.XMLConstants;

@Controller
//@CrossOrigin(origins = "http://localhost:8082") open for specific port
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class AssignmentSheetController {

	public final int BATCH_SIZE = 500;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	AssignmentUserRepository assignmentUserRepository;
	
	@Autowired
	AssignmentReportRepository assignmentReportRepository;

	/**
	 * Method to upload multiple files
	 * 
	 * @param files
	 * @return FileResponse
	 */
	@PostMapping("/assignmentsheet")
	public ResponseEntity<FileUploadResponse> uploadFiles(@RequestParam("file") MultipartFile[] files) {
		
		System.out.println("files[0].getOriginalFilename():"+files[0].getOriginalFilename());

		try {
			List<String> fileNames = new ArrayList<>();
			byte[] bytes = new byte[0];
			try {
				bytes = files[0].getBytes();
				
				AssignmentReport report = new AssignmentReport();
				
				System.out.println("files[0].getOriginalFilename():"+files[0].getOriginalFilename());
				report.setFilename(files[0].getOriginalFilename());
				assignmentReportRepository.save(report);
				
				readAssignmentXML(new String(bytes), report);
				
				
				
				System.out.println("bytes.lenht -->" + bytes.length);
				fileNames.add(files[0].getOriginalFilename());
			} catch (IOException e) {
				e.printStackTrace();
			}

			return ResponseEntity.status(HttpStatus.OK)
					.body(new FileUploadResponse("Files uploaded successfully: " + fileNames));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
					.body(new FileUploadResponse("Exception to upload files!"));
		}
	}

	/**
	 * Create directory to save files, if not exist
	 */
	private void createDirIfNotExist() {
		// create directory to save the files
		File directory = new File(FileUtil.folderPath);
		if (!directory.exists()) {
			directory.mkdir();
		}
	}

	/**
	 * Method to get the list of files from the file storage folder.
	 * 
	 * @return file
	 */
	@GetMapping("/filesa")
	public ResponseEntity<String[]> getListFiles() {
		return ResponseEntity.status(HttpStatus.OK).body(new File(FileUtil.folderPath).list());
	}

	private void readAssignmentXML(String content,AssignmentReport report) {
		try {

			SAXBuilder sax = new SAXBuilder();

			content = content.replaceAll("&nbsp;", "");
			content = content.replaceAll("&", "&amp;");

			sax.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			sax.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

			// XML is a local file
			InputStream stream = new ByteArrayInputStream(content.getBytes("UTF-8"));
			Document doc = sax.build(stream);

			Element rootNode = doc.getRootElement();
			List<Element> trList = rootNode.getChildren("tr");

			List<AsgnmtAssociate> asgnmtEndingList = new ArrayList<AsgnmtAssociate>();
			HashMap<Long, AsgnmtAssociate> projectsEndingList = new HashMap<Long, AsgnmtAssociate>();
			List<AssignmentUser> assignmentUserList = new ArrayList<AssignmentUser>();

			for (int rowNum = 1; rowNum < trList.size(); rowNum++) {

				AsgnmtAssociate asgnmtAssociate = new AsgnmtAssociate();

				AssignmentUser assignmentUser = new AssignmentUser();
				assignmentUser.setAssignmentReport(report);

				List<Element> tdList = trList.get(rowNum).getChildren();
				int index = 0;
				for (Element cell : tdList) {
					asgnmtAssociate.setValue(index, cell.getValue());
					assignmentUser.setValue(index, cell.getValue());
					assignmentUserList.add(assignmentUser);
					index++;
				}

				if (PMUtils.differenceBetweenTwoDates(new Date(), asgnmtAssociate.getAssignmentEnddate()) < 60)
					asgnmtEndingList.add(asgnmtAssociate);
				if (PMUtils.differenceBetweenTwoDates(new Date(), asgnmtAssociate.getProjectEndDate()) < 60)
					projectsEndingList.put(asgnmtAssociate.getProjectID(), asgnmtAssociate);
				// break;
			}

			insertAssignmentUsersInDB(assignmentUserList);

			System.out.println("Projects ending in next 60 days   ==== " + projectsEndingList.size());
			System.out.println("Assignments ending in next 60 days   ==== " + asgnmtEndingList.size());

			try {
				EmailUtil util = new EmailUtil();
				util.sendEmailAssignmentsAndProjectsEnding(mailSender, asgnmtEndingList, projectsEndingList);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		} catch (IOException | JDOMException e) {
			e.printStackTrace();
		}
	}

	@Transactional
	public void insertAssignmentUsersInDB(List<AssignmentUser> assignmentUserList) {
		assignmentUserRepository.saveAll(assignmentUserList);
	}

}
