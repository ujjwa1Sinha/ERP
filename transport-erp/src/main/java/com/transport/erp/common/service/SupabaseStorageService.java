package com.transport.erp.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

    private final S3Client s3Client;

    @Value("${supabase.s3.bucket:documents}")
    private String bucketName;

    @Value("${supabase.url:https://dummy.supabase.co}")
    private String supabaseUrl;

    /**
     * Uploads a file to Supabase Storage and returns the public URL.
     */
    public String uploadFile(MultipartFile file, String folder) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String storedFilename = folder + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storedFilename)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Construct and return the public URL to access this file later
            // Format:
            // https://[project_id].supabase.co/storage/v1/object/public/[bucket_name]/[file_path]
            return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + storedFilename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read file for upload to Supabase", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to Supabase S3 bucket", e);
        }
    }
}
