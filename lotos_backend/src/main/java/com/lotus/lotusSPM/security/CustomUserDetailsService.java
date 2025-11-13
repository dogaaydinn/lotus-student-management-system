package com.lotus.lotusSPM.security;

import com.lotus.lotusSPM.dao.*;
import com.lotus.lotusSPM.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private CoordinatorDao coordinatorDao;

    @Autowired
    private AdminDao adminDao;

    @Autowired
    private InstructorDao instructorDao;

    @Autowired
    private CareerCenterDao careerCenterDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        throw new UnsupportedOperationException("Use loadUserByUsernameAndRole method");
    }

    public UserDetails loadUserByUsernameAndRole(String username, String role) throws UsernameNotFoundException {
        switch (role.toUpperCase()) {
            case "STUDENT":
                Student student = studentDao.getStudentByUserName(username);
                if (student == null) {
                    throw new UsernameNotFoundException("Student not found with username: " + username);
                }
                return new UserPrincipal(student.getId(), student.getUsername(),
                    student.getEmail(), student.getPassword(), "STUDENT");

            case "COORDINATOR":
                Coordinator coordinator = coordinatorDao.getCoordinatorByUserName(username);
                if (coordinator == null) {
                    throw new UsernameNotFoundException("Coordinator not found with username: " + username);
                }
                return new UserPrincipal(coordinator.getId(), coordinator.getUsername(),
                    coordinator.getEmail(), coordinator.getPassword(), "COORDINATOR");

            case "ADMIN":
                Admin admin = adminDao.getAdminByUserName(username);
                if (admin == null) {
                    throw new UsernameNotFoundException("Admin not found with username: " + username);
                }
                return new UserPrincipal(admin.getId(), admin.getUsername(),
                    admin.getEmail(), admin.getPassword(), "ADMIN");

            case "INSTRUCTOR":
                Instructor instructor = instructorDao.getInstructorByUserName(username);
                if (instructor == null) {
                    throw new UsernameNotFoundException("Instructor not found with username: " + username);
                }
                return new UserPrincipal(instructor.getId(), instructor.getUsername(),
                    instructor.getEmail(), null, "INSTRUCTOR");

            case "CAREER_CENTER":
                CareerCenter careerCenter = careerCenterDao.getCareerCenterByUserName(username);
                if (careerCenter == null) {
                    throw new UsernameNotFoundException("Career Center staff not found with username: " + username);
                }
                return new UserPrincipal(careerCenter.getId(), careerCenter.getUsername(),
                    careerCenter.getEmail(), careerCenter.getPassword(), "CAREER_CENTER");

            default:
                throw new UsernameNotFoundException("Invalid role: " + role);
        }
    }
}
