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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Check if the file safe utils
 *
 * @author : xiaonan.fhn
 * @date 2023/06/27
 */
public class SafeFileUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(SafeFileUtils.class);

    /**
     * Check the filePath whether exist in the whitelist
     * The argument is util of the file path
     *
     * @param filePath      file path
     * @param whitelistPath while list of path
     * @return whether exist in the whitelist
     */
    public static boolean checkPathInWhitelist(String filePath, List<String> whitelistPath) {
        try {
            File file = new File(filePath);
            return checkPathInWhitelist(file, whitelistPath);
        } catch (Exception e) {
            LOGGER.error("FilepathTraversalChecker checkPathTraversal CatchException, " +
                    "filePath = {} Check path traversal catch exception, not deny!", filePath);
            return true;
        }
    }

    /**
     * Check the filePath whether exist in the whitelist
     * The argument is util of the file path
     *
     * @param file          file path
     * @param whitelistPath while list of path
     * @return whether exist in the whitelist
     */
    public static boolean checkPathInWhitelist(File file, List<String> whitelistPath) {
        try {
            if (file == null) {
                LOGGER.error("Target file path is null, need to be deny!");
                return false;
            }
            String canonicalPath = file.getCanonicalPath();
            if ("".equals(canonicalPath)) {
                LOGGER.error("Target canonical file path is null, need to be deny!");
                return false;
            }
            if (whitelistPath == null) {
                LOGGER.error("White path list is null, using error, need to be deny!");
                return false;
            }
            for (String whitePath : whitelistPath) {
                if (canonicalPath.startsWith(whitePath)) {
                    return true;
                }
            }
            LOGGER.error("Target canonical file path not in white list, need to deny!");
            return false;
        } catch (Exception e) {
            LOGGER.error("Check path traversal catch exception, not deny!");
            return true;
        }
    }
}
