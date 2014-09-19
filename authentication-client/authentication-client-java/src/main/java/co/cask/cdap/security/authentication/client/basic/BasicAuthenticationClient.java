/*
 * Copyright © 2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.security.authentication.client.basic;

import co.cask.cdap.security.authentication.client.AbstractAuthenticationClient;
import co.cask.cdap.security.authentication.client.AccessToken;
import co.cask.cdap.security.authentication.client.Credential;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;

/**
 * Authentication client that supports "Basic access authentication" using username and password.
 */
public class BasicAuthenticationClient extends AbstractAuthenticationClient {
  private static final Logger LOG = LoggerFactory.getLogger(BasicAuthenticationClient.class);

  private static final String AUTHENTICATION_HEADER_PREFIX_BASIC = "Basic ";
  private static final String USERNAME_PROP_NAME = "security.auth.client.username";
  private static final String PASSWORD_PROP_NAME = "security.auth.client.password";
  private static final String DISABLE_SELF_SIGNED_CERTIFICATES_PROP_NAME = "security.auth.disable.certificate.check";

  private boolean disableCertCheck;
  private String username;
  private String password;
  private final List<Credential> credentials;

  /**
   * Constructs new instance.
   */
  public BasicAuthenticationClient() {
    super();
    credentials = ImmutableList.of(new Credential(USERNAME_PROP_NAME, "Username for basic authentication.", false),
                                   new Credential(PASSWORD_PROP_NAME, "Password for basic authentication.", true));
  }

  @Override
  public void configure(Properties properties) {
    if (StringUtils.isNotEmpty(username) || StringUtils.isNotEmpty(password)) {
      throw new IllegalStateException("Client is already configured!");
    }

    username = properties.getProperty(USERNAME_PROP_NAME);
    Preconditions.checkArgument(StringUtils.isNotEmpty(username), "The username property cannot be empty.");

    password = properties.getProperty(PASSWORD_PROP_NAME);
    Preconditions.checkArgument(StringUtils.isNotEmpty(password), "The password property cannot be empty.");

    disableCertCheck = Boolean.valueOf(properties.getProperty(DISABLE_SELF_SIGNED_CERTIFICATES_PROP_NAME));

    LOG.debug("Basic authentication client is configured successfully.");
  }

  @Override
  public List<Credential> getRequiredCredentials() {
    return credentials;
  }

  @Override
  protected AccessToken fetchAccessToken() throws IOException {
    if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
      throw new IllegalStateException("Base authentication client is not configured!");
    }

    LOG.debug("Authentication is enabled in the gateway server. Authentication URI {}.", getAuthUrl());
    HttpGet getRequest = new HttpGet(getAuthUrl());

    String auth = Base64.encodeBase64String(String.format("%s:%s", username, password).getBytes());
    auth = auth.replaceAll("(\r|\n)", StringUtils.EMPTY);
    getRequest.addHeader(HttpHeaders.AUTHORIZATION, AUTHENTICATION_HEADER_PREFIX_BASIC + auth);

    return execute(getRequest);
  }


  @Override
  protected HttpClient initHttpClient() {
    Registry<ConnectionSocketFactory> registry = null;
    if (disableCertCheck) {
      try {
        registry = getRegistryWithDisabledCertCheck();
        LOG.debug("init HTTP Client with self-signed certificates");
      } catch (KeyManagementException e) {
        LOG.error("Failed to init SSL context: {}", e);
      } catch (NoSuchAlgorithmException e) {
        LOG.error("Failed to get instance of SSL context: {}", e);
      }
    }

    if (registry != null) {
      return HttpClients.custom().setConnectionManager(new BasicHttpClientConnectionManager(registry)).build();
    } else {
      return HttpClients.createDefault();
    }
  }
}