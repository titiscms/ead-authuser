package com.ead.authuser.services;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UtilsService {

    String getUrlGetAllCourseByUser(UUID userId, Pageable pageable);

    String getUrlGetCourseById(UUID courseId);

    String getUrlDeleteCourseUserByUser(UUID userId);
}
