package com.lotus.lotusSPM.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lotus.lotusSPM.dao.MessagesDao;
import com.lotus.lotusSPM.model.Messages;
import com.lotus.lotusSPM.service.base.MessagesService;


@Service
@Transactional(rollbackFor = Exception.class)
public class MessagesServiceImpl implements MessagesService {

	private MessagesDao messagesDao;

	@Autowired
	public void setMessagesDao(MessagesDao messagesDao) {
		this.messagesDao = messagesDao;
	}

	@Override
	public void deleteMessage(Long id) {
		messagesDao.deleteById(id);
	}

	@Override
	public Messages createMessage(Messages messages) {
		return messagesDao.save(messages);
	}

	@Override
	public List<Messages> getMessages() {
		return messagesDao.findAll();
	}

	@Override
	public List<Messages> findByTo(String to) {
		 List<Messages> results = messagesDao.findByTo(to);
	        return results;	}

	@Override
	public List<Messages> findByFrom(String from) {
		List<Messages> result = messagesDao.findByFrom(from);
		return result;
	}

	@Override
	public Messages findById(Long id) {
		return messagesDao.findById(id)
				.orElseThrow(() -> new RuntimeException("Message not found with id: " + id));
	}


}
