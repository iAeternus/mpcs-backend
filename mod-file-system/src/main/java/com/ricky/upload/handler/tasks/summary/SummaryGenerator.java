package com.ricky.upload.handler.tasks.summary;

import java.io.IOException;
import java.io.InputStream;

public interface SummaryGenerator {

    String generate(InputStream inputStream);

}
