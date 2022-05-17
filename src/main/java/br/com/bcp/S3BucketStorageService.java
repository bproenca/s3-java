package br.com.bcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.http.Method;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Item;

@Service
public class S3BucketStorageService {

    private Logger logger = LoggerFactory.getLogger(S3BucketStorageService.class);

    @Autowired
    private MinioClient minioClient;

    @Value("${application.bucket.name}")
    private String bucketName;

    public String createPreAuthReqToGet() {
        // Get presigned URL string to download 'my-objectname' in 'my-bucketname' and its life time
        // is 2 hours. >> FUNCIONOU
        String url = "Failed";
        try {
            url =
               minioClient.getPresignedObjectUrl(
                   GetPresignedObjectUrlArgs.builder()
                       .method(Method.GET)
                       .bucket(bucketName)
                       .object("tb131.txt")
                       .expiry(2, TimeUnit.HOURS)
                       .build());
        } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | XmlParserException | ServerException
                | IllegalArgumentException | IOException e) {
            logger.error("Failed to Create Pre-Authenticated Request ", e);
        }
        return url;
    }

    public String createPreAuthReqToPut() {
        // Get presigned URL string to upload 'my-objectname' in 'my-bucketname'
        // with response-content-type as application/json and life time as one day.
        Map<String, String> reqParams = new HashMap<String, String>();
        reqParams.put("response-content-type", "application/json");
        String url = "Failed";
        try {
            url =
               minioClient.getPresignedObjectUrl(
                   GetPresignedObjectUrlArgs.builder()
                       .method(Method.PUT)
                       .bucket(bucketName)
                       .object("tb131.txt")
                       .expiry(2, TimeUnit.HOURS)
                       .extraQueryParams(reqParams)
                       .build());
            System.out.println(url);
        } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | XmlParserException | ServerException
                | IllegalArgumentException | IOException e) {
            logger.error("Failed to Create Pre-Authenticated Request ", e);
        }
        return url;
    }

    /**
     * Upload file into AWS S3
     *
     * @param fileName
     * @param file
     * @return String
     */
    public String uploadFile(String fileName, MultipartFile file) {
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());
        } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
                | IllegalArgumentException | IOException e) {
            logger.error("MinioClient error: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return "File uploaded (minio): " + fileName;
    }

    /**
     * Deletes file from AWS S3 bucket
     *
     * @param fileName
     * @return
     */
    public String deleteFile(final String fileName) {
        // Remove object.
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
            return "Deleted File: " + fileName;
        } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
                | IllegalArgumentException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Downloads file using amazon S3 client from S3 bucket
     *
     * @param fileName
     * @return ByteArrayOutputStream
     */
    public ByteArrayOutputStream downloadFile(String fileName) {
        try {
            InputStream is = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int len;
            byte[] buffer = new byte[16384];
            while ((len = is.read(buffer, 0, buffer.length)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            is.close();
            return outputStream;
        } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
                | IllegalArgumentException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get all files from S3 bucket
     *
     * @return
     */
    public List<String> listFiles() {
        // Lists objects information recursively.
        Iterable<Result<Item>> results = minioClient.listObjects(
            ListObjectsArgs.builder().bucket(bucketName).recursive(true).build());
        
        List<String> keys = new ArrayList<>();
        try {
            for (Result<Item> result : results) {
                keys.add(result.get().objectName());
            }
        } catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException | InsufficientDataException
                | InternalException | InvalidResponseException | NoSuchAlgorithmException | ServerException
                | XmlParserException | IOException e) {
            throw new RuntimeException(e);
        }    
        return keys;
    }

}
