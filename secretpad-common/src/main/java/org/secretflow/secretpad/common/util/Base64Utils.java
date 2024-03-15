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

import org.apache.commons.codec.binary.Base64;


/**
 * ApprovalController.
 *
 * @author cml
 * @date 2023/11/17
 */
public final class Base64Utils {

    private Base64Utils() {
    }

    public static byte[] decode(String base64) {
        byte[] bytes = base64.getBytes();
        return Base64.decodeBase64(bytes);
    }

    public static String encode(byte[] bytes) {
        return new String(Base64.encodeBase64(bytes));
    }


}
