package com.lotus.lotusSPM.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.lotus.lotusSPM.dao.StudentDao;
import com.lotus.lotusSPM.model.Student;
import com.lotus.lotusSPM.service.base.StudentService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class StudentServiceImpl implements StudentService {

	private StudentDao studentDao;

	@Autowired
	public void setStudentDao(StudentDao studentDao) {
		this.studentDao = studentDao;
	}


	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	@Cacheable(value = "students", key = "#stu_id")
	public Student findByUsername(String stu_id) {
		log.debug("Fetching student from database: {}", stu_id);
		return studentDao.findByUsername(stu_id);

	}


	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Student> findStudents() {
		log.warn("findStudents() called without pagination - this may cause performance issues with large datasets");
		return studentDao.findAll();
	}

	/**
	 * Find students with pagination support
	 * @param pageable Pagination and sorting parameters
	 * @return Page of students
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Page<Student> findStudents(Pageable pageable) {
		log.debug("Finding students with pagination: page={}, size={}",
				pageable.getPageNumber(), pageable.getPageSize());
		return studentDao.findAll(pageable);
	}


	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	@Cacheable(value = "students", key = "'id:' + #id")
	public Student findStdById(Long id) {
		log.debug("Fetching student from database with id: {}", id);
		return studentDao.findById(id)
				.orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
	}


	@Override
	@CacheEvict(value = "students", allEntries = true)
	public Student createStudent(Student student) {
		log.info("Creating new student, clearing cache");
		return studentDao.save(student);
	}


	@Override
	@Caching(evict = {
			@CacheEvict(value = "students", key = "'id:' + #id"),
			@CacheEvict(value = "students", allEntries = true)
	})
	public void deleteStudent(Long id) {
		log.info("Deleting student with id: {}, clearing cache", id);
		studentDao.deleteById(id);
	}


	


	

	

}
