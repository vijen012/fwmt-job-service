package uk.gov.ons.fwmt.job_service.rest.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.fwmt.job_service.rest.UserResourceService;
import uk.gov.ons.fwmt.job_service.rest.dto.UserDto;

import java.util.Optional;

@Slf4j
@Service
public class UserResourceServiceImpl implements UserResourceService {
  private transient RestTemplate restTemplate;

  private transient String findUrl;
  private transient String findAltUrl;

  @Autowired
  public UserResourceServiceImpl(
      RestTemplate restTemplate,
      @Value("${service.resource.baseUrl}") String baseUrl,
      @Value("${service.resource.operation.users.find.path}") String findPath,
      @Value("${service.resource.operation.users.findAlt.path}") String findAltPath) {
    this.restTemplate = restTemplate;
    this.findUrl = baseUrl + findPath;
    this.findAltUrl = baseUrl + findAltPath;
  }

  @Override
  public Optional<UserDto> findByAuthNo(String authNo) {
    log.debug("findByAuthNo: authNo={}", authNo);
    Optional<UserDto> userDto = ResourceRESTHelper.get(restTemplate, findUrl, UserDto.class, authNo);
    if (userDto.isPresent()) {
      log.debug("findByAuthNo found: {}", userDto.get());
    } else {
      log.debug("findByAuthNo not found");
    }
    return userDto;
 }

  @Override
  public Optional<UserDto> findByAlternateAuthNo(String authNo) {
    log.debug("findByAlternateAuthNo: authNo={}", authNo);
    Optional<UserDto> userDto = ResourceRESTHelper.get(restTemplate, findAltUrl, UserDto.class, authNo);
    if (userDto.isPresent()) {
      log.debug("findByAuthNo found: {}", userDto.get());
    } else {
      log.debug("findByAuthNo not found");
    }
    return userDto;
  }

  @Override
  public boolean existsByAuthNoAndActive(String authNo, boolean active) {
    log.debug("existsByAuthNoAndActive: authNo={},active={}", authNo, active);
    final Optional<UserDto> userDto = findByAuthNo(authNo);
    boolean result = userDto.filter(userDto1 -> userDto1.isActive() == active).isPresent();
    if (result) {
      log.debug("existsByAuthNoAndActive found");
    } else {
      log.debug("existsByAuthNoAndActive not found");
    }
    return result;
  }

  @Override
  public boolean existsByAlternateAuthNoAndActive(String authNo, boolean active) {
    log.debug("existsByAlternativeAuthNoAndActive: authNo={},active={}", authNo, active);
    final Optional<UserDto> userDto = findByAlternateAuthNo(authNo);
    boolean result = userDto.filter(userDto1 -> userDto1.isActive() == active).isPresent();
    if (result) {
      log.debug("existsByAlternativeAuthNoAndActive found");
    } else {
      log.debug("existsByAlternativeAuthNoAndActive not found");
    }
    return result;
  }

}
