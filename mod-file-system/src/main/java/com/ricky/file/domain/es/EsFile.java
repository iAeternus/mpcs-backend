package com.ricky.file.domain.es;

import com.ricky.common.domain.marker.Identified;
import com.ricky.common.exception.MyException;
import com.ricky.file.domain.FileCategory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

import static com.ricky.common.constants.ConfigConstants.FILE_ES_INDEX_NAME;
import static com.ricky.common.exception.ErrorCodeEnum.ES_FILE_INVALID;
import static com.ricky.common.utils.ValidationUtils.isBlank;
import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;
import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@Getter
@Document(indexName = FILE_ES_INDEX_NAME)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EsFile implements Identified {

    /**
     * 文件ID
     */
    @Id
    @Field(type = Keyword)
    private String id;

    /**
     * 文件名
     */
    @Field(type = Text, analyzer = "ik_filename")
    private String name;

    /**
     * 文件分类字符串，来自 {@link FileCategory}
     */
    @Field(type = Keyword)
    private String category;

    /**
     * 文件摘要
     */
    @Field(type = Text, analyzer = "ik_smart")
    private String summary;

    /**
     * 关键词列表
     */
    @Field(type = Keyword)
    private List<String> keywords;

    @Field(type = FieldType.Long)
    private Long sizeInBytes;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private Date lastModified;

    public EsFile(
            String id,
            String name,
            String category,
            String summary,
            List<String> keywords,
            Long sizeInBytes,
            Date lastModified
    ) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.summary = summary;
        this.keywords = keywords;
        this.sizeInBytes = sizeInBytes;
        this.lastModified = lastModified;
    }

    public void validate() {
        if (isBlank(id)) {
            throw new MyException(ES_FILE_INVALID, "文件ID不能为空");
        }
        if (isBlank(name)) {
            throw new MyException(ES_FILE_INVALID, "文件名不能为空");
        }
    }

}
