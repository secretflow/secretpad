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

import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

/**
 * EncryptUtils.
 *
 * @author cml
 * @date 2023/09/20
 */
public class EncryptUtils {

    public static final String KEY_ALGORITHM = "RSA";
    public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private final static Logger LOGGER = LoggerFactory.getLogger(EncryptUtils.class);

    /**
     * encrypt use private key
     *
     * @param data       data tobe signed
     * @param privateKey private key (BASE64 encoded)
     * @return
     * @throws Exception
     */
    public static String signSHA256withRSA(byte[] data, String privateKey) {
        try {
            PrivateKey pk = getPrivateKey(privateKey);
            byte[] encoded = pk.getEncoded();
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateK);
            signature.update(data);
            return Base64Utils.encode(signature.sign());
        } catch (Exception e) {
            LOGGER.error("signSHA256withRSA error", e);
            throw SecretpadException.of(SystemErrorCode.SIGNATURE_ERROR, e);
        }

    }


    /**
     * verify data
     *
     * @param data      data has been signed
     * @param publicKey public key (BASE64 encoded)
     * @param sign      signed data
     * @return
     * @throws Exception
     */
    public static boolean verifySHA256withRSA(byte[] data, String publicKey, String sign) throws Exception {
        PublicKey pk = getPublicKey(publicKey);
        byte[] encoded = pk.getEncoded();
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicK = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicK);
        signature.update(data);
        return signature.verify(Base64Utils.decode(sign));
    }

    public static boolean validateCertChain(List<String> certChains) {
        if (certChains == null || certChains.isEmpty()) {
            LOGGER.error("Certificate chain is empty or null");
            return false;
        }
        try {
            X509Certificate rootCert = (X509Certificate) decodeCertificate(certChains.get(certChains.size() - 1));
            X509Certificate subCert = (X509Certificate) decodeCertificate(certChains.get(0));

            Principal issuerDN = subCert.getIssuerDN();
            Principal subjectDN = rootCert.getSubjectDN();
            if (!issuerDN.equals(subjectDN)) {
                return false;
            }

            PublicKey publicKey = rootCert.getPublicKey();
            subCert.verify(publicKey);
            return true;
        } catch (Exception e) {
            LOGGER.error("Certificate chain validation error", e);
            return false;
        }
    }

    public static boolean compareCertPubKey(String comparer, String comparee) {
        if (comparer == null || comparee == null) {
            LOGGER.error("Certificate comparison failed: one of the certificates is null");
            return false;
        }
        try {
            Certificate certificateComparer = decodeCertificate(comparer);
            Certificate certificateComparee = decodeCertificate(comparee);

            return certificateComparer.getPublicKey().equals(certificateComparee.getPublicKey());
        } catch (Exception e) {
            LOGGER.error("Certificate comparison error", e);
            return false;
        }
    }

    public static Certificate decodeCertificate(String base64EncodedCert) {
        try {
            byte[] decodedBytes = Base64Utils.decode(base64EncodedCert);
            try (ByteArrayInputStream bis = new ByteArrayInputStream(decodedBytes)) {
                return CertificateFactory.getInstance("X.509").generateCertificate(bis);
            }
        } catch (IOException | CertificateException e) {
            LOGGER.error("Certificate decoding error", e);
            throw new RuntimeException(e);
        }
    }

    private static PrivateKey getPrivateKey(String privateKey) throws Exception {

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        byte[] decodeBase64 = org.apache.commons.codec.binary.Base64.decodeBase64(privateKey.getBytes(StandardCharsets.UTF_8));

        String key = new String(decodeBase64);

        key = key.replaceAll("\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replace("", "").replace(" ", "").trim();

        byte[] decode = org.apache.commons.codec.binary.Base64.decodeBase64(key);


        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(decode);

        return keyFactory.generatePrivate(pkcs8EncodedKeySpec);

    }

    private static PublicKey getPublicKey(String pubKey) throws Exception {

        byte[] decodeBase64 = org.apache.commons.codec.binary.Base64.decodeBase64(pubKey.getBytes(StandardCharsets.UTF_8));


        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        Certificate certificate = cf.generateCertificate(new ByteArrayInputStream(decodeBase64));

        return certificate.getPublicKey();

    }


    /**
     * Create base64 encoded signature using SHA256/RSA.
     *
     * @param input str
     * @param strPk pk
     */
    public static String signSHA256RSA(String input, String strPk) throws Exception {
        // Remove markers and new line characters in private key
        String realPK = strPk.replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("\n", "");
        byte[] b1 = Base64.getDecoder().decode(realPK);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(b1);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(kf.generatePrivate(spec));
        byte[] encode = input.getBytes();
        privateSignature.update(encode);
        byte[] s = privateSignature.sign();
        return Base64.getEncoder().encodeToString(s);
    }
}
