package com.ricky.file.domain;

import com.ricky.common.domain.AggregateRoot;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Optional;

import static com.ricky.common.constants.ConfigConstant.FILE_COLLECTION;

@Getter
@Document(FILE_COLLECTION)
@TypeAlias(FILE_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class File extends AggregateRoot {

    private String ownerId;
    private String teamId; // 若属于团队则记录团队ID
    private String parentId; // 父文件夹ID，根目录也是文件夹
    private String filename;
    private MetaData metaData;
    private String path; // 存储路径
    private FileStatus status;

    // TODO

}
