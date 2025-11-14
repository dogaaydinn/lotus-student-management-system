package com.lotus.lotusSPM.web;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.lotus.lotusSPM.model.Admin;
import com.lotus.lotusSPM.model.Messages;
import com.lotus.lotusSPM.model.Notifications;
import com.lotus.lotusSPM.service.base.MessagesService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class MessasgesController {

	@Autowired
	private MessagesService messagesService;


	@GetMapping("/message/{id}")
	public ResponseEntity<Object> getMessage(@PathVariable("id") Long id) {

			Messages msg = messagesService.findById(id);
			return ResponseEntity.ok(msg);
	
	} 
	@DeleteMapping("/messages/{id}")
	public ResponseEntity<?> deleteMessage(@PathVariable("id") Long id) {
		try {
			messagesService.deleteMessage(id);
			log.info("Message deleted successfully: {}", id);
			return ResponseEntity.ok().build();
		} catch (Exception ex) {
			log.error("Failed to delete message with id: {}. Error: {}", id, ex.getMessage(), ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

	}

	@GetMapping("/messages")
	public ResponseEntity<Object> getMessages() {
		List<Messages> msg = messagesService.getMessages();
		return ResponseEntity.ok(msg);
	}

	@PostMapping("/message")
	public ResponseEntity<URI> createMessages(@RequestBody Messages messages) {
		try {
			messagesService.createMessage(messages);
			Long id = messages.getId();
			URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
			return ResponseEntity.created(location).build();
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GetMapping("/messageTo/{to}")
	public ResponseEntity<Object> getMsgByTo(@PathVariable("to") String to) {

		List<Messages> msg = messagesService.findByTo(to);
		return ResponseEntity.ok(msg);

	}
	
	@GetMapping("/messageFrom/{from}")
	public ResponseEntity<Object> getMsgByFrom(@PathVariable("from") String from) {

		List<Messages> msg = messagesService.findByFrom(from);
		return ResponseEntity.ok(msg);

	}

}
