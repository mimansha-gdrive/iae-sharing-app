package com.netflix.iae;

import org.apache.commons.validator.routines.EmailValidator;

/**
 * Common utilities.
 *
 * @author mimansha
 */
public class Utils {
  /**
   * Validates if the given input is a valid email address.
   *
   * @param email input email
   * @return boolean indicating if the address is a valid email.
   */
  public static boolean isValidEmailAddress(String email) {
    return EmailValidator.getInstance(true).isValid(email);
  }
}
