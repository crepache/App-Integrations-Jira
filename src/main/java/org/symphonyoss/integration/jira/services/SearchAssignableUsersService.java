package org.symphonyoss.integration.jira.services;

import static org.symphonyoss.integration.exception.RemoteApiException.COMPONENT;
import static org.symphonyoss.integration.jira.properties.ServiceProperties.APPLICATION_KEY_ERROR;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Exception;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1HttpRequestException;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Provider;
import org.symphonyoss.integration.jira.exception.JiraAuthorizationException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.ErrorResponse;

import java.io.IOException;
import java.net.URL;

/**
 * Created by alexandre-silva-daitan on 15/08/17.
 */
@Component
public class SearchAssignableUsersService {

  @Autowired
  private LogMessageSource logMessage;

  public ResponseEntity searchAssingablesUsers(String accessToken, OAuth1Provider provider,
      URL myselfUrl, String component, String issueKey) {

    if (issueKey == null || issueKey.isEmpty()) {
      ErrorResponse response = new ErrorResponse();
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    HttpResponse response = null;
    try {
      response = provider.makeAuthorizedRequest(accessToken, myselfUrl, HttpMethods.GET, null);
    } catch (OAuth1HttpRequestException e) {
      if (e.getCode() == HttpStatus.NOT_FOUND.value()) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setMessage(e.getLocalizedMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
      }
    } catch (OAuth1Exception e) {
      throw new JiraAuthorizationException(component,
          logMessage.getMessage(APPLICATION_KEY_ERROR), e);
    }
    try {
      return ResponseEntity.ok().body(response.parseAsString());
    } catch (IOException e) {
      throw new JiraAuthorizationException(COMPONENT,
          logMessage.getMessage(APPLICATION_KEY_ERROR), e);
    }
  }
}
