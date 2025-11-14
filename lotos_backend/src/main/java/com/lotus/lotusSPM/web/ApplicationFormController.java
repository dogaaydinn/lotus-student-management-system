package com.lotus.lotusSPM.web;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.lotus.lotusSPM.message.ResponseFile;
import com.lotus.lotusSPM.message.ResponseMessage;
import com.lotus.lotusSPM.model.ApplicationForm;
import com.lotus.lotusSPM.model.Student;
import com.lotus.lotusSPM.service.base.ApplicationFormService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class ApplicationFormController {
	
	@Autowired
	private ApplicationFormService applicationFormService;

	@DeleteMapping("/form/{id}")
	public ResponseEntity<?> deleteForm(@PathVariable("id") String id) {
		try {
			applicationFormService.deleteForm(id);
			log.info("Application form deleted successfully: {}", id);
			return ResponseEntity.ok().build();
		} catch (Exception ex) {
			log.error("Failed to delete application form with id: {}. Error: {}", id, ex.getMessage(), ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@PostMapping("/uploadForm")
	  public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("form") MultipartFile file,Student student) {
	    String message = "";
	    try {
	    	applicationFormService.store(file,student);

	      message = "Uploaded the file successfully: " + file.getOriginalFilename();
	      return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
	    } catch (Exception e) {
	      message = "Could not upload the file: " + file.getOriginalFilename() + "!";
	      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
	    }
	  }
	
	@GetMapping("/forms")
	  public ResponseEntity<List<ResponseFile>> getListFiles() {
	    List<ResponseFile> files = applicationFormService.getAllForms().map(dbFile -> {
	      String fileDownloadUri = ServletUriComponentsBuilder
	          .fromCurrentContextPath()
	          .path("/forms/")
	          .path(dbFile.getId().toString())
	          .toUriString();

	      return new ResponseFile(
	          dbFile.getName(),
	          fileDownloadUri,
	          dbFile.getType(),
	          dbFile.getData().length);
	    }).collect(Collectors.toList());

	    return ResponseEntity.status(HttpStatus.OK).body(files);
	  }
	
	//??gereksiz
	@GetMapping("/forms/{id}")
	  public ResponseEntity<byte[]> getFile(@PathVariable String id) {
	    ApplicationForm fileDB = applicationFormService.getForm(id);

	    return ResponseEntity.ok()
	        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDB.getName() + "\"")
	        .body(fileDB.getData());
	  }
}
