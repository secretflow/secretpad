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

import org.secretflow.secretpad.common.enums.UserOwnerTypeEnum;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author yutu
 * @date 2024/04/22
 */
public class UserOwnerTypeEnumTest {

    @Test
    void fromString() {
        Assertions.assertEquals(UserOwnerTypeEnum.fromString("EDGE"), UserOwnerTypeEnum.EDGE, "no such user owner type");
        Assertions.assertEquals(UserOwnerTypeEnum.fromString("edge"), UserOwnerTypeEnum.EDGE, "no such user owner type");
        Assertions.assertEquals(UserOwnerTypeEnum.fromString("test"), UserOwnerTypeEnum.EDGE, "no such user owner type");
        Assertions.assertEquals(UserOwnerTypeEnum.fromString("TEST"), UserOwnerTypeEnum.EDGE, "no such user owner type");
        Assertions.assertEquals(UserOwnerTypeEnum.fromString("p2p"), UserOwnerTypeEnum.P2P, "no such user owner type");
        Assertions.assertEquals(UserOwnerTypeEnum.fromString("P2P"), UserOwnerTypeEnum.P2P, "no such user owner type");
        Assertions.assertEquals(UserOwnerTypeEnum.fromString("autonomy"), UserOwnerTypeEnum.P2P, "no such user owner type");
        Assertions.assertEquals(UserOwnerTypeEnum.fromString("AUTONOMY"), UserOwnerTypeEnum.P2P, "no such user owner type");
        Assertions.assertEquals(UserOwnerTypeEnum.fromString("center"), UserOwnerTypeEnum.CENTER, "no such user owner type");
        Assertions.assertEquals(UserOwnerTypeEnum.fromString("CENTER"), UserOwnerTypeEnum.CENTER, "no such user owner type");
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> UserOwnerTypeEnum.fromString("123"));
    }
}