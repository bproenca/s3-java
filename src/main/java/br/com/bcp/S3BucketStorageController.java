package br.com.bcp;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.amazonaws.services.s3.model.AmazonS3Exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class S3BucketStorageController {
    @Autowired
    S3BucketStorageService service;

    @GetMapping("/list/files")
    public ResponseEntity<List<String>> getListOfFiles() {
        return new ResponseEntity<>(service.listFiles(), HttpStatus.OK);
    }

    @PostMapping("/file/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("fileName") String fileName,
                                             @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(service.uploadFile(fileName, file), HttpStatus.OK);
    }

    @GetMapping(value = "/download/{filename}")
    public ResponseEntity<byte[]> download(@PathVariable String filename) {
        ByteArrayOutputStream downloadInputStream = service.downloadFile(filename);

        return ResponseEntity.ok()
                .contentType(contentType(filename))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(downloadInputStream.toByteArray());
    }

    @GetMapping(value = "/downloadfile/**")
    public ResponseEntity<byte[]> downloadFile(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String filename = requestURI.replace("/downloadfile/", "");
        try {
            ByteArrayOutputStream downloadInputStream = service.downloadFile(filename);
            
            return ResponseEntity.ok()
                    .contentType(contentType(filename))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(downloadInputStream.toByteArray());
        } catch (AmazonS3Exception exp) {
            if (exp != null && exp.getStatusCode() == 403) {
                throw new AccessDenied(filename);
            } else {
                throw exp;                
            }
        }

    }

    @GetMapping(value = "/delete/{filename}")
    public ResponseEntity<String> delete(@PathVariable("filename") String filename) {
        return new ResponseEntity<>(service.deleteFile(filename), HttpStatus.OK);
    }

    @DeleteMapping(value = "/deletefile/**")
    public ResponseEntity<String> deleteFile(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String filename = requestURI.replace("/deletefile/", "");
        return new ResponseEntity<>(service.deleteFile(filename), HttpStatus.OK);
    }

    private MediaType contentType(String filename) {
        String[] fileArrSplit = filename.split("\\.");
        String fileExtension = fileArrSplit[fileArrSplit.length - 1];
        switch (fileExtension) {
            case "txt":
                return MediaType.TEXT_PLAIN;
            case "png":
                return MediaType.IMAGE_PNG;
            case "jpg":
                return MediaType.IMAGE_JPEG;
            case "pdf":
                return MediaType.APPLICATION_PDF;
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
