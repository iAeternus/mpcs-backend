package com.ricky.common.domain.page;

import com.ricky.common.domain.AggregateRoot;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;

import static com.ricky.common.utils.ValidationUtils.isBlank;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * 排序字段注册表
 * <p>
 * 用于将 API 层暴露的排序字段映射为数据库字段名，
 * 避免客户端直接依赖数据库字段结构
 *
 * <h3>Examples</h3>
 * <pre>{@code
 * SortRegistry registry = SortRegistry.newInstance()
 *     .register("createdAt", "created_at")
 *     .register("author", "authorId");
 *
 * Sort sort = registry.resolve(query.getSortedBy(), query.isAsc());
 * }</pre>
 * <p>
 * 当排序字段为空或未注册时，默认按 createdAt 倒序排序
 */
public class SortRegistry {

    private final Map<String, String> fieldMap = new HashMap<>();

    private SortRegistry() {
    }

    /**
     * 创建新的排序注册表
     */
    public static SortRegistry newInstance() {
        return new SortRegistry();
    }

    /**
     * 注册 API 排序字段与数据库字段的映射关系
     *
     * @param apiField 对外暴露的排序字段名
     * @param dbField  实际数据库字段名
     */
    public SortRegistry register(String apiField, String dbField) {
        fieldMap.put(apiField, dbField);
        return this;
    }

    /**
     * 根据排序参数解析为 {@link Sort}
     *
     * @param sortedBy API 传入的排序字段
     * @param asc      是否升序
     * @return Spring Data {@link Sort} 对象
     */
    public Sort resolve(String sortedBy, boolean asc) {
        if (isBlank(sortedBy) || !fieldMap.containsKey(sortedBy)) {
            return Sort.by(DESC, AggregateRoot.Fields.createdAt);
        }
        return Sort.by(asc ? ASC : DESC, fieldMap.get(sortedBy));
    }
}
