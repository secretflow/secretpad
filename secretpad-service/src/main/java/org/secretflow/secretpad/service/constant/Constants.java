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

package org.secretflow.secretpad.service.constant;

/**
 * Constants
 *
 * @author : xiaonan.fhn
 * @date 2023/5/15
 */
public class Constants {
    public static final String NODE_NAME_PATTERN = "^[\\u4e00-\\u9fa5a-zA-Z0-9\\_-]{1,32}$";
    public static final String IP_PORT_PATTERN =
            "^.{1,50}:([0-9]|[1-9]\\d|[1-9]\\d{2}|[1-9]\\d{3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$";

    public static final String TEE_PROJECT_MODE = "tee";

    public static final String HTTP_PREFIX_REG = "^http://.*";

    public static final String IP_PREFIX_REG = "^(http|https)://.*";
}
