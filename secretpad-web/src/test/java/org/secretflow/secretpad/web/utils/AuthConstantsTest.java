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

package org.secretflow.secretpad.web.utils;

import org.secretflow.secretpad.web.constant.AuthConstants;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author yutu
 * @date 2024/12/13
 */
@Slf4j
public class AuthConstantsTest {

    @Test
    public void testAuthConstants() {
        for (int i = 0; i < 10; i++) {
            String password = AuthConstants.generateRandomPassword();
            Assertions.assertEquals(8, password.length());
            log.info("generateRandomPassword password:{}", password);
        }

        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                String password = AuthConstants.getRandomPassword();
                Assertions.assertEquals(8, password.length());
                log.info("getRandomPassword password:{}", password);
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                log.error("thread join error", e);
            }
        }
    }
}