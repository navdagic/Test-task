package org.avda.testtask.service.arithmetic;

import java.io.*;


/**
 * Compression application using static arithmetic coding.
 * <p>Usage: java ArithmeticCompress InputFile OutputFile</p>
 * <p>Then use the corresponding "ArithmeticDecompress" application to recreate the original input file.</p>
 * <p>Note that the application uses an alphabet of 257 symbols - 256 symbols for the byte
 * values and 1 symbol for the EOF marker. The compressed file format starts with a list
 * of 256 symbol frequencies, and then followed by the arithmetic-coded data.</p>
 */
public class ArithmeticCompress {

    public static void run(String inFilePath, String outFilePath, byte[] crcBytes) throws IOException {

        if(crcBytes.length != 8){
            System.err.println("Invalid crc poslan");
            return;
        }

        File inputFile  = new File(inFilePath);
        File outputFile = new File(outFilePath);

        // Read input file once to compute symbol frequencies
        FrequencyTable freqs = getFrequencies(inputFile);
        freqs.increment(256);  // EOF symbol gets a frequency of 1

        InputStream in = null;
        OutputStream outStream = null;
        BitOutputStream out = null;

        try{
            outStream = new FileOutputStream(outputFile);
            outStream.write(crcBytes); // write crc

            // Read input file again, compress with arithmetic coding, and write output file
            in = new BufferedInputStream(new FileInputStream(inputFile));
            out = new BitOutputStream(new BufferedOutputStream(outStream));
            writeFrequencies(out, freqs);
            compress(freqs, in, out);
        }
        catch (IOException e){
            System.err.println("Greska u kompresiji");
        }
        finally{
            if(in != null){
                in.close();
            }
            if(out != null){
                out.close();
            }
        }
    }


    // Returns a frequency table based on the bytes in the given file.
    // Also contains an extra entry for symbol 256, whose frequency is set to 0.
    private static FrequencyTable getFrequencies(File file) throws IOException {
        FrequencyTable freqs = new SimpleFrequencyTable(new int[257]);
        try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
            while (true) {
                int b = input.read();
                if (b == -1)
                    break;
                freqs.increment(b);
            }
        }
        return freqs;
    }


    private static void writeFrequencies(BitOutputStream out, FrequencyTable freqs) throws IOException {
        for (int i = 0; i < 256; i++)
            writeInt(out, 32, freqs.get(i));
    }


    private static void compress(FrequencyTable freqs, InputStream in, BitOutputStream out) throws IOException {
        ArithmeticEncoder enc = new ArithmeticEncoder(32, out);
        while (true) {
            int symbol = in.read();
            if (symbol == -1)
                break;
            enc.write(freqs, symbol);
        }
        enc.write(freqs, 256);  // EOF
        enc.finish();  // Flush remaining code bits
    }


    // Writes an unsigned integer of the given bit width to the given stream.
    private static void writeInt(BitOutputStream out, int numBits, int value) throws IOException {
        if (numBits < 0 || numBits > 32)
            throw new IllegalArgumentException();

        for (int i = numBits - 1; i >= 0; i--)
            out.write((value >>> i) & 1);  // Big endian
    }

}