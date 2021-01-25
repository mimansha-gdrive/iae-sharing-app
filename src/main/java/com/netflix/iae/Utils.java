package com.netflix.iae;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.validator.routines.EmailValidator;

/**
 * Common utilities for the application.
 *
 * @author mimansha
 */
public class Utils {
  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
  private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);
  private static final String TOKENS_DIRECTORY_PATH = "tokens";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final int GOOGLE_AUTH_RECEIVER_PORT = 8888;
  /**
   * Validates if the given input is a valid email address.
   *
   * @param email input email
   * @return boolean indicating if the address is a valid email.
   */
  public static boolean isValidEmailAddress(String email) {
    return EmailValidator.getInstance(true).isValid(email);
  }

  /**
   * Get credentials for google drive.
   * IMP: The OAUTH2 tokens are received the very first time and stored in the tokens directory. They
   * are refreshed automatically then after.

   * @return OAUTH2 Credentials.
   * @throws IOException
   * @throws GeneralSecurityException
   */
  public static Credential getDriveCredential() throws IOException, GeneralSecurityException {
    // Load client secrets.
    InputStream in = Utils.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

    if (in == null) {
      throw new FileNotFoundException("Credentials not found in resource location: " + CREDENTIALS_FILE_PATH);
    }

    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow =
            new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();

    // DO NOT MODIFY THE PORT, it is used to receive the tokens
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(GOOGLE_AUTH_RECEIVER_PORT).build();
    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
  }
}
