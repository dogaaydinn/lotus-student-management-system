package com.lotus.lotusSPM.web;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.lotus.lotusSPM.model.Student;
import com.lotus.lotusSPM.service.StudentServiceImpl;
import com.lotus.lotusSPM.service.base.StudentService;

import lombok.extern.slf4j.Slf4j;

@RestController
//@RequestMapping("/student")
@Slf4j
public class StudentController {

	@Autowired
	private StudentServiceImpl studentServiceImpl;

	@Autowired
	private StudentService studentService;

	@GetMapping("/student/{username}")
	public ResponseEntity<Object> getStudent(@PathVariable("username") String username) {
		try {
			Student student = studentService.findByUsername(username);
			return ResponseEntity.ok(student);
		} catch (Exception ex) {
			log.error("Student not found with username: {}", username, ex);
			return ResponseEntity.notFound().build();
		}

	}

	@GetMapping("/students")
	public ResponseEntity<Object> getStudent() {
		log.warn("Legacy endpoint /students called without pagination");
		List<Student> stocks = studentService.findStudents();
		return ResponseEntity.ok(stocks);
	}

	/**
	 * Get students with pagination (recommended endpoint)
	 * @param page Page number (0-indexed, default: 0)
	 * @param size Page size (default: 20, max: 100)
	 * @param sortBy Field to sort by (default: id)
	 * @param sortDir Sort direction (asc/desc, default: asc)
	 * @return Paginated list of students
	 */
	@GetMapping("/api/v1/students")
	public ResponseEntity<Page<Student>> getStudentsPaginated(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "id") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDir) {

		try {
			// Limit max page size to prevent abuse
			size = Math.min(size, 100);

			Sort sort = sortDir.equalsIgnoreCase("desc")
					? Sort.by(sortBy).descending()
					: Sort.by(sortBy).ascending();

			Pageable pageable = PageRequest.of(page, size, sort);

			log.info("Fetching students - page: {}, size: {}, sortBy: {}, sortDir: {}",
					page, size, sortBy, sortDir);

			Page<Student> students = studentServiceImpl.findStudents(pageable);

			return ResponseEntity.ok(students);
		} catch (Exception ex) {
			log.error("Error fetching paginated students: {}", ex.getMessage(), ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DeleteMapping("/student/{id}")
	public ResponseEntity<?> deleteStudent(@PathVariable("id") Long id) {
		try {
			studentService.deleteStudent(id);
			log.info("Student deleted successfully: {}", id);
			return ResponseEntity.ok().build();
		} catch (Exception ex) {
			log.error("Failed to delete student with id: {}", id, ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping("/student")
	public ResponseEntity<URI> createStudent(@RequestBody Student student) {
		try {
			studentService.createStudent(student);
			Long id = student.getId();
			URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
			log.info("Student created successfully with id: {}", id);
			return ResponseEntity.created(location).build();
		} catch (Exception ex) {
			log.error("Failed to create student: {}", ex.getMessage(), ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping("/student/login")
	public ResponseEntity<Object> login(@RequestBody Student student) {
		try {
			Student login = studentService.findByUsername(student.getUsername());
			if (login == null || !login.getPassword().equals(student.getPassword())) {
				log.warn("Failed login attempt for username: {}", student.getUsername());
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}
			log.info("Successful login for username: {}", student.getUsername());
			return ResponseEntity.ok().build();
		} catch (Exception ex) {
			log.error("Login error for username: {}", student.getUsername(), ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
