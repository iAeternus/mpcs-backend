package com.ricky.common.constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.time.format.DateTimeFormatter;

import static java.time.ZoneId.systemDefault;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.CHINA;

/**
 * @author Ricky
 * @version 1.0
 * @date 2025/2/26
 * @className ConfigConstant
 * @desc 整个系统需要的配置常数，统一配置避免出现不匹配的情况
 */
public interface ConfigConstants {

    Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    String AUTHORIZATION = "Authorization";
    String BEARER = "Bearer ";
    String AUTH_COOKIE_NAME = "mpcstoken";
    String NO_USER_ID = "NO_USER_ID";
    String CHINA_TIME_ZONE = "Asia/Shanghai";
    DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("yyyy-MM-dd HH:mm").withZone(systemDefault());
    DateTimeFormatter DATE_FORMATTER = ofPattern("yyyy-MM-dd").withZone(systemDefault());
    Collator CHINESE_COLLATOR = Collator.getInstance(CHINA);

    // MongoDB集合名
    String USER_COLLECTION = "users";
    String FILE_COLLECTION = "files";
    String FOLDER_COLLECTION = "folders";
    String FOLDER_HIERARCHY_COLLECTION = "folders_hierarchy";
    String COMMUNITY_POST_COLLECTION = "community_posts";
    String TEAM_COLLECTION = "teams";
    String SHEDLOCK_COLLECTION = "shedlock";
    String VERIFICATION_COLLECTION = "verifications";
    String UPLOAD_SESSION_COLLECTION = "upload_sessions";
    String FILE_EXTRA_COLLECTION = "file_extra";
    String GROUP_COLLECTION = "groups";
    String LIKE_COLLECTION = "likes";
    String COMMENT_COLLECTION = "comments";
    String COMMENT_HIERARCHY_COLLECTION = "comments_hierarchies";
    String SENSITIVE_WORD_COLLECTION = "sensitive_words";

    String PUBLISHING_DOMAIN_EVENT_COLLECTION = "publishing_domain_event";
    String CONSUMING_DOMAIN_EVENT_COLLECTION = "consuming_domain_event";

    // 数据库ID前缀
    String USER_ID_PREFIX = "USR";
    String FILE_ID_PREFIX = "FIL";
    String FOLDER_ID_PREFIX = "FLD";
    String FOLDER_HIERARCHY_ID_PREFIX = "FDH";
    String VERIFICATION_ID_PREFIX = "VRC";
    String UPLOAD_SESSION_ID_PREFIX = "UPL";
    String FILE_EXTRA_ID_PREFIX = "EXT";
    String GROUP_ID_PREFIX = "GRP";
    String POST_ID_PREFIX = "PUB";
    String LIKE_ID_PREFIX = "LIK";
    String COMMENT_ID_PREFIX = "CMT";
    String COMMENT_HIERARCHY_ID_PREFIX = "CMH";
    String SENSITIVE_WORD_ID_PREFIX = "SST";

    // Redis
    String USER_CACHE = "USERS";
    String FILE_CACHE = "FILES";
    String UPLOAD_SESSION_CACHE = "UPLOAD_SESSIONS";
    String USER_FOLDERS_CACHE = "USER_FOLDERS";
    String FOLDER_CACHE = "FOLDERS";
    String USER_FOLDER_HIERARCHIES_CACHE = "USER_FOLDER_HIERARCHIES";
    String FOLDER_HIERARCHY_CACHE = "FOLDER_HIERARCHIES";
    String FILE_EXTRA_CACHE = "FILE_EXTRAS";
    String USER_GROUPS_CACHE = "USER_GROUPS";
    String PUBLIC_FILE_CACHE = "PUBLIC_FILES";

    String LIKE_KEY = "LIKES";
    String LIKED_COUNT_KEY = "LIKED_COUNTS";
    String COMMENT_COUNT_KEY = "COMMENT_COUNTS";

    String REDIS_DOMAIN_EVENT_CONSUMER_GROUP = "domain.event.group";
    String REDIS_WEBHOOK_CONSUMER_GROUP = "webhook.group";
    String REDIS_NOTIFICATION_CONSUMER_GROUP = "notification.group";

    // ES索引
    String FILE_ES_INDEX_NAME = "file";

    // 领域事件别名
    String USER_CREATED_EVENT_NAME = "USER_CREATED";
    String FILE_UPLOADED_EVENT_NAME = "FILE_UPLOADED";
    String FILE_DELETED_EVENT_NAME = "FILE_DELETED";
    String FOLDER_HIERARCHY_CHANGED_EVENT_NAME = "FOLDER_HIERARCHY_CHANGED";
    String FOLDER_CREATED_EVENT_NAME = "FOLDER_CREATED";
    String FOLDER_DELETED_EVENT_NAME = "FOLDER_DELETED";
    String FILE_EXTRA_DELETED_EVENT_NAME = "FILE_EXTRA_DELETED";
    String GROUP_DELETED_EVENT_NAME = "GROUP_DELETED";
    String GROUP_MEMBERS_CHANGED_EVENT_NAME = "GROUP_MEMBERS_CHANGED";
    String FILE_PUBLISHED_EVENT_NAME = "FILE_PUBLISHED";
    String FILE_WITHDREW_EVENT_NAME = "FILE_WITHDREW";
    String COMMENT_CREATED_EVENT_NAME = "COMMENT_CREATED";
    String COMMENT_DELETED_EVENT_NAME = "COMMENT_DELETED";
    String LIKED_EVENT_NAME = "LIKED";
    String UNLIKED_EVENT_NAME = "UNLIKED";

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
    int MAX_CUSTOM_ID_LENGTH = 50;

    int MIN_PAGE_INDEX = 1;
    int MAX_PAGE_INDEX = 10000;
    int MIN_PAGE_SIZE = 10;

    int MAX_GROUP_MANAGER_SIZE = 10;

    // LLM摘要生成
    int BUFFER_SIZE = 8192;
    int MAX_INPUT_TEXT_LENGTH = 100000;
    int SUMMARY_MAX_TOKENS = 300; // 摘要Token限制

}
