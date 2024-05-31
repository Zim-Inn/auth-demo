package zim.personal.authdemo.util;

import lombok.Getter;
import zim.personal.authdemo.constant.ResponseCode;
import zim.personal.authdemo.exception.CustomRuntimeException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * A utility class for reading files from the resources directory.
 */
@Getter
public class ResourceFileOperator {

    private final File resource;


    /**
     * Constructor to inject ResourceLoader.
     *
     * @param filePath the file path of the file to read
     */
    public ResourceFileOperator(String filePath) {
        this.resource = new File(filePath);
    }

    /**
     * Reads the content of a file located in the resources' directory.
     *
     * @return the content of the file as a String
     */
    public String readFileFromResources() {
        try {
            if (!resource.exists()) {
                throw new CustomRuntimeException("File not found: " + resource.getAbsoluteFile(), ResponseCode.FILE_ERROR);
            }
            if (resource.isDirectory()) {
                throw new CustomRuntimeException("Required a dir but found a file: " + resource.getAbsoluteFile(), ResponseCode.FILE_ERROR);
            }
            return Files.readString(resource.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CustomRuntimeException("read file error!", ResponseCode.FILE_ERROR);
        }
    }

    /**
     * @param content the content would be overwritten to the old file
     */
    public void writeResourceFile(String content) {
        try {
            Files.writeString(resource.toPath(), content, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new CustomRuntimeException("write file error!", ResponseCode.FILE_ERROR);
        }
    }

    // Example usage
    public static void main(String[] args) {
        ResourceFileOperator reader = new ResourceFileOperator("src/main/resource/data/userData.json"); // Replace with actual ResourceLoader
        String content = reader.readFileFromResources();
        System.out.println(content);
    }
}

