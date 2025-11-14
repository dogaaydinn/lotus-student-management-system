package com.lotus.lotusSPM.service;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.lotus.lotusSPM.dao.OpportunitiesDao;
import com.lotus.lotusSPM.model.Opportunities;
import com.lotus.lotusSPM.service.base.OpportunitiesService;

@Service
@Transactional(rollbackFor = Exception.class)
public class OpportunitiesServiceImpl implements OpportunitiesService {

	private OpportunitiesDao opportunitiesDao;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	public void setOpportunitiesDao(OpportunitiesDao opportunitiesDao) {
		this.opportunitiesDao = opportunitiesDao;
	}

	

	@Override
	public List<Opportunities> getOpportunities() {
		return opportunitiesDao.findAll();
	}

	

	@Override
	public Opportunities findByUsername(String username) {
		return opportunitiesDao.findByUsername(username);
	}

	public String random() {

		// create a string of uppercase and lowercase characters and numbers
		String upperAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String lowerAlphabet = "abcdefghijklmnopqrstuvwxyz";
		String numbers = "0123456789";

		String alphaNumeric = upperAlphabet + lowerAlphabet + numbers;

		StringBuilder sb = new StringBuilder();

		Random random = new Random();

		int length = 10;

		for (int i = 0; i < length; i++) {

			int index = random.nextInt(alphaNumeric.length());

			char randomChar = alphaNumeric.charAt(index);

			sb.append(randomChar);
		}

		String randomString = sb.toString();
		return randomString;
	}

	@Override
	public Opportunities findById(Long id) {
		return opportunitiesDao.findById(id)
				.orElseThrow(() -> new RuntimeException("Opportunity not found with id: " + id));
	}



	@Override
	public Opportunities createOpportunities(Opportunities opportunities) {
		return opportunitiesDao.save(opportunities);
	}

}
