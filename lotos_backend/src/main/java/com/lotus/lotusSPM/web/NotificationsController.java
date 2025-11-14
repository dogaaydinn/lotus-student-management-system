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
import com.lotus.lotusSPM.model.Notifications;
import com.lotus.lotusSPM.service.base.NotificationsService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class NotificationsController {

	@Autowired
	private NotificationsService notificationsService;

	@DeleteMapping("/notification/{id}")
	public ResponseEntity<?> deleteNotification(@PathVariable("id") Long id) {
		try {
			notificationsService.deleteNotifications(id);
			log.info("Notification deleted successfully: {}", id);
			return ResponseEntity.ok().build();
		} catch (Exception ex) {
			log.error("Failed to delete notification with id: {}. Error: {}", id, ex.getMessage(), ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

	}
	
	@GetMapping("/notifications")
	public ResponseEntity<Object> getNotifications() {
		List<Notifications> notifications = notificationsService.getNotifications();
		return ResponseEntity.ok(notifications);
	}
	
	
	@PostMapping("/notification")
	public ResponseEntity<URI> createNotification(@RequestBody Notifications notifications) {
		try {
			notificationsService.createNotifications(notifications);
			Long id = notifications.getId();
			URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
			return ResponseEntity.created(location).build();
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
}


}