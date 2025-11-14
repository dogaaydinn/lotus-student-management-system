package com.lotus.lotusSPM.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.lotus.lotusSPM.dao.OfficialLetterDao;
import com.lotus.lotusSPM.dao.StudentDao;
import com.lotus.lotusSPM.model.ApplicationForm;
import com.lotus.lotusSPM.model.OfficialLetter;
import com.lotus.lotusSPM.model.Student;
import com.lotus.lotusSPM.service.base.OfficialLetterService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class OfficialLetterServiceImpl implements OfficialLetterService {

	/**
	 * Validate that a string is safe to use as a single file component.
	 * Returns true if the string contains no path separators or "..".
	 * Adapt as needed for further restrictions.
	 */
	private static boolean isValidFileComponent(String value) {
		if (value == null || value.isEmpty()) return false;
		return !value.contains("..") && !value.contains("/") && !value.contains("\\");
	}

	@Value("${app.pdf.output.path:${java.io.tmpdir}/lotus-pdfs}")
	private String pdfOutputPath;

	@Autowired
	private OfficialLetterDao officialLetterDao;
	@Autowired
	private StudentDao studentDao;

	@Override
	public OfficialLetter createOfficialLetter(OfficialLetter ol) {

		OfficialLetter officialLetter = new OfficialLetter();
		Student student = studentDao.findByUsername(ol.getUsername());

		// Validate username for safe usage in file name
		if (!isValidFileComponent(ol.getUsername())) {
			log.error("Invalid username used as file component: {}", ol.getUsername());
			throw new IllegalArgumentException("Invalid username for official letter file.");
		}

		Document document = new Document();
		try {
			// Create output directory if it doesn't exist
			Path outputDir = Paths.get(pdfOutputPath);
			if (!Files.exists(outputDir)) {
				Files.createDirectories(outputDir);
			}

			String filename = String.format("OfficialLetter_%s_%d.pdf",
					ol.getUsername(), System.currentTimeMillis());
			Path filePath = outputDir.resolve(filename);

			OutputStream outputStream = new FileOutputStream(filePath.toFile());

			PdfWriter.getInstance(document, outputStream);

			document.open();

			document.add(new Paragraph(
					"                                                                                                                 "
							+ ol.getDate()));
			document.add(new Paragraph("      "));
			document.add(new Paragraph("To " + ol.getComName()));
			document.add(new Paragraph("      "));
			document.add(new Paragraph(student.getName() + "  " + student.getSurname()
					+ " who has applied to your department for a summer internship, is studying at Üsküdar University, Faculty of Engineering and Natural Sciences, Software Engineering Department. In the Software Engineering department, there are two compulsory internships, one at the end of the second year and the other at the end of third year. The duration of each compulsory internship is 20 working days. Work Accident and Occupational Diseases Insurance Premiums between the dates of internship of the student are covered by our University. The named student has  "
					+ student.getInternshipStatus()
					+ " compulsory internships. This document has been prepared to inform your institution.  "));
			document.add(new Paragraph("      "));
			document.add(new Paragraph(
					"                                                       Software Engineering Internship Committee Member "));
			document.add(new Paragraph(
					"                                                      Dr. John Smith (name of the department internship coordinator) "));
//******************* coordinator nerede tutulsun tablolar bağlanmalı düzenlenmeli
			document.close();
			outputStream.close();

			log.info("PDF created successfully for user: {} at path: {}", ol.getUsername(), filePath);
		} catch (Exception e) {
			log.error("Failed to create PDF for user: {}. Error: {}", ol.getUsername(), e.getMessage(), e);
			throw new RuntimeException("Failed to create official letter PDF", e);
		}

		officialLetter.setId(ol.getId());
		officialLetter.setPdf(documentToByte(document));
		officialLetter.setComName(ol.getComName());
		officialLetter.setDate(ol.getDate());
		return officialLetterDao.save(ol);
	}

	@Override
	public OfficialLetter store(MultipartFile file) throws IOException {
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		OfficialLetter FileDB = new OfficialLetter(file.getBytes(),fileName, file.getContentType());
		return officialLetterDao.save(FileDB);
	}

	@Override
	public List<OfficialLetter> getOfficialLetter() {
		return officialLetterDao.findAll();
	}

	
	

	public byte[] documentToByte(Document doc) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(doc);
			return bos.toByteArray();
		} catch (Exception e) {
			log.error("Failed to convert document to byte array. Error: {}", e.getMessage(), e);
			return null;
		}


	}

	@Override
	public OfficialLetter findById(Long id) {
		return officialLetterDao.findById(id)
				.orElseThrow(() -> new RuntimeException("Official letter not found with id: " + id));
	}

	@Override
	public OfficialLetter findByUsername(String username) {
		return officialLetterDao.findByUsername(username);
	}

	

}
