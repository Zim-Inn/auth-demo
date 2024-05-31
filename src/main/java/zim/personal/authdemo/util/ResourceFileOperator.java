package zim.personal.authdemo.util;

import lombok.Getter;
import zim.personal.authdemo.constant.ResponseCode;
import zim.personal.authdemo.exception.CustomRuntimeException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A utility class for reading files from the resources directory.
 */
@Getter
public class ResourceFileOperator {

    private final File resource;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();


    /**
     * Constructor to inject ResourceLoader.
     *
     * @param filePath the file path of the file to read
     */
    public ResourceFileOperator(String filePath) throws IOException {
        this.resource = new File(filePath);
        if (!this.resource.exists()){
            resource.getParentFile().mkdirs();
            resource.createNewFile();
        }

    }

    /**
     * Reads the content of a file located in the resources' directory.
     *
     * @return the content of the file as a String
     */
    public String readFileFromResources() {
        readWriteLock.readLock().lock();
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
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * @param content the content would be overwritten to the old file
     */
    public void writeResourceFile(String content) {
        writeResourceFile(content, () -> {});
    }

    /**
     * @param content the content would be overwritten to the old file
     */
    public void writeResourceFile(String content, Runnable rollBack) {
        readWriteLock.writeLock().lock();
        try {
            Files.writeString(resource.toPath(), content);
        } catch (IOException e) {
            rollBack.run();
            throw new CustomRuntimeException("write file error!", ResponseCode.FILE_ERROR);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}

