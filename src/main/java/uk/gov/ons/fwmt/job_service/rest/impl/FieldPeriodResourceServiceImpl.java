package uk.gov.ons.fwmt.job_service.rest.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.fwmt.job_service.rest.FieldPeriodResourceService;
import uk.gov.ons.fwmt.job_service.rest.dto.FieldPeriodDto;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Slf4j
@Service
public class FieldPeriodResourceServiceImpl implements FieldPeriodResourceService {

  @Autowired
  private transient RestTemplate restTemplate;
  @Autowired
  private transient BasicAuthorizationInterceptor basicInterceptor;
  @Value("${service.resource.fieldPeriod.operation.find.fieldPeriodUrl}")
  private transient String findURL;

  @PostConstruct
  private void initialize() {
    restTemplate.getInterceptors().add(basicInterceptor);
  }

  @Override
  public Optional<FieldPeriodDto> findByFieldPeriod(final String fieldPeriod) {

    try {
      final ResponseEntity<FieldPeriodDto> fieldPeriodDtoResponseEntity = restTemplate
          .exchange(findURL, HttpMethod.GET, null, FieldPeriodDto.class, fieldPeriod);
      if (fieldPeriodDtoResponseEntity != null && fieldPeriodDtoResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
        return Optional.ofNullable(fieldPeriodDtoResponseEntity.getBody());
      }
    } catch (HttpClientErrorException ht) {
      //TODO log error
    }
    return Optional.empty();
  }
}
