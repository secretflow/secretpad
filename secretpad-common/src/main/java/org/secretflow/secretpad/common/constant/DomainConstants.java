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
 * @date 2023/08/01
 */
public class DomainConstants {

    public static final String ALICE = "alice";
    public static final String BOB = "bob";
    public static final String ALICE_TABLE = "alice-table";
    public static final String BOB_TABLE = "bob-table";

    public static final String NODE_READY = "Ready";

    public enum TokenStatusEnum {
        used,
        unused
    }

    public enum DomainRoleEnum {
        partner, normal
    }

    public enum DomainStatusEnum {
        // Pending...
        Pending,
        // Ready
        Ready,
        // NotReady
        NotReady,
        // Unknown
        Unknown
    }

    public enum DomainTypeEnum {
        // embedded
        embedded,
        // normal
        normal,
        // primary
        primary
    }

    public enum DomainCertConfigEnum {
        // configured
        configured,
        // unConfigured
        unconfirmed
    }

    public enum DomainModeEnum {
        // tee
        tee(0),
        // mpc
        mpc(1),
        // teeAndMpc
        teeAndMpc(2);

        public final int code;

        DomainModeEnum(int code) {
            this.code = code;
        }
    }

    public enum DomainEmbeddedNodeEnum {
        // alice
        alice,
        // bob
        bob,
        //tee
        tee
    }
}