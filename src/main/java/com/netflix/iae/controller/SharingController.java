package com.netflix.iae.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.netflix.iae.service.FileDetail;
import com.netflix.iae.service.SearchResult;
import com.netflix.iae.service.SharingService;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing all the REST endpoints for the Sharing application.
 *
 * @author mimansha
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:3000")
public class SharingController {
  private static final Logger log = LoggerFactory.getLogger(SharingController.class);

  private final SharingService sharingService;
  private final ObjectMapper mapper = new ObjectMapper();

  @Autowired
  public SharingController(SharingService sharingService) {
    this.sharingService = Preconditions.checkNotNull(sharingService);
  }

  /**
   * REST endpoint to list all the files in the google drive. The list is organized in a tree
   * structure, starting with the root folder.
   *
   * @return JSON representation of the file hierarchy with additional metadata.
   */
  @RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public ResponseEntity<String> list() {
    log.info("Serving list request.");

    try {
      List<FileDetail> fileDetail = sharingService.listFiles();
      String result = mapper.writeValueAsString(fileDetail);
      return ResponseEntity.ok().body(result);
    } catch (Exception e) {
      log.error("Error in processing request.", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  /**
   * REST endpoint to transfer the ownership of one a file/folder to another user. All the
   * sub-folders and files in the file hierarchy of the root are affected by the ownership transfer.
   *
   * <p>NOTE: the ownership can only be transferred within the same domain.
   *
   * @param id fileId of the resource
   * @param ownerEmail the email address of the new owner.
   * @return
   */
  @RequestMapping(
      value = "/change-owner",
      params = {"id", "new-owner-email"},
      method = RequestMethod.GET,
      produces = "application/json")
  @ResponseBody
  public ResponseEntity<String> changeOwner(@RequestParam("id") String id, @RequestParam("new-owner-email") String ownerEmail)
      throws JsonProcessingException {
    log.info("Serving share request.");

    HashMap<String, String> responseMap = new HashMap<>();
    try {
      sharingService.changeOwner(id, ownerEmail);
      responseMap.put("message", "Successfully transferred ownership");

      return ResponseEntity.ok().body(mapper.writeValueAsString(responseMap));
    } catch (Exception e) {
      String errMsg = "Error in processing change owner request. " + e.getMessage();
      log.error(errMsg, e);

      responseMap.put("message", errMsg);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapper.writeValueAsString(responseMap));
    }
  }

  /**
   * REST endpoint to search for files/folders with matching names.
   *
   * @return JSON representation of the files
   */
  @RequestMapping(
      value = "/search",
      params = {"query"},
      method = RequestMethod.GET,
      produces = "application/json")
  @ResponseBody
  public ResponseEntity<String> search(@RequestParam("query") String query) {
    log.info("Serving search request.");

    try {
      List<SearchResult> fileDetail = sharingService.searchFilesByName(query);
      String result = mapper.writeValueAsString(fileDetail);
      return ResponseEntity.ok().body(result);
    } catch (Exception e) {
      log.error("Error in processing search request.", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }
}
