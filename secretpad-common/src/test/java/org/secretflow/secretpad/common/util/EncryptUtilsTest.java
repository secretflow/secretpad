/*
 * Copyright 2024 Ant Group Co., Ltd.
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

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

/**
 * @author chenmingliang
 * @date 2024/06/13
 */
public class EncryptUtilsTest {

    @Test
    public void decodeCertificateInvalidBase64() {
        String base64 = "invalid_base64";
        Assertions.assertThrows(RuntimeException.class, () -> EncryptUtils.decodeCertificate(base64));
    }

    @Test
    public void validateCertEmptyInput() {
        Assertions.assertFalse(EncryptUtils.validateCertChain(new ArrayList<>()));
    }

    @Test
    public void validateCert() throws IOException, InterruptedException, CertificateException {
        Process process = initCmd();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while ((reader.readLine()) != null) ;
        }

        process.waitFor();

        try (FileInputStream rootCertStream = new FileInputStream("root.crt");
             FileInputStream intermediateCertStream = new FileInputStream("intermediate.crt")) {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate rootCert = (X509Certificate) certFactory.generateCertificate(rootCertStream);

            X509Certificate intermediateCert = (X509Certificate) certFactory.generateCertificate(intermediateCertStream);

            EncryptUtils.validateCertChain(Lists.newArrayList(Base64Utils.encode(rootCert.getEncoded()), Base64Utils.encode(intermediateCert.getEncoded())));

            Assertions.assertTrue(EncryptUtils.compareCertPubKey(Base64Utils.encode(rootCert.getEncoded()), Base64Utils.encode(rootCert.getEncoded())));
        }


    }

    @Test
    public void testComparePubKeyFalse() {
        EncryptUtils.compareCertPubKey("", "");
    }


    private static Process initCmd() throws IOException {
        String[] commands = {
                "/bin/sh", "-c",
                "openssl genpkey -algorithm RSA -out root.key -aes256 -pass pass:rootpass && " +
                        "openssl req -x509 -new -key root.key -sha256 -days 3650 -out root.crt -subj \"/C=US/ST=CA/L=San Francisco/O=MyOrg/OU=MyUnit/CN=Root CA\" -passin pass:rootpass && " +
                        "openssl genpkey -algorithm RSA -out intermediate.key -aes256 -pass pass:intermediatepass && " +
                        "openssl req -new -key intermediate.key -out intermediate.csr -subj \"/C=US/ST=CA/L=San Francisco/O=MyOrg/OU=MyUnit/CN=Intermediate CA\" -passin pass:intermediatepass && " +
                        "openssl x509 -req -in intermediate.csr -CA root.crt -CAkey root.key -CAcreateserial -out intermediate.crt -days 3650 -sha256 -passin pass:rootpass"
        };

        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.redirectErrorStream(true);

        return pb.start();
    }


}
