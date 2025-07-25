/*
 * Copyright 2023 Ant Group Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.secretflow.secretpad.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Certificate utils
 *
 * @author yansi
 * @date 2023/5/8
 */
@Slf4j
public class CertUtils {

    /**
     * Specify the size limit for the private key file as recommended 10kb
     */
    private static final int MAX_PRIVATE_KEY_SIZE = 10 * 1024;

    /**
     * Regular expression pattern for PEM files compliant with PKCS standard specifications
     */
    private static final Pattern PEM_PATTERN = Pattern.compile(
            "-{5}BEGIN\\s+([A-Z\\s]+)-{5}\\n?" +
                    "([a-zA-Z0-9+/=\\s\\n]+)" +
                    "-{5}END\\s+\\1-{5}"
    );

    /**
     * Loads an X.509 certificate from the classpath resources or filesystem
     *
     * @param filepath path of a cert file
     *                 Example:
     *                 1. classpath:./certs/ca.crt
     *                 2. file:./config/certs/ca.crt
     *                 3. ./config/certs/ca.crt
     */
    public static X509Certificate loadX509Cert(String filepath) throws CertificateException, IOException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Path path = Paths.get(filepath);
        if (Files.size(path) > FileUtils.CERT_FILE_MAX_SIZE) {
            throw new IOException("Certificate file size exceeds limit: " + filepath);
        }
        try (InputStream in = Files.newInputStream(path)) {
            return (X509Certificate) cf.generateCertificate(in);
        }
    }

    public static void loadPrivateKey(String filePath) throws Exception {
        Path path = Paths.get(filePath);
        if (Files.size(path) > MAX_PRIVATE_KEY_SIZE) {
            throw new IOException("Private key file size exceeds limit: " + filePath);
        }
        String keyPem = Files.readString(path, StandardCharsets.UTF_8);
        byte[] keyBytes = decodePem(keyPem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        keyFactory.generatePrivate(keySpec);
    }

    private static byte[] decodePem(String pem) {
        Matcher matcher = PEM_PATTERN.matcher(pem);
        if (!matcher.find() || matcher.groupCount() < 2) {
            throw new IllegalArgumentException("Invalid PEM format");
        }

        String base64Data = matcher.group(2)
                .replaceAll("\\s", "")
                .replaceAll("\\n", "");
        return Base64.getDecoder().decode(base64Data);
    }
}