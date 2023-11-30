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

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * Compress utils
 *
 * @author : xiaonan.fhn
 * @date 2023/07/01
 */
public class CompressUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(CompressUtils.class);

    private static final int BUFFER_SIZE = 1024 * 100;

    /**
     * Get files via path
     *
     * @param path
     * @return File list
     */
    public static List<File> getFiles(String path) {
        List<File> list = new LinkedList<File>();

        File file = new File(path);
        File[] tempList = file.listFiles();
        if (null != tempList && tempList.length > 0) {
            for (int i = 0; i < tempList.length; i++) {
                if (tempList[i].isFile()) {
                    list.add(new File(tempList[i].getPath()));
                }
                if (tempList[i].isDirectory()) {
                    List<File> tmpList = getFiles(tempList[i].getPath());
                    if (!CollectionUtils.isEmpty(tmpList)) {
                        list.addAll(tmpList);
                    }
                }
            }
        }
        return list;
    }

    /**
     * The private function returns the collection of files compressed into a tar package
     *
     * @param files  The collection of files to compress
     * @param target The target file for the output stream
     * @return File  Specifies the target file to return
     */
    public static File pack(List<File> files, String inPutPath, File target) throws IOException {
        try (FileOutputStream out = new FileOutputStream(target)) {
            try (BufferedOutputStream bos = new BufferedOutputStream(out, BUFFER_SIZE)) {
                try (TarArchiveOutputStream os = new TarArchiveOutputStream(bos)) {
                    // Solve the problem of file name too long
                    os.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
                    for (File file : files) {
                        // Remove the directory in front of the file
                        os.putArchiveEntry(new TarArchiveEntry(file, file.getAbsolutePath().replace(inPutPath, "")));
                        try (FileInputStream fis = new FileInputStream(file)) {
                            IOUtils.copy(fis, os);
                            os.closeArchiveEntry();
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
        return target;
    }

    /**
     * Compress source file list to target
     *
     * @param source
     * @param target
     * @param fileName
     * @throws Exception
     */
    public static void compress(String source, String target, String fileName) throws Exception {

        List<File> list = getFiles(source);
        if (CollectionUtils.isEmpty(list)) {
            LOGGER.error("source file is empty , please check [  {}  ]....", source);
            return;
        }
        File file = new File(target);
        if (!file.exists()) {
            file.mkdirs();
        }

        compressTar(list, source, target, fileName);


    }

    /**
     * Compress tar file
     *
     * @param list
     * @param outPutPath
     * @param fileName
     */
    public static File compressTar(List<File> list, String inPutPath, String outPutPath, String fileName) throws Exception {
        File outPutFile = new File(outPutPath + File.separator + fileName + ".tar.gz");
        File tempTar = new File("temp.tar");
        try (FileInputStream fis = new FileInputStream(pack(list, inPutPath, tempTar))) {
            try (BufferedInputStream bis = new BufferedInputStream(fis, BUFFER_SIZE)) {
                try (FileOutputStream fos = new FileOutputStream(outPutFile)) {
                    try (GZIPOutputStream gzp = new GZIPOutputStream(fos)) {
                        int count;
                        byte[] data = new byte[BUFFER_SIZE];
                        while ((count = bis.read(data, 0, BUFFER_SIZE)) != -1) {
                            gzp.write(data, 0, count);
                        }
                    }
                }
            }
        }

        try {
            Files.deleteIfExists(tempTar.toPath());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return outPutFile;
    }

    /**
     * Decompress file from filePath to outputDir
     *
     * @param filePath
     * @param outputDir
     * @return
     * @throws RuntimeException
     */
    public static boolean decompress(String filePath, String outputDir) {
        File file = new File(filePath);
        if (!file.exists()) {
            LOGGER.error("decompress file not exist.");
            return false;
        }
        try {
            if (filePath.endsWith(".zip")) {
                unZip(file, outputDir);
            }
            if (filePath.endsWith(".tar.gz") || filePath.endsWith(".tgz")) {
                decompressTarGz(file, outputDir);
            }
            if (filePath.endsWith(".tar.bz2")) {
                decompressTarBz2(file, outputDir);
            }

            return true;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Decompress zip file to outputDir
     *
     * @param file      zip file object to unzip
     * @param outputDir the target outputDir
     * @throws IOException
     */
    public static void unZip(File file, String outputDir) throws IOException {
        try (ZipFile zipFile = new ZipFile(file, StandardCharsets.UTF_8)) {
            // create output directory
            createDirectory(outputDir, null);
            Enumeration<?> enums = zipFile.entries();
            while (enums.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) enums.nextElement();
                if (entry.isDirectory()) {
                    // create an empty directory
                    createDirectory(outputDir, entry.getName());
                } else {
                    try (InputStream in = zipFile.getInputStream(entry)) {
                        try (OutputStream out = new FileOutputStream(
                                new File(outputDir + File.separator + entry.getName()))) {
                            writeFile(in, out);
                        }
                    }
                }
            }
        }
    }

    /**
     * Decompress tar file to outputDir
     *
     * @param file
     * @param outputDir
     * @throws IOException
     */
    public static void decompressTarGz(File file, String outputDir) throws IOException {
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(
                new GzipCompressorInputStream(
                        new BufferedInputStream(
                                new FileInputStream(file))))) {
            // create output directory
            createDirectory(outputDir, null);
            TarArchiveEntry entry = null;
            while ((entry = tarIn.getNextTarEntry()) != null) {
                // the tar entry type is directory
                if (entry.isDirectory()) {
                    // create an empty directory
                    createDirectory(outputDir, entry.getName());
                } else {
                    // the tar entry type is file
                    try (OutputStream out = new FileOutputStream(
                            new File(outputDir + File.separator + entry.getName()))) {
                        writeFile(tarIn, out);
                    }
                }
            }
        }

    }

    /**
     * Decompress tar.bz2 file to outputDir
     *
     * @param file      zip file object to unzip
     * @param outputDir the target outputDir
     */
    public static void decompressTarBz2(File file, String outputDir) throws IOException {
        try (TarArchiveInputStream tarIn =
                     new TarArchiveInputStream(
                             new BZip2CompressorInputStream(
                                     new FileInputStream(file)))) {
            createDirectory(outputDir, null);
            TarArchiveEntry entry;
            while ((entry = tarIn.getNextTarEntry()) != null) {
                if (entry.isDirectory()) {
                    createDirectory(outputDir, entry.getName());
                } else {
                    try (OutputStream out = new FileOutputStream(
                            new File(outputDir + File.separator + entry.getName()))) {
                        writeFile(tarIn, out);
                    }
                }
            }
        }
    }

    /**
     * Write file from inputStream to outputStream
     *
     * @param in
     * @param out
     * @throws IOException
     */
    public static void writeFile(InputStream in, OutputStream out) throws IOException {
        int length;
        byte[] b = new byte[BUFFER_SIZE];
        while ((length = in.read(b)) != -1) {
            out.write(b, 0, length);
        }
    }

    /**
     * Create directory
     *
     * @param outputDir
     * @param subDir
     */
    public static void createDirectory(String outputDir, String subDir) {
        File file = new File(outputDir);
        // the subdirectory is not empty
        if (!(subDir == null || "".equals(subDir.trim()))) {
            file = new File(outputDir + File.separator + subDir);
        }
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.mkdirs();
        }
    }


}
