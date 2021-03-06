/*
  Copyright.. etc
 */

package uk.gov.ons.fwmt.job_service.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.ons.fwmt.job_service.data.dto.GatewayCommonErrorDTO;
import uk.gov.ons.fwmt.job_service.data.dto.SampleSummaryDTO;
import uk.gov.ons.fwmt.job_service.service.JobService;

import java.io.IOException;

import static uk.gov.ons.fwmt.job_service.config.MDCHelper.FILENAME_KEY;
import static uk.gov.ons.fwmt.job_service.config.MDCHelper.FILENAME_PREFIX;

/**
 * Class for file upload controller
 *
 * @author Thomas Poot
 * @author James Berry
 * @author Jacob Harrison
 */

@Slf4j
@RestController
public class JobServiceController {

  private final JobService jobService;

  @Autowired
  public JobServiceController(JobService jobService) {
    this.jobService = jobService;
  }

  @RequestMapping(value = "/samples", method = RequestMethod.POST, produces = "application/json")
  @ApiResponses(value = {
      @ApiResponse(code = 400, message = "Bad Request", response = GatewayCommonErrorDTO.class),
      @ApiResponse(code = 415, message = "Unsupported Media Type", response = GatewayCommonErrorDTO.class),
      @ApiResponse(code = 500, message = "Internal Server Error", response = GatewayCommonErrorDTO.class),
  })
  public ResponseEntity<SampleSummaryDTO> sampleREST(@RequestParam("file") MultipartFile file,
      RedirectAttributes redirectAttributes)
      throws IOException {
    SampleSummaryDTO summary = null;
    // log the file name we're working with
    MDC.put(FILENAME_KEY, FILENAME_PREFIX + file.getOriginalFilename());
    log.info("Entered with file name {}", file.getOriginalFilename());
    if(!"sample_LFS_2018-07-30T14-14-10Z.csv".equals(file.getOriginalFilename())) {
      summary = jobService.processSampleFile(file);
      log.info("Exited with {} processed rows and {} unprocessed rows", summary.getProcessedRows(),
              summary.getUnprocessedRows().size());
    }
    return ResponseEntity.ok(summary);
  }
}
