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

package org.secretflow.secretpad.common.constant;

/**
 * @author yutu
 * @date 2023/11/15
 */
public class SystemConstants {
    public static final String DEV = "dev";

    public static final String EDGE = "edge";
    public static final String DEFAULT = "default";

    public static final String TEST = "test";

    public static final String SKIP_TEST = "!test";

    public static final String P2P = "p2p";
    public static final String SKIP_TEST_P2P = "!test&&!p2p";

    public static final String SKIP_P2P = "!p2p";

    public static final String USER_ADMIN = "admin";

    public static final String USER_OWNER_ID_FILE = "./config/ownerId";

    public static final String P2P_NODE_INST_TOKEN_FILE = "./config/p2pNodeInstToken";
}