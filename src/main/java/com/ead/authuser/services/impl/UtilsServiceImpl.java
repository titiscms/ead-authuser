package com.ead.authuser.services.impl;

import com.ead.authuser.services.UtilsService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UtilsServiceImpl implements UtilsService {

    @Override
    public String getUrlGetAllCourseByUser(UUID userId, Pageable pageable) {
        return "/courses?userId=" + userId + "&page=" + pageable.getPageNumber() + "&size="
                + pageable.getPageSize() + "&sort=" + pageable.getSort().toString().replace(": ", ",");
    }

    @Override
    public String getUrlGetCourseById(UUID courseId) {
        return "/courses/" + courseId;
    }

    @Override
    public String getUrlDeleteCourseUserByUser(UUID userId) {
        return "/courses/users/" + userId ;
    }
}
