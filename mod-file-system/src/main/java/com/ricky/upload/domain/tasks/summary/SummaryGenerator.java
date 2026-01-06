package com.ricky.upload.domain.tasks.summary;

import java.io.IOException;

public interface SummaryGenerator {

    String generate(String textFilePath) throws IOException;

}
