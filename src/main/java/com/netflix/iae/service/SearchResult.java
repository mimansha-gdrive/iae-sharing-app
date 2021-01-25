package com.netflix.iae.service;

/**
 * POJO for Search Results.
 *
 * @author mimansha
 */
public class SearchResult {
  private final String text;
  private final String id;
  private final String type;

  public SearchResult(String text, String id, String type) {
    this.text = text;
    this.id = id;
    this.type = type;
  }

  public String getText() {
    return text;
  }

  public String getId() {
    return id;
  }

  public String getType() {
    return type;
  }
}
