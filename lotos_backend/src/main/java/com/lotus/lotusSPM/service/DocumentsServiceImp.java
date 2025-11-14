package com.lotus.lotusSPM.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.lotus.lotusSPM.dao.DocumentsDao;
import com.lotus.lotusSPM.model.Documents;
import com.lotus.lotusSPM.service.base.DocumentsService;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocumentsServiceImp implements DocumentsService {

	@Autowired
	private DocumentsDao documentsDao;

	@Override
	public Documents store(MultipartFile file) throws IOException {
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		Documents FileDB = new Documents(fileName, file.getContentType(), file.getBytes());
		return documentsDao.save(FileDB);
	}

	@Override
	public Documents getFile(String id) {
		return documentsDao.findById(id)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
	}

	@Override
	public Stream<Documents> getAllFiles() {
		return documentsDao.findAll().stream();
	}

	@Override
	public List<Documents> getDocuments() {
		return documentsDao.findAll();
	}

	@Override
	public void deleteDocument(String id) {
		documentsDao.deleteById(id);
	}

}
