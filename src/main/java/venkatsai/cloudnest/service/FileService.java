package venkatsai.cloudnest.service;

import io.minio.*;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import venkatsai.cloudnest.dto.APIResponseDTO;
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

    public APIResponseDTO<FileEntity> uploadFiles(MultipartFile file) throws IOException, MinioException {
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
        return APIResponseDTO.<FileEntity>builder()
                .data(fileEntity)
                .status(201).message("File Created Successfully").timeStamp(LocalDateTime.now()).build();
    }


    public FileEntity buildEntity(MultipartFile file, String fileId){
        String path = "uploads";
        return FileEntity.builder()
                .id(fileId)
                .fileName(file.getOriginalFilename())
                .mime(file.getContentType())
                .createdAt(LocalDateTime.now())
                .storagePath(path)
                .size(file.getSize()).build();
    }


    public boolean isFileExists(MultipartFile file){
        String name = file.getOriginalFilename();
        return fileRepository.existsByFileNameEqualsIgnoreCase(name);
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


    public APIResponseDTO<FileEntity> deleteFile(String id) throws IOException, MinioException {
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
            return APIResponseDTO.<FileEntity>builder()
                    .message(" File Deleted Successfully")
                    .status(200)
                    .timeStamp(LocalDateTime.now())
                    .data(file).build();
        }
        throw new FileNotFoundException("File Not Found");
    }


    public String getFileName(String id){
        FileEntity file =  fileRepository.findById(id);
        if(file != null){
            return file.getFileName();
        }
        return "File";
    }
}
