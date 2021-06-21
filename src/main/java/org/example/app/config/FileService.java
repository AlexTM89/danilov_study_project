package org.example.app.config;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileService {

    Logger logger = Logger.getLogger(FileService.class);

    public List<String> getFiles() {
        List<String> result = new ArrayList<>();

        File dir = Paths.get(System.getProperty("catalina.home"), "uploads").toFile();
        if (!dir.exists()) {
            return result;
        }

        for (File file: dir.listFiles()) {
            result.add(file.getAbsolutePath());
        }

        return result;

    }

    public void downloadFile(String fileName, HttpServletResponse response) {
        if (fileName.isEmpty()) {
            return;
        }
        // prepare response
        response.setStatus(HttpStatus.OK.value());
        MediaType mediaType = new MediaType(FilenameUtils.getExtension(fileName));
        response.setContentType(mediaType.getType());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,"attachment;filename=" + Paths.get(fileName).getFileName().toString());

        // load file to response
        try (OutputStream stream = new BufferedOutputStream(response.getOutputStream())) {
            stream.write(Files.readAllBytes(Paths.get(fileName)));
            response.flushBuffer();
        } catch (IOException e) {
            response.setStatus(500);
            logger.error(e.getMessage());
        }
    }
}
