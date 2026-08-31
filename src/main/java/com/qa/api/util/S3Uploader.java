package com.qa.api.util;

import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class S3Uploader {

    public static void uploadFileToS3(String presignedUrl, String filePath) throws IOException {
        uploadFileToS3(presignedUrl, filePath, "application/octet-stream");
    }

    public static void uploadFileToS3(String presignedUrl, String filePath, String contentType) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        byte[] fileBytes = Files.readAllBytes(file.toPath());

        URL url = new URL(presignedUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", contentType);
        connection.setFixedLengthStreamingMode(fileBytes.length);

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(fileBytes);
        }

        int responseCode = connection.getResponseCode();
        Assert.assertTrue(responseCode == 200 || responseCode == 204,
                "Upload failed. Response code: " + responseCode);

        connection.disconnect();
    }
}
