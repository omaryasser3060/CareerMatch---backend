package com.example.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PDFExtractionService {

    private final FileStorageService fileStorageService;

    /**
     * Extract text from a PDF file.
     *
     * @param filePath the path to the PDF file
     * @return the extracted text
     * @throws IOException if the file cannot be read
     */
    public String extractText(String filePath) throws IOException {
        log.debug("Extracting text from PDF: {}", filePath);

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            log.debug("Extracted {} characters from PDF: {}", text.length(), filePath);
            return text.trim();
        } catch (IOException e) {
            log.error("Failed to extract text from PDF: {}", filePath, e);
            throw e;
        }
    }

    /**
     * Extract text from a PDF byte array.
     *
     * @param pdfBytes the PDF content
     * @return the extracted text
     * @throws IOException if the PDF cannot be read
     */
    public String extractText(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document).trim();
        }
    }
}