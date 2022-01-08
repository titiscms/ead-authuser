package com.ead.authuser.controllers;

import com.ead.authuser.clients.UserClient;
import com.ead.authuser.dtos.CourseDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(path = "/users/{userId}/courses")
public class UserCourseController {

    @Autowired
    private UserClient userClient;

    @GetMapping
    public ResponseEntity<Page<CourseDto>> getAllCoursesByUser(@PathVariable UUID userId,
                                                               @PageableDefault(page = 0, size = 10, sort = "courseId", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<CourseDto> allCoursesByUserPage = userClient.getAllCoursesByUser(userId, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(allCoursesByUserPage);
    }
}
