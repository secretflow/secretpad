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

package org.secretflow.secretpad.web.constant;

import java.util.Random;

/**
 * Authorization Constants
 *
 * @author : xiaonan.fhn
 * @date 2023/05/26
 */
public class AuthConstants {
    public static final String TOKEN_NAME = "User-Token";
    public static final String USER_NAME = "admin";
    private volatile static String RANDOM_PASSWORD;

    /**
     * get tokenName
     *
     * @param platformType   platform type
     * @param platformNodeId platform node id
     * @return {@link String }
     */

    @Deprecated
    public static String getTokenName(String platformType, String platformNodeId) {
        return TOKEN_NAME + "_" + platformType + "_" + platformNodeId;
    }

    public static String getRandomPassword() {
        if (RANDOM_PASSWORD == null) {
            synchronized (AuthConstants.class) {
                if (RANDOM_PASSWORD == null) {
                    RANDOM_PASSWORD = generateRandomPassword();
                }
            }
        }
        return RANDOM_PASSWORD;
    }

    public static String generateRandomPassword() {
        String upperCase = "ABCDEFGHJKMNPQRSTUVWXYZ";
        String lowerCase = "abcdefghjkmnpqrstuvwxyz";
        String numbers = "123456789";
        String specialChars = "@$!%*?&/\\";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            int type = random.nextInt(4);
            switch (type) {
                case 0:
                    password.append(upperCase.charAt(random.nextInt(upperCase.length())));
                    break;
                case 1:
                    password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
                    break;
                case 2:
                    password.append(numbers.charAt(random.nextInt(numbers.length())));
                    break;
                case 3:
                    password.append(specialChars.charAt(random.nextInt(specialChars.length())));
                    break;
                default:
                    break;
            }
        }
        String temPassword = password.toString();
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (!temPassword.matches(regex)) {
            return generateRandomPassword();
        }
        return password.toString();
    }
}
