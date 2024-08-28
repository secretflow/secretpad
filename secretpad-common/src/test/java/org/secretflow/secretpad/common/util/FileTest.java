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

import org.secretflow.secretpad.common.constant.SystemConstants;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * File test
 *
 * @author yansi
 * @date 2023/5/9
 */
public class FileTest {
    @Test
    public void testLoadFile() throws FileNotFoundException {
        String[] filenames = {"classpath:./a/a.txt"};
        for (String filename : filenames) {
            FileUtils.readFile(filename);
        }
    }

    @Test
    public void testWriteToFile() throws IOException {
        Assertions.assertDoesNotThrow(() -> FileUtils.delFile(SystemConstants.USER_OWNER_ID_FILE));
        Assertions.assertDoesNotThrow(() -> FileUtils.writeToFile("test1"));
        Assertions.assertEquals("test1", FileUtils.readFile2String(SystemConstants.USER_OWNER_ID_FILE));
        Assertions.assertThrows(IllegalArgumentException.class, () -> FileUtils.writeToFile(null));
        Assertions.assertDoesNotThrow(() -> FileUtils.writeToFile("test2"));
        Assertions.assertEquals("test2", FileUtils.readFile2String(SystemConstants.USER_OWNER_ID_FILE));
        FileUtils.delFile(SystemConstants.USER_OWNER_ID_FILE);
        FileUtils.delFile(Path.of(SystemConstants.USER_OWNER_ID_FILE).getParent().toString());
    }
}
