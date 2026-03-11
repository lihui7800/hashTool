package com.enss.sha256tool;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class Sha256Util {

    /**
     * Computes the SHA-256 hash of a file using streaming I/O.
     * The file is never fully loaded into memory, so files of arbitrary size are supported.
     *
     * @param file the target file
     * @return 64-character lowercase hexadecimal string
     */
    public static String sha256Hex(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream in = Files.newInputStream(file.toPath());
             DigestInputStream dis = new DigestInputStream(in, digest)) {
            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) { /* stream through file to update digest */ }
        }
        byte[] hash = digest.digest();
        StringBuilder sb = new StringBuilder(64);
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
