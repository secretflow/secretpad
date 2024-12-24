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


import org.secretflow.secretpad.common.constant.SystemConstants;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class TokenUtil {

    /**
     * default expire 30 min
     */
    private static final long EXPIRE_TIME_MILLIS = 30 * 60 * 1000;

    /**
     * default header
     */
    private static final Map<String, Object> HEADER = Map.of("typ", "JWT", "alg", "HS256");


    /**
     * default sign
     *
     * @param secret   secret
     * @param audience audience
     * @return sign
     */
    public static String sign(String secret, String audience) {
        String sign = sign(secret, audience, null, null);
        // ./config/p2pNodeInstToken/secret_audience
        String tokenFilePath = SystemConstants.P2P_NODE_INST_TOKEN_FILE + FileUtils.FILE_SEPARATOR + secret + "_" + audience;
        FileUtils.writeContentToFile(sign, tokenFilePath);
        return tokenFilePath;
    }


    /**
     * sign JWT token
     *
     * @param secret     secret key
     * @param audience   who need the token
     * @param payload    data need to pass
     * @param expireTime expire relative time default is 30 min
     */
    public static String sign(String secret, String audience, Map<String, Object> payload, Long expireTime) {

        long currentTime = System.currentTimeMillis();
        Map<String, Object> allPayload = new HashMap<>();
        allPayload.put("timestamp", currentTime);
        allPayload.put("signNonce", UUID.randomUUID().toString());
        if (payload != null) {
            allPayload.putAll(payload);
        }

        long expire = (expireTime != null && expireTime >= 0) ?
                (currentTime + expireTime) : (currentTime + EXPIRE_TIME_MILLIS);

        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withHeader(HEADER)
                .withAudience(audience) // nodeId
                .withPayload(allPayload) //other infor
                .withExpiresAt(new Date(expire)) //expire
                .sign(algorithm);
    }

    /**
     * verify
     * secret: instId
     * token:
     * audience: nodeId
     */
    public static boolean verify(String token, String secret, String audience) {
        DecodedJWT jwt;
        try {
            jwt = JWT.decode(token);
        } catch (Exception e) {
            log.error("token decode ex , ex={}  token={}", e, token);
            return false;
        }
        /* expire check **/
        if (jwt.getExpiresAt().before(new Date())) {
            log.error("token is expired , token={}", token);
            return false;
        }

        /* check audi*/
        String audJwt = jwt.getAudience().get(0);
        if (!StringUtils.equals(audJwt, audience)) {
            log.error("token audience is different, token={}", token);
            return false;
        }

        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
        } catch (Exception e) {
            log.error("token verify fail", e);
            return false;
        }
        return true;
    }
}
