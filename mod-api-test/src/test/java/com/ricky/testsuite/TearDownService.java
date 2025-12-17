package com.ricky.testsuite;

import com.ricky.file.domain.StorageId;
import com.ricky.file.infra.impl.GridFsFileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TearDownService {

    @Autowired
    private GridFsFileStorage gridFsFileStorage;

    public void deleteFileFromGridFs(StorageId storageId) {
        gridFsFileStorage.delete(storageId);
    }

}
