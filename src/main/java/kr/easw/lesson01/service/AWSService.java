package kr.easw.lesson01.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import kr.easw.lesson01.model.dto.AWSKeyDto;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

@Service
public class AWSService {
    private static final String BUCKET_NAME = "easw-random-bucket-" + UUID.randomUUID();
    private AmazonS3 s3Client = null;

    public void initAWSAPI(AWSKeyDto awsKey) {
        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsKey.getApiKey(), awsKey.getApiSecretKey())))
                .withRegion(Regions.US_EAST_1)
                .build();
        for (Bucket bucket : s3Client.listBuckets()) {
            if (bucket.getName().startsWith("easw-random-bucket-")){
                deleteS3Bucket(bucket.getName());
            }
        }
        s3Client.createBucket(BUCKET_NAME);
    }

    // Delete the s3 bucket function for unversioned buckets.
    // After deleting all of the object from s3 bucket, finally delete the s3 bucket.
    public void deleteS3Bucket(String bucketName) {
        try {
            ObjectListing objectListing = s3Client.listObjects(bucketName);
            for (S3ObjectSummary s3ObjectSummary : objectListing.getObjectSummaries()) {
                s3Client.deleteObject(bucketName, s3ObjectSummary.getKey());
            }
            s3Client.deleteBucket(bucketName);
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }

    // Convert S3ObjectInputStream to byte[] due to file transfer.
    public byte[] getS3Object(String key) {
        S3Object fullObject = null;

        System.out.println("Downloading an object");
        try {
            fullObject = s3Client.getObject(new GetObjectRequest(BUCKET_NAME, key));
            System.out.println("Content-Type: " + fullObject.getObjectMetadata().getContentType());

            return IOUtils.toByteArray(fullObject.getObjectContent());
        } catch (AmazonS3Exception | IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    public boolean isInitialized() {
        return s3Client != null;
    }

    public List<String> getFileList() {
        return s3Client.listObjects(BUCKET_NAME).getObjectSummaries().stream().map(S3ObjectSummary::getKey).toList();
    }

    @SneakyThrows
    public void upload(MultipartFile file) {
        s3Client.putObject(BUCKET_NAME, file.getOriginalFilename(), new ByteArrayInputStream(file.getResource().getContentAsByteArray()), new ObjectMetadata());
    }
}
