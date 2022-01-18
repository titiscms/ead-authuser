package com.ead.authuser.controllers;

import com.ead.authuser.clients.CourseClient;
import com.ead.authuser.dtos.CourseDto;
import com.ead.authuser.dtos.UserCourseDto;
import com.ead.authuser.models.UserCourseModel;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserCourseService;
import com.ead.authuser.services.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(path = "/users")
public class UserCourseController {

    @Autowired
    private CourseClient courseClient;

    @Autowired
    private UserService userService;

    @Autowired
    private UserCourseService userCourseService;

    @GetMapping(path = "/{userId}/courses")
    public ResponseEntity<Page<CourseDto>> getAllCoursesByUser(@PathVariable UUID userId,
                                                               @PageableDefault(page = 0, size = 10, sort = "courseId", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<CourseDto> allCoursesByUserPage = courseClient.getAllCoursesByUser(userId, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(allCoursesByUserPage);
    }

    @PostMapping(path = "/{userId}/courses/subscription")
    public ResponseEntity<Object> saveSubscriptionUserInCourse(@PathVariable UUID userId,
                                                               @RequestBody @Valid UserCourseDto userCourseDto) {
        log.debug("POST saveSubscriptionUserInCourse userCourseDto received {} ", userCourseDto.toString());
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (userModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        if (userCourseService.existsByUserAndCourseId(userModelOptional.get(), userCourseDto.getCourseId())) {
            log.warn("Subscription already exists for courseId {}", userCourseDto.getCourseId());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Subscription already exists.");
        }
        try {
            courseClient.getOneCourseById(userCourseDto.getCourseId());
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found.");
            }
            log.error("Could not retrieve course courseId {} ", userCourseDto.getCourseId(), e);
        }
        UserCourseModel userCourseModelSaved = userCourseService.save(userModelOptional.get().convertToUserCourseModel(userCourseDto.getCourseId()));
        log.debug("POST saveSubscriptionUserInCourse courseId saved {} ", userCourseModelSaved.getCourseId());
        log.info("Subscription created successfully courseId {}", userCourseModelSaved.getCourseId());
        return ResponseEntity.status(HttpStatus.CREATED).body(userCourseModelSaved);
    }

    @DeleteMapping(path = "/courses/{courseId}")
    public ResponseEntity<Object> deleteUserCourseByCourse(@PathVariable UUID courseId) {
        log.debug("DELETE deleteUserCourseByCourse userCourseId received {} ", courseId);
        if (!userCourseService.existsByCourseId(courseId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("UserCourse not found.");
        }
        userCourseService.deleteUserCourseByCourse(courseId);
        log.debug("DELETE deleteUserCourseByCourse courseId deleted {} ", courseId);
        log.info("UserCourse deleted successfully courseId {}", courseId);
        return ResponseEntity.status(HttpStatus.OK).body("UserCourse deleted successfully.");
    }
}
