package sit.us1.backend.utils;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.core.io.ByteArrayResource;
import sit.us1.backend.dtos.tasksDTO.ResourceFileDTO;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FilePreviewUtil {

    // Handle image preview
    public static ByteArrayResource generateImagePreview(ResourceFileDTO resourceFile, int width, int height) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(resourceFile.getFile().getInputStream())
                .size(width, height) // Resize image
                .outputFormat("jpeg") // Always return JPEG for consistency
                .toOutputStream(outputStream);

        return new ByteArrayResource(outputStream.toByteArray());
    }

    // Handle text/RTF preview
    public static ByteArrayResource generateTextPreview(ResourceFileDTO resourceFile) throws IOException {
        String textContent = new String(resourceFile.getFile().getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return new ByteArrayResource(textContent.getBytes(StandardCharsets.UTF_8));
    }

    // Handle PDF preview
    public static ByteArrayResource generatePdfPreview(ResourceFileDTO resourceFile, int width, int height) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (PDDocument document = PDDocument.load(resourceFile.getFile().getInputStream())) {
            PDFRenderer renderer = new PDFRenderer(document);

            // Render first page of the PDF
            BufferedImage originalImage = renderer.renderImageWithDPI(0, 300); // High DPI for better quality

            // Resize the image
            Thumbnails.of(originalImage)
                    .size(width > 0 ? width : originalImage.getWidth(),
                            height > 0 ? height : originalImage.getHeight())
                    .outputFormat("jpeg")
                    .toOutputStream(outputStream);

            return new ByteArrayResource(outputStream.toByteArray());
        }
    }

    // Default behavior for unsupported files
    public static ByteArrayResource handleUnsupportedFile(ResourceFileDTO resourceFile) throws IOException {
        return new ByteArrayResource(resourceFile.getFile().getInputStream().readAllBytes());
    }
}

