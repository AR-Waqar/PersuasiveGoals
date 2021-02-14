package com.example.goals;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class UploadRequestBody extends RequestBody {

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    private File file;
    private String contentType;
    private UploadCallback uploadCallback;

    public UploadRequestBody(File file, String contentType, UploadCallback uploadCallback) {
        this.file = file;
        this.contentType = contentType;
        this.uploadCallback = uploadCallback;
    }


    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType + "/*");
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = file.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream in = new FileInputStream(file);
        long uploaded = 0;
        try {
            int read;
            while ((read = in.read(buffer)) != -1) {
                uploaded += read;
                sink.write(buffer, 0, read);
            }
        } finally {
            in.close();
        }
    }

    @Override
    public long contentLength() throws IOException {
        return file.length();
    }

    interface UploadCallback {
        void onProgressUpdate(int percentage);
    }
}


