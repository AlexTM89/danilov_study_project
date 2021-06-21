package org.example.app.exceptions;

public class UploadNullFileException extends Exception {

    private final String message;
    public UploadNullFileException(String cannot_upload_empty_file) {
        this.message = cannot_upload_empty_file;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
