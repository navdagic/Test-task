package org.avda.testtask.service;

import org.avda.testtask.datasrouce.FileDataSource;
import org.avda.testtask.model.FileDetails;
import org.avda.testtask.service.arithmetic.ArithmeticCompress;
import org.avda.testtask.service.arithmetic.ArithmeticDecompress;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.zip.CRC32;
import java.util.zip.Checksum;



@Service
public class  FileService {

    public static final String TXT_DIRECTORY = System.getProperty("user.dir") + "/src/main/resources/txtDirectory";
    public static final String AVDA_DIRECTORY = System.getProperty("user.dir") + "/src/main/resources/avdaDirectory";
    private final FileDataSource fileDataSource = new FileDataSource();

    public Resource downloadFile(String filename) throws IOException {
        return fileDataSource.downloadFile(filename);
    }

    public void uploadFile(MultipartFile file) throws IOException {
        FileDetails fileDetails;
        final String extension = fileType(file.getOriginalFilename());
        if(extension.equals("txt")){ // compression
            fileDetails = new FileDetails(true, file);
            fileDataSource.saveTxt(file); // prvo spasi txt

            compressAndCrcAndSave(fileDetails);
        }
        else if(extension.equals("avda")){ // decompression
            fileDetails = new FileDetails(false, file);
            fileDataSource.saveAvda(file); // prvo spasi avda

            decompressAndCrcAndSave(fileDetails);
        }
        else throw new IOException("Extension is not .txt or .avda !");
    }

    private void decompressAndCrcAndSave(FileDetails fileDetails) throws IOException {
        // *** ARITHMETIC DECOMPRESSION *** //
        byte[] sentCrc = ArithmeticDecompress.run(AVDA_DIRECTORY + File.separator + StringUtils.cleanPath(fileDetails.getMultipartFile().getOriginalFilename()),
                TXT_DIRECTORY + File.separator + StringUtils.cleanPath(getFileNameNew(fileDetails.getMultipartFile().getOriginalFilename())));
        // *** ARITHMETIC DECOMPRESSION *** //


        // ** crc check ** //
        fileDetails.setCrc32(bytesToLong(sentCrc));


        System.out.println("Procitani CRC: " + fileDetails.getCrc32());

        long newCrc = calculateCRC32(TXT_DIRECTORY + File.separator + StringUtils.cleanPath(getFileNameNew(fileDetails.getMultipartFile().getOriginalFilename())));
        System.out.println("Izracunati CRC: " + newCrc);

        if(newCrc != fileDetails.getCrc32()){
            throw new IllegalArgumentException("Crc do not match!");
        }
        // ** crc check ** //
    }

    private long bytesToLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    private void compressAndCrcAndSave(FileDetails fileDetails) throws IOException {
        //calculate crc32//
        // get bytes from file
        byte[] bytes = fileDetails.getMultipartFile().getBytes();

        Checksum checksum = new CRC32();

        // update the current checksum with the specified array of bytes
        checksum.update(bytes, 0, bytes.length);

        // get the current checksum value
        final long checksumValue = checksum.getValue();

        fileDetails.setCrc32(checksumValue);
        //calculate crc32//
        // *** ARITHMETIC CODING *** //
        ArithmeticCompress.run(TXT_DIRECTORY + File.separator + StringUtils.cleanPath(fileDetails.getMultipartFile().getOriginalFilename()),
                AVDA_DIRECTORY + File.separator + StringUtils.cleanPath(getFileNameNew(fileDetails.getMultipartFile().getOriginalFilename())),
                longToBytes(fileDetails.getCrc32()));
        // *** ARITHMETIC CODING *** //
    }

    private String getFileNameNew(String oldName){
        final String extension = fileType(oldName);
        if(extension.equals("txt")) // return avda
            return oldName.replace(".txt", ".avda");
        else // return txt
            return oldName.replace(".avda", ".txt");
    }

    private byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    private long calculateCRC32(String filePath) throws IOException {
        boolean failure = false;
        InputStream inputStream = null;
        CRC32 crc32 = new CRC32();
        int nextByte = 0;
        try {
            inputStream = new FileInputStream(filePath);
            while((nextByte = inputStream.read()) != -1){
                crc32.update(nextByte);
            }
        }
        catch (IOException e){
            failure = true;
            System.err.println("Crc IOException: " + e.getMessage());
        }
        finally {
            if(inputStream != null){
                inputStream.close();
            }
        }
        return (failure) ? -1 : crc32.getValue();
    }

    public static String fileType(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("fileName must not be null!");
        }

        String extension = "";

        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            extension = fileName.substring(index + 1);
        }

        return extension;

    }
}
