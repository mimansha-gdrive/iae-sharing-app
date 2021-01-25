package com.netflix.iae.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Model to capture file details.
 *
 * @author mimansha
 */
public class FileDetail {
  public static final String SHARED_WITH_ME = "Shared with me";
  public static final String MY_DRIVE = "My Drive";

  private String text;
  private String parentId;
  private final String id;
  private final String type;
  private final List<FileDetail> nodes;

  /** Construct a FileDetail wrapper. */
  public FileDetail(String text, String id, List<String> parentId, String type) {
    if (parentId == null || parentId.isEmpty()) {
      this.text = text;
      this.parentId = SHARED_WITH_ME;
    } else {
      this.text = text;
      this.parentId = parentId.get(0);
    }

    this.id = id;
    this.type = type;
    this.nodes = new ArrayList<>();
  }

  public String getText() {
    return text;
  }

  public String getId() {
    return id;
  }

  public String getParentId() {
    return parentId;
  }

  public String getType() {
    return type;
  }

  public List<FileDetail> getNodes() {
    return nodes;
  }
}
