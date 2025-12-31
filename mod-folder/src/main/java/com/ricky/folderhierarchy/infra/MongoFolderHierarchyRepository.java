package com.ricky.folderhierarchy.infra;

import com.ricky.common.exception.MyException;
import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.domain.FolderHierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import static com.ricky.common.exception.ErrorCodeEnum.FOLDER_HIERARCHY_NOT_FOUND;
import static com.ricky.common.utils.ValidationUtils.isNull;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class MongoFolderHierarchyRepository extends MongoBaseRepository<FolderHierarchy> implements FolderHierarchyRepository {

    @Override
    public FolderHierarchy byUserId(String userId) {
        requireNotBlank(userId, "User ID must not be blank.");

        Query query = query(where("userId").is(userId));
        FolderHierarchy hierarchy = mongoTemplate.findOne(query, FolderHierarchy.class);

        if (isNull(hierarchy)) {
            throw new MyException(FOLDER_HIERARCHY_NOT_FOUND, "未找到文件夹。", "userId", userId);
        }

        return hierarchy;
    }

    @Override
    public void save(FolderHierarchy folderHierarchy) {
        super.save(folderHierarchy);
    }
}
