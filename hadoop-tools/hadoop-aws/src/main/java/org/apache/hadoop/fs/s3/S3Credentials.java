/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.fs.s3;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.s3native.S3xLoginHelper;

/**
 * <p>
 * Extracts AWS credentials from the filesystem URI or configuration.
 * </p>
 */
@InterfaceAudience.Private
@InterfaceStability.Unstable
@Deprecated
public class S3Credentials {
  
  private String accessKey;
  private String secretAccessKey; 

  /**
   * @param uri bucket URI optionally containing username and password.
   * @param conf configuration
   * @throws IllegalArgumentException if credentials for S3 cannot be
   * determined.
   * @throws IOException if credential providers are misconfigured and we have
   *                     to talk to them.
   */
  public void initialize(URI uri, Configuration conf) throws IOException {
    if (uri.getHost() == null) {
      throw new IllegalArgumentException("Invalid hostname in URI " + uri);
    }
    S3xLoginHelper.Login login =
        S3xLoginHelper.extractLoginDetailsWithWarnings(uri);
    if (login.hasLogin()) {
      accessKey = login.getUser();
      secretAccessKey = login.getPassword();
    }
    String scheme = uri.getScheme();
    String accessKeyProperty = String.format("fs.%s.awsAccessKeyId", scheme);
    String secretAccessKeyProperty =
      String.format("fs.%s.awsSecretAccessKey", scheme);
    if (accessKey == null) {
      accessKey = conf.getTrimmed(accessKeyProperty);
    }
    if (secretAccessKey == null) {
      final char[] pass = conf.getPassword(secretAccessKeyProperty);
      if (pass != null) {
        secretAccessKey = (new String(pass)).trim();
      }
    }
    if (accessKey == null && secretAccessKey == null) {
      throw new IllegalArgumentException("AWS " +
                                         "Access Key ID and Secret Access " +
                                         "Key must be specified " +
                                         "by setting the " +
                                         accessKeyProperty + " and " +
                                         secretAccessKeyProperty +
                                         " properties (respectively).");
    } else if (accessKey == null) {
      throw new IllegalArgumentException("AWS " +
                                         "Access Key ID must be specified " +
                                         "by setting the " +
                                         accessKeyProperty + " property.");
    } else if (secretAccessKey == null) {
      throw new IllegalArgumentException("AWS " +
                                         "Secret Access Key must be " +
                                         "specified by setting the " +
                                         secretAccessKeyProperty +
                                         " property.");       
    }

  }
  
  public String getAccessKey() {
    return accessKey;
  }
  
  public String getSecretAccessKey() {
    return secretAccessKey;
  }
}
