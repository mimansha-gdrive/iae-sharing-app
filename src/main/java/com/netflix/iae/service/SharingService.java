package com.netflix.iae.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.common.base.Preconditions;
import com.netflix.iae.Utils;
import com.netflix.iae.config.SharingAppConfig;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to interact with Google drive using their client.
 *
 * @author mimansha
 */
@Service
public class SharingService {
  private static final Logger log = LoggerFactory.getLogger(SharingService.class);

  private final SharingAppConfig appConfig;
  private final SharingHelper sharingHelper;

  @Autowired
  public SharingService(SharingAppConfig appConfig, SharingHelper sharingHelper) {
    this.appConfig = appConfig;
    this.sharingHelper = sharingHelper;
  }

  /**
   * Transfers the ownership of a drive resource identified by {@code fileId} to new owner {@code
   * newOwnerEmail}. Ownership is transferred for all the folders and files owned by this user
   * (authenticated one) to the new user.
   *
   * @param fileId id of the google drive file
   * @param newOwnerEmail new owner email address
   * @throws IOException IOException.
   * @throws GeneralSecurityException GeneralSecurityException.
   */
  public void changeOwner(final String fileId, final String newOwnerEmail)
      throws IOException, GeneralSecurityException {
    log.info("Request to change owner of file '{}' to user '{}'", fileId, newOwnerEmail);

    Preconditions.checkArgument(fileId != null && !fileId.isEmpty(), "Invalid fileId.");
    Preconditions.checkArgument(
        newOwnerEmail != null && !newOwnerEmail.isEmpty(), "Invalid email.");
    Preconditions.checkArgument(Utils.isValidEmailAddress(newOwnerEmail), "Invalid email.");
    Preconditions.checkArgument(
        sharingHelper.checkOwner(fileId), "Not the owner of requested resource.");

    // 1. List all the files under the given fileId
    List<String> children = sharingHelper.getAllChildren(listFiles(), fileId);

    // 2. Batch update ownership transfer
    sharingHelper.updatePermissionsInBatch(children, newOwnerEmail);
  }

  /**
   * Lists the files present in the drive in a tree structure. There would be multiple root elements
   * depending on the folder structure at the drive. File list request uses pagination and the page
   * size is configured at {@link SharingAppConfig}.
   *
   * @return List of file roots.
   * @throws IOException IOException.
   * @throws GeneralSecurityException GeneralSecurityException.
   */
  public List<FileDetail> listFiles() throws IOException, GeneralSecurityException {
    final Drive drive = sharingHelper.getDrive();
    final List<FileDetail> allFiles = new ArrayList<>();
    FileList result = null;

    while (result == null || result.getNextPageToken() != null) {
      result =
          drive
              .files()
              .list()
              .setPageSize(appConfig.getDefaultPaginationSize())
              .setPageToken(result == null ? null : result.getNextPageToken())
              .setFields("nextPageToken, files(id, name, parents, mimeType)")
              .execute();

      List<File> files = result.getFiles();
      if (files == null || files.isEmpty()) {
        break;
      }

      List<FileDetail> details =
          files.stream()
              .map(a -> new FileDetail(a.getName(), a.getId(), a.getParents(), a.getMimeType()))
              .collect(Collectors.toList());

      allFiles.addAll(details);
    }

    return constructTree(allFiles);
  }

  /**
   * Constructs a file tree hierarchy based on the parent ids available.
   *
   * @param details list of file details
   * @return root FileDetail object
   */
  private List<FileDetail> constructTree(List<FileDetail> details) {
    Map<String, FileDetail> fileMap =
        details.stream().collect(Collectors.toMap(FileDetail::getId, Function.identity()));

    List<FileDetail> roots = new ArrayList<>();

    // Link items to their parents
    for (FileDetail detail : details) {
      String parentId = detail.getParentId();
      FileDetail parent = fileMap.get(parentId);

      if (parent == null) {
        String dummyParent = parentId.equals(FileDetail.SHARED_WITH_ME) ? FileDetail.SHARED_WITH_ME : FileDetail.MY_DRIVE;

        FileDetail rootFile = new FileDetail(dummyParent, dummyParent, Arrays.asList(""), "");
        fileMap.put(parentId, rootFile);
        parent = rootFile;
        roots.add(rootFile);
      }

      parent.getNodes().add(detail);
    }

    return roots;
  }

  /**
   * Search for files/folders by name.
   *
   * @param name name of the resource.
   * @return
   * @throws IOException
   * @throws GeneralSecurityException
   */
  public List<SearchResult> searchFilesByName(String name) throws IOException, GeneralSecurityException {
    final Drive drive = sharingHelper.getDrive();
    String query = "name contains '" + name + "'";

    FileList result =
        drive.files().list().setQ(query).setFields("nextPageToken, files(id, name, mimeType)").execute();

    List<File> files = result.getFiles();
    if (files == null || files.isEmpty()) {
      return new ArrayList<>();
    }

    List<SearchResult> details =
            files.stream()
                    .map(a -> new SearchResult(a.getName(), a.getId(), a.getMimeType()))
                    .collect(Collectors.toList());

    return details;
  }
}
