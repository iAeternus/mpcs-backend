package com.ricky.upload.handler.tasks.extracttext.impl;

import com.ricky.upload.handler.tasks.extracttext.TextExtractor;

import java.io.*;

public class TxtExtractor implements TextExtractor {

    @Override
    public void extract(InputStream inputStream, String textFilePath) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuffer = new StringBuilder();
        String content;
        while ((content = bufferedReader.readLine()) != null) {
            stringBuffer.append(content);
        }
        bufferedReader.close();
        inputStreamReader.close();
        inputStream.close();

        File file = new File(textFilePath);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

        bufferedWriter.write(stringBuffer.toString());
        bufferedWriter.close();
    }
}
