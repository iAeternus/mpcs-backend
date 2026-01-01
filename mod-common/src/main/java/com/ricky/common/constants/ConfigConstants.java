package com.ricky.common.constants;

/**
 * @author Ricky
 * @version 1.0
 * @date 2025/2/26
 * @className ConfigConstant
 * @desc 整个系统需要的配置常数，统一配置避免出现不匹配的情况
 */
public interface ConfigConstants {

    String CHINA_TIME_ZONE = "Asia/Shanghai";

    String AUTHORIZATION = "Authorization";
    String BEARER = "Bearer ";
    String AUTH_COOKIE_NAME = "mpcstoken";
    String NO_USER_ID = "NO_USER_ID";

    String LOCAL_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    String USER_COLLECTION = "users";
    String FILE_COLLECTION = "files";
    String FOLDER_COLLECTION = "folders";
    String FOLDER_HIERARCHY_COLLECTION = "folders_hierarchy";
    String COMMUNITY_POST_COLLECTION = "community_posts";
    String TEAM_COLLECTION = "teams";
    String SHEDLOCK_COLLECTION = "shedlock";
    String VERIFICATION_COLLECTION = "verifications";
    String UPLOAD_SESSION_COLLECTION = "upload_sessions";
    String SENSITIVE_WORD_COLLECTION = "sensitive_words";

    String PUBLISHING_DOMAIN_EVENT_COLLECTION = "publishing_domain_event";
    String CONSUMING_DOMAIN_EVENT_COLLECTION = "consuming_domain_event";

    String USER_ID_PREFIX = "USR";
    String FILE_ID_PREFIX = "FIL";
    String FOLDER_ID_PREFIX = "FLD";
    String FOLDER_HIERARCHY_ID_PREFIX = "FDH";
    String VERIFICATION_ID_PREFIX = "VRC";
    String UPLOAD_SESSION_ID_PREFIX = "UPL";
    String SENSITIVE_WORD_ID_PREFIX = "SST";

    String USER_CACHE = "USER";
    String FILE_CACHE = "FILE";
    String UPLOAD_SESSION_CACHE = "UPLOAD_SESSION";
    String HASH_STORAGES_CACHE = "HASH_STORAGES_CACHE";
    String USER_FOLDERS_CACHE = "USER_FOLDERS";
    String FOLDER_HIERARCHY_CACHE = "FOLDER_HIERARCHY";

    String REDIS_DOMAIN_EVENT_CONSUMER_GROUP = "domain.event.group";
    String REDIS_WEBHOOK_CONSUMER_GROUP = "webhook.group";
    String REDIS_NOTIFICATION_CONSUMER_GROUP = "notification.group";

    String[] AVATAR_TYPES = {"image/png", "image/jpeg", "image/gif"};

    int MAX_URL_LENGTH = 1024;
    int MAX_CASES_SIZE = 64;
    int MAX_ANSWER_COUNT = 64;
    int MAX_GENERIC_NAME_LENGTH = 50;
    int MAX_GENERIC_TEXT_LENGTH = 1024;
    int MAX_CODE_LENGTH = 65535;
    int MAX_DESC_LENGTH = 128;
    int MAX_PAGE_SIZE = 500;
    int MAX_COMMENT_HIERARCHY_LEVEL = 6;
    int MAX_FOLDER_HIERARCHY_LEVEL = 5;

    int MIN_PAGE_INDEX = 1;
    int MAX_PAGE_INDEX = 10000;
    int MIN_PAGE_SIZE = 10;

}
