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

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * File utils
 *
 * @author yansi
 * @date 2023/5/4
 */
@Slf4j
public class FileUtils {

    /**
     *
     **/
    public final static String FILE_SEPARATOR = "/";

    /**
     * cert limit 10kb
     **/
    public final static long CERT_FILE_MAX_SIZE = 1 * 10 * 1024;

    /**
     * Load file from the classpath resources or filesystem
     *
     * @param filepath path of a file
     *                 Example:
     *                 1. classpath:./a.txt
     *                 2. file:./config/a.txt
     *                 3. ./config/a.txt
     * @return File
     * @throws FileNotFoundException
     */
    public static File readFile(String filepath) throws FileNotFoundException {
        File file = ResourceUtils.getFile(filepath);
        if (!file.exists()) {
            throw new FileNotFoundException(filepath);
        }
        return file;
    }

    /**
     * Load file from the classpath resources or filesystem and return string
     *
     * @param filepath
     * @return String
     * @throws IOException
     */
    public static String readFile2String(String filepath) throws IOException {
        return readFile2String(readFile(filepath));
    }

    /**
     * Load file from the file and return string
     *
     * @param file
     * @return String
     * @throws IOException
     */
    public static String readFile2String(File file) throws IOException {
        return FileCopyUtils.copyToString(new FileReader(file));
    }


    /*** create  */
    public static boolean createDirIfNotExist(String dir) {
        try {
            File f = new File(dir);
            if (!f.exists()) {
                return f.mkdirs();
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * write content to file(./config/ownerId)
     *
     * @param content content
     */
    public static void writeToFile(String content) {
        writeContentToFile(content, SystemConstants.USER_OWNER_ID_FILE);
    }

    /**
     * write content to file
     *
     * @param content content
     * @param path    path
     */
    public static void writeContentToFile(String content, String path) {
        Assert.notNull(content, "content is null");
        Assert.notNull(path, "path is null");
        try {
            Path p = Paths.get(path);
            if (!Files.exists(p.getParent())) {
                Files.createDirectories(p.getParent());
            }
            Files.write(p, content.getBytes());
        } catch (IOException e) {
            log.error("write content to file error, path:{}", path, e);
        }
    }

    /**
     * delete file
     *
     * @param filePath filePath
     */
    public static void delFile(String filePath) {
        try {
            Files.delete(Paths.get(filePath));
        } catch (IOException e) {
            log.error("delete file error, path:{}", filePath, e);
        }
    }

}
