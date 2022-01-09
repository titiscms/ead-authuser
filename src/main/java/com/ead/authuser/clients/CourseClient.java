package com.ead.authuser.clients;

import com.ead.authuser.dtos.CourseDto;
import com.ead.authuser.dtos.ResponsePageDto;
import com.ead.authuser.services.UtilsService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Log4j2
@Component
public class CourseClient {

    @Value("${ead.api.url.course}")
    private String REQUEST_URI_COURSE;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UtilsService utilsService;

    public Page<CourseDto> getAllCoursesByUser(UUID userId, Pageable pageable) {
        List<CourseDto> searchResult = new ArrayList<>();
        String url = REQUEST_URI_COURSE + utilsService.getUrl(userId, pageable);
        log.debug("Request URL: {} ", url);
        log.info("Request URL: {} ", url);
        try {
            ParameterizedTypeReference<ResponsePageDto<CourseDto>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ResponsePageDto<CourseDto>> result = restTemplate.exchange(url, HttpMethod.GET, null, responseType);
            searchResult = result.getBody().getContent();
            log.debug("Response number of elements: {} ", searchResult.size());
        } catch (HttpStatusCodeException e) {
            log.error("Error request /courses ", e);
        }
        log.info("Ending request /courses userId {}", userId);
        return new PageImpl<>(searchResult);
    }

    public ResponseEntity<CourseDto> getOneCourseById(UUID courseId) {
        String url = REQUEST_URI_COURSE + utilsService.getCourseById(courseId);
        log.debug("Request URL: {} ", url);
        log.info("Request URL: {} ", url);
        return restTemplate.exchange(url, HttpMethod.GET, null, CourseDto.class);
    }
}
