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

/**
 * @author yutu
 * @date 2024/07/24
 */
public class DesensitizationUtils {

    /**
     * mobile phone number desensitization
     *
     * @param mobile phoneNumber
     * @return desensitized mobile phone number
     */
    public static String mobileDesensitize(String mobile) {
        if (mobile == null || !mobile.matches("^1[3-9]\\d{9}$")) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(7);
    }

    /**
     * id number desensitization
     *
     * @param idCard idNumber
     * @return desensitized id number
     */
    public static String idCardDesensitize(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        int length = idCard.length();
        return idCard.substring(0, 4) + "****" + idCard.substring(length - 4);
    }


    public static String akSkDesensitize(String akSk) {
        if (akSk == null || akSk.length() < 3) {
            return null;
        }
        return akSk.substring(0, 3) + "****";
    }

}
