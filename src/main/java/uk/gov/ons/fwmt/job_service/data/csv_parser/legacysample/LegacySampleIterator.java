package uk.gov.ons.fwmt.job_service.data.csv_parser.legacysample;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import uk.gov.ons.fwmt.job_service.data.csv_parser.CSVIterator;
import uk.gov.ons.fwmt.job_service.data.legacy_ingest.LegacySampleGFFDataIngest;
import uk.gov.ons.fwmt.job_service.data.legacy_ingest.LegacySampleIngest;
import uk.gov.ons.fwmt.job_service.data.legacy_ingest.LegacySampleLFSDataIngest;
import uk.gov.ons.fwmt.job_service.data.legacy_ingest.LegacySampleSurveyType;
import uk.gov.ons.fwmt.job_service.exceptions.ExceptionCode;
import uk.gov.ons.fwmt.job_service.exceptions.types.FWMTCommonException;
import uk.gov.ons.fwmt.job_service.rest.client.FieldPeriodResourceServiceClient;

import java.time.LocalDate;

public class LegacySampleIterator extends CSVIterator<LegacySampleIngest> {
  private LegacySampleSurveyType legacySampleSurveyType;

  private FieldPeriodResourceServiceClient fieldPeriodResourceServiceClient;

  public LegacySampleIterator(CSVParser parser, LegacySampleSurveyType legacySampleSurveyType,
      FieldPeriodResourceServiceClient fieldPeriodResourceServiceClient) {
    super(parser);
    this.legacySampleSurveyType = legacySampleSurveyType;
    this.fieldPeriodResourceServiceClient = fieldPeriodResourceServiceClient;
  }

  @Override
  public LegacySampleIngest ingest(CSVRecord record) throws FWMTCommonException {
    // handle fields specific to a survey type
    LegacySampleIngest instance = new LegacySampleIngest();
    switch (legacySampleSurveyType) {
    case LFS:
      parseLegacySampleLFSData(instance, record);
      break;
    case GFF:
      parseLegacySampleGFFData(instance, record);
      break;
    default:
      throw new IllegalArgumentException("Unknown survey type");
    }
    parseLegacySampleCommonData(instance, record);
    return instance;
  }

  protected void parseLegacySampleCommonData(LegacySampleIngest instance, CSVRecord record) {
    // derive the TM job id
    instance.setTmJobId(LegacySampleUtils.constructTmJobId(record, legacySampleSurveyType).trim());

    // derive the coordinates, if we were given a non-null non-empty grid ref
    if (instance.getOsGridRef() != null && instance.getOsGridRef().length() > 0) {
      String[] osGridRefSplit = instance.getOsGridRef().split(",", 2);
      if (osGridRefSplit.length != 2) {
        throw FWMTCommonException
            .makeCsvInvalidFieldException("OSGridRef", "Did not match the expected format of 'X,Y'");
      }
      instance.setGeoX(Float.parseFloat(osGridRefSplit[0]));
      instance.setGeoY(Float.parseFloat(osGridRefSplit[1]));
    }

    // Set Lat and Long from CSV Record
    if(instance.getLat() != null) {
      instance.setLat(Float.valueOf(record.get("Lat")));
    }
    if(instance.getLng() != null) {
      instance.setLng(Float.valueOf(record.get("Long")));
    }
  }

  protected void parseLegacySampleGFFData(LegacySampleIngest instance, CSVRecord record) throws FWMTCommonException {
    // set normal fields
    LegacySampleAnnotationProcessor.process(instance, record, "GFF");
    // set derived due date
    String tla = instance.getTla().toUpperCase();
    int stage = Integer.parseInt(instance.getStage().trim()); 
    String reissue = instance.getStage().substring(1, 2);
    LocalDate date = null;
    if (tla.equals("NSW") && (reissue.equals("2") || reissue.equals("3")) && stage > 922){    
        date = LegacySampleUtils.convertToFieldPeriodDate(instance.getStage(), fieldPeriodResourceServiceClient).plusDays(15);;
    }
    else {
    	date = LegacySampleUtils.convertToFieldPeriodDate(instance.getStage(), fieldPeriodResourceServiceClient);
    }
    instance.setDueDate(date);
    instance.setCalculatedDueDate(String.valueOf(date));
    // set survey type and extra data
    instance.setLegacySampleSurveyType(LegacySampleSurveyType.GFF);
    instance.setGffData(new LegacySampleGFFDataIngest());
    instance.setLfsData(null);
    LegacySampleAnnotationProcessor.process(instance.getGffData(), record, null);
  }

  protected void parseLegacySampleLFSData(LegacySampleIngest instance, CSVRecord record) throws FWMTCommonException{
    // set normal fields
    LegacySampleAnnotationProcessor.process(instance, record, "LFS");
    // set derived due date
    LocalDate date = LegacySampleUtils.convertToFieldPeriodDate(instance.getStage(), fieldPeriodResourceServiceClient);
    instance.setDueDate(date);
    instance.setCalculatedDueDate(String.valueOf(date));
    // set if the record is looking for work

    // set survey type and extra data
    instance.setLegacySampleSurveyType(LegacySampleSurveyType.LFS);
    instance.setGffData(null);
    instance.setLfsData(new LegacySampleLFSDataIngest());

    try {
      instance.setLfsData(LegacySampleUtils.checkSetLookingForWorkIndicator(instance, record));
    } catch (FWMTCommonException e) {
      throw new FWMTCommonException(ExceptionCode.CSV_OTHER,"Error within job indicator", e);
    }

    LegacySampleAnnotationProcessor.process(instance.getLfsData(), record, null);
  }

}
