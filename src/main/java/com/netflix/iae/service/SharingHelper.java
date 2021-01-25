package com.netflix.iae.service;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.common.base.Preconditions;
import com.netflix.iae.config.SharingAppConfig;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper class containing various business logic for the SharingService.
 *
 * @author mimansha
 */
@Component
public class SharingHelper {
  private static final Logger log = LoggerFactory.getLogger(SharingHelper.class);
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  private final SharingAppConfig appConfig;

  @Autowired
  public SharingHelper(final SharingAppConfig appConfig) {
    this.appConfig = Preconditions.checkNotNull(appConfig);
  }

  Drive getDrive() throws IOException, GeneralSecurityException {
    return new Drive.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, appConfig.driveCredential())
        .setApplicationName(SharingAppConfig.APPLICATION_NAME)
        .build();
  }

  boolean checkOwner(String fileId) throws IOException, GeneralSecurityException {
    final Drive drive = getDrive();
    File result = drive.files().get(fileId).setFields("id, name, owners").execute();

    return result.getOwners().get(0).getMe();
  }

  List<String> getAllChildren(final List<FileDetail> allFiles, final String fileId) {
    // Find the root node matching the fileId
    Queue<FileDetail> queue = new LinkedList<>();
    queue.addAll(allFiles);

    FileDetail rootNode = null;
    while (!queue.isEmpty()) {
      FileDetail node = queue.remove();
      if (node.getId().equals(fileId)) {
        rootNode = node;
        break;
      }
      queue.addAll(node.getNodes());
    }

    if (rootNode == null) {
      return Arrays.asList(fileId);
    }

    // Depth first search to create a list of all the children
    Stack<FileDetail> stack = new Stack<>();
    stack.add(rootNode);
    List<String> resultList = new ArrayList<>();
    while (!stack.isEmpty()) {
      FileDetail node = stack.pop();
      resultList.add(node.getId());

      node.getNodes().forEach(a -> stack.push(a));
    }

    return resultList;
  }

  void updatePermissionsInBatch(final List<String> fileIds, final String email)
      throws IOException, GeneralSecurityException {
    JsonBatchCallback<Permission> callback =
        new JsonBatchCallback<Permission>() {
          @Override
          public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
            log.error("Failure in transferring ownership.", e);
          }

          @Override
          public void onSuccess(Permission permission, HttpHeaders responseHeaders) {
            log.info("Ownership transferred. Permission ID: '{}'", permission.getId());
          }
        };

    final Drive drive = getDrive();
    BatchRequest batch = drive.batch();

    for (String fileId : fileIds) {
      Permission userPermission =
          new Permission().setType("user").setRole("owner").setEmailAddress(email);
      drive
          .permissions()
          .create(fileId, userPermission)
          .setFields("id")
          .setTransferOwnership(true)
          .queue(batch, callback);
    }

    batch.execute();
  }
}
