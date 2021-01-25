package com.netflix.iae.config;

import org.springframework.context.annotation.Configuration;

/**
 * Application configuration for the sharing app.
 *
 * @author mimansha
 */
@Configuration
public class SharingAppConfig {
  public static final String APPLICATION_NAME = "Google Drive Sharing Service";
  private static final int DEFAULT_PAGINATION_SIZE = 100;

  public int getDefaultPaginationSize() {
    return DEFAULT_PAGINATION_SIZE;
  }
}
