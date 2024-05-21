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

package org.secretflow.secretpad.common.enums;

import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;

import java.util.Locale;

/**
 * User Owner Type
 *
 * @author beiwei
 * @date 2023/9/12
 */
public enum UserOwnerTypeEnum {
    /**
     * Edge
     */
    EDGE,

    /**
     * Center
     */
    CENTER,

    /**
     * P2p
     */
    P2P;

    public PermissionUserTypeEnum toPermissionUserType() {
        if (EDGE.equals(this)) {
            return PermissionUserTypeEnum.EDGE_USER;
        }
        if (CENTER.equals(this)) {
            return PermissionUserTypeEnum.USER;
        }
        if (P2P.equals(this)) {
            return PermissionUserTypeEnum.NODE;
        }
        throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, "Invalidate user owner type: " + this);
    }

    public static UserOwnerTypeEnum fromString(String str) {
        return switch (str.toLowerCase(Locale.ROOT)) {
            case "edge", "test" -> EDGE;
            case "center" -> CENTER;
            case "p2p", "autonomy" -> P2P;
            default -> throw new IllegalArgumentException("Invalidate user owner type: " + str);
        };
    }
}
