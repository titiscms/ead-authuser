package com.ead.authuser.clients;

import com.ead.authuser.dtos.CourseDto;
import com.ead.authuser.dtos.ResponsePageDto;
import com.ead.authuser.services.UtilsService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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

//    @Retry(name = "retryInstance", fallbackMethod = "retryFallback")
    // no caso dessa chamada não faria sentido ter um metodo fallback para esse circuit breaker.
    @CircuitBreaker(name = "circuitbreakerInstance", fallbackMethod = "circuitbreakerFallback")
    public Page<CourseDto> getAllCoursesByUser(UUID userId, Pageable pageable, String token) {
        List<CourseDto> searchResult = new ArrayList<>();
        String url = REQUEST_URI_COURSE + utilsService.getUrlGetAllCourseByUser(userId, pageable);
        log.debug("Request URL: {} ", url);
        log.info("Request URL: {} ", url);
        // TODO : Remove try / catch before global exception handling
        try {
            ParameterizedTypeReference<ResponsePageDto<CourseDto>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ResponsePageDto<CourseDto>> result = restTemplate.exchange(url, HttpMethod.GET, buildRequestEntity(token), responseType);
            searchResult = result.getBody().getContent();
            log.debug("Response number of elements: {} ", searchResult.size());
        } catch (HttpStatusCodeException e) {
            log.error("Error request /courses ", e);
        }
        log.info("Ending request /courses userId {}", userId);
        return new PageImpl<>(searchResult);
    }

    public Page<CourseDto> retryFallback(UUID userId, Pageable pageable, Throwable cause) {
        log.error("Inside retry retryFallback, cause - {} ", cause.toString());
        List<CourseDto> searchResult = new ArrayList<>();
        return new PageImpl<>(searchResult);
    }

    public Page<CourseDto> circuitbreakerFallback(UUID userId, Pageable pageable, Throwable cause) {
        log.error("Inside circuit breaker circuitbreakerFallback, cause - {} ", cause.toString());
        List<CourseDto> searchResult = new ArrayList<>();
        return new PageImpl<>(searchResult);
    }

    private HttpEntity<String> buildRequestEntity(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        return new HttpEntity<>("parameters", headers);
    }
}
