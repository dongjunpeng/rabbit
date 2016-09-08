package com.buterfleoge.rabbit.view;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.view.AbstractView;

/**
 * @author xiezhenzong
 *
 */
public class PdfView extends AbstractView {

    private static final String CLASS_PATH = "classpath:";
    private static final int CLASS_PATH_LENGHT = CLASS_PATH.length();

    public static final String PATH_KEY = "pdfPath";

    public PdfView() {
        setContentType("application/pdf");
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        InputStream inputStream = getPdfFileInputStream(model);
        ByteArrayOutputStream baos = createTemporaryOutputStream();

        try {
            byte[] buffer = new byte[1024];
            int bytesRead = -1;
            // write bytes read from the input stream into the output stream
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
        } finally {
            inputStream.close();
        }

        // Flush to HTTP response.
        writeToResponse(response, baos);
    }

    private InputStream getPdfFileInputStream(Map<String, Object> model) throws IOException {
        String pdfPath = (String) model.get(PATH_KEY);
        Resource resource = null;
        if (pdfPath.startsWith(CLASS_PATH)) {
            resource = new ClassPathResource(pdfPath.substring(CLASS_PATH_LENGHT));
        } else {
            resource = new FileSystemResource(pdfPath);
        }

        InputStream inputStream = resource.getInputStream();
        return inputStream;
    }

}
