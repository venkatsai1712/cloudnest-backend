package venkatsai.cloudnest.service;

import io.minio.*;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import venkatsai.cloudnest.entity.FileEntity;
import venkatsai.cloudnest.repository.FileRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileService {
    private final FileRepository fileRepository;
    private final MinioClient minioClient;
    @Autowired
    public FileService(FileRepository fileRepository, MinioClient minioClient){
        this.fileRepository = fileRepository;
        this.minioClient = minioClient;
    }

    public FileEntity uploadFile(MultipartFile file) throws IOException, MinioException {
        if(file.getSize() > 10240){
            throw new FileUploadException("File Size is Greater than 10KB");
        }
        if(isFileExists(file)){
            throw new FileAlreadyExistsException("File Already Exists");
        }

        String fileId = UUID.randomUUID() + "_" + file.getOriginalFilename();
        FileEntity fileEntity = buildEntity(file, fileId);


        minioClient.putObject(
                PutObjectArgs.builder()
                        .stream(file.getInputStream(), file.getSize(), -1L)
                        .bucket("uploads")
                        .object(fileId)
                        .contentType(file.getContentType())
                        .build()
        );

        fileRepository.save(fileEntity);
        return fileEntity;
    }


    public FileEntity buildEntity(MultipartFile file, String fileId){
        String path = "uploads";
        return FileEntity.builder()
                .id(fileId)
                .name(file.getOriginalFilename())
                .contentType(file.getContentType())
                .createdAt(LocalDateTime.now())
                .storagePath(path)
                .size(file.getSize()).build();
    }


    public boolean isFileExists(MultipartFile file){
        String name = file.getOriginalFilename();
        return fileRepository.existsByNameEqualsIgnoreCase(name);
    }


    public List<FileEntity> getFiles() throws FileNotFoundException {
        if(fileRepository.findAll().isEmpty()){
            throw new FileNotFoundException("Files Not Found");
        }
        return fileRepository.findAll();
    }


    public InputStream downloadFile(String id) throws IOException, MinioException {
        FileEntity file =  fileRepository.findById(id);
        if(file != null){
            String path = file.getStoragePath();
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(path)
                            .object(id)
                            .build()
            );
        }
        throw new FileNotFoundException("File Not Found");
    }


    public FileEntity deleteFile(String id) throws IOException, MinioException {
        FileEntity file = fileRepository.findById(id);
        if(file != null){
            String path = file.getStoragePath();

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(path)
                            .object(id)
                            .build()
            );

            fileRepository.delete(file);
            return file;
        }
        throw new FileNotFoundException("File Not Found");
    }


    public String getName(String id){
        FileEntity file =  fileRepository.findById(id);
        if(file != null){
            return file.getName();
        }
        return "File";
    }
}
