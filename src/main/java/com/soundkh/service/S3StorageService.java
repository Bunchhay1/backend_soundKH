package com.soundkh.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;

@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${minio.bucket.audio-raw}")     private String audioRawBucket;
    @Value("${minio.bucket.public-assets}") private String publicAssetsBucket;

    public S3StorageService(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    public String uploadAudio(String key, InputStream in, long length) {
        s3Client.putObject(PutObjectRequest.builder().bucket(audioRawBucket).key(key).build(),
                RequestBody.fromInputStream(in, length));
        return key;
    }

    public String uploadPublicAsset(String key, InputStream in, long length) {
        s3Client.putObject(PutObjectRequest.builder().bucket(publicAssetsBucket).key(key).build(),
                RequestBody.fromInputStream(in, length));
        return key;
    }

    public InputStream downloadAudio(String key) {
        return s3Client.getObject(GetObjectRequest.builder().bucket(audioRawBucket).key(key).build());
    }

    public ResponseInputStream<GetObjectResponse> downloadRange(String key, String range) {
        return s3Client.getObject(GetObjectRequest.builder()
                .bucket(audioRawBucket).key(key).range(range).build());
    }

    public long getObjectSize(String key) {
        return s3Client.headObject(HeadObjectRequest.builder()
                .bucket(audioRawBucket).key(key).build()).contentLength();
    }

    public String presign(String key, Duration ttl) {
        var getRequest = GetObjectRequest.builder()
                .bucket(audioRawBucket)
                .key(key)
                .build();
        var presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .getObjectRequest(getRequest)
                .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    public void deleteAudio(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(audioRawBucket).key(key).build());
    }
}
