package com.ricky.user.infra;

import com.ricky.common.exception.MyException;
import com.ricky.common.oss.minio.MinioProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.ricky.common.constants.ConfigConstants.AVATAR_BUCKET;
import static com.ricky.common.exception.ErrorCodeEnum.OSS_ERROR;
import static com.ricky.common.utils.ValidationUtils.isBlank;

@Component
@RequiredArgsConstructor
public class AvatarUrlResolver {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    @Value("${security.avatar.presign-seconds:3600}")
    private Integer presignSeconds;

    public String toPublicUrl(String avatarUrlOrObjectKey) {
        if (isBlank(avatarUrlOrObjectKey)) {
            return "";
        }
        if (isExternalUrl(avatarUrlOrObjectKey)) {
            return avatarUrlOrObjectKey.trim();
        }

        String objectKey = normalizeObjectKey(avatarUrlOrObjectKey);
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(AVATAR_BUCKET)
                            .object(objectKey)
                            .expiry(presignSeconds)
                            .build()
            );
        } catch (Exception ex) {
            throw new MyException(OSS_ERROR, "Generate avatar url failed", "objectKey", objectKey);
        }
    }

    private String normalizeObjectKey(String avatarUrlOrObjectKey) {
        String value = avatarUrlOrObjectKey.trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            String endpoint = minioProperties.getEndpoint();
            if (isBlank(endpoint)) {
                return value;
            }
            String normalizedEndpoint = endpoint.replaceAll("/+$", "");
            if (value.startsWith(normalizedEndpoint + "/")) {
                String path = value.substring(normalizedEndpoint.length() + 1);
                return path.startsWith(AVATAR_BUCKET + "/")
                        ? path.substring(AVATAR_BUCKET.length() + 1)
                        : path;
            }
            return value;
        }

        String objectKey = value.replaceFirst("^/+", "");
        if (objectKey.startsWith(AVATAR_BUCKET + "/")) {
            return objectKey.substring(AVATAR_BUCKET.length() + 1);
        }
        return objectKey;
    }

    private boolean isExternalUrl(String avatarUrlOrObjectKey) {
        String value = avatarUrlOrObjectKey.trim();
        if (!(value.startsWith("http://") || value.startsWith("https://"))) {
            return false;
        }
        String endpoint = minioProperties.getEndpoint();
        if (isBlank(endpoint)) {
            return true;
        }
        String normalizedEndpoint = endpoint.replaceAll("/+$", "");
        return !value.startsWith(normalizedEndpoint + "/");
    }
}
