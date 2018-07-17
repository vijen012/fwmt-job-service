package uk.gov.ons.fwmt.job_service.rest.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.fwmt.job_service.exceptions.types.FWMTCommonException;
import uk.gov.ons.fwmt.job_service.rest.dto.FieldPeriodDto;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FieldPeriodResourceServiceImplTest {

  @InjectMocks private FieldPeriodResourceServiceImpl fieldPeriodResourceService;
  @Mock private RestTemplate restTemplate;
  @Mock private ResponseEntity<FieldPeriodDto> responseEntity;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void findByFieldPeriod() {
    //Given
    String fieldPeriod = "807";
    FieldPeriodDto expectedFPDto = new FieldPeriodDto();
    when(restTemplate.getForEntity(any(), eq(FieldPeriodDto.class), eq(fieldPeriod)))
        .thenReturn(responseEntity);
    when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
    when(responseEntity.getBody()).thenReturn(expectedFPDto);

    //When
    Optional<FieldPeriodDto> result = fieldPeriodResourceService.findByFieldPeriod(fieldPeriod);

    //Then
    assertTrue(result.isPresent());
    assertEquals(expectedFPDto, result.get());
    verify(restTemplate).getForEntity(any(), eq(FieldPeriodDto.class), eq(fieldPeriod));
  }

  // this normally throws a ResourceServiceMalfunctionException
  // TODO add a check for the exception code
  @Test
  public void findByFieldPeriodAndThrowHttpClientErrorException() {
    //Given
    String fieldPeriod = "807";
    when(restTemplate.getForEntity(any(), eq(FieldPeriodDto.class), eq(fieldPeriod)))
        .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

    expectedException.expect(FWMTCommonException.class);
    expectedException.expectMessage(HttpStatus.BAD_REQUEST.toString());

    //When
    fieldPeriodResourceService.findByFieldPeriod(fieldPeriod);
  }
}