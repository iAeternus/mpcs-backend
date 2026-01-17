package com.ricky.common.constants;

public interface LuaScriptConstants {

    /**
     * 原子地 : 获取 Hash 中的全部键值对，并删除该 Hash Key
     * <p>
     * KEYS[1] : 待读取并删除的 Hash Key
     * <p>
     * 返回值 : 形如 [field1, value1, field2, value2, ...]
     */
    String HGETALL_AND_DEL_LUA_SCRIPT = """
            local key = KEYS[1]
            local allData = redis.call('HGETALL', key)
            redis.call('DEL', key)
            return allData
            """;

    /**
     * 尝试点赞
     * <p>
     * KEYS[1] : LIKE_CACHE (userId::postId -> status)
     * KEYS[2] : LIKED_COUNT_CACHE (postId -> count)
     * <p>
     * ARGV[1] : userId::postId
     * ARGV[2] : postId
     * ARGV[3] : LIKE 状态码
     * <p>
     * 返回值 :
     * 1 = 点赞成功 (状态发生变化)
     * 0 = 已点赞 (无变化)
     */
    String TRY_LIKE_LUA = """
            local current = redis.call('HGET', KEYS[1], ARGV[1])
            if current == ARGV[3] then
                return 0
            end
            redis.call('HSET', KEYS[1], ARGV[1], ARGV[3])
            redis.call('HINCRBY', KEYS[2], ARGV[2], 1)
            return 1
            """;

    /**
     * 尝试取消点赞
     * <p>
     * KEYS[1] : LIKE_CACHE (userId::postId -> status)
     * KEYS[2] : LIKED_COUNT_CACHE (postId -> count)
     * <p>
     * ARGV[1] : userId::postId
     * ARGV[2] : postId
     * ARGV[3] : LIKE 状态码
     * ARGV[4] : UNLIKE 状态码
     * <p>
     * 返回值 :
     * 1 = 取消成功 (状态发生变化)
     * 0 = 非点赞状态 (无变化)
     */
    String TRY_UNLIKE_LUA = """
            local current = redis.call('HGET', KEYS[1], ARGV[1])
            if current ~= ARGV[3] then
                return 0
            end
            redis.call('HSET', KEYS[1], ARGV[1], ARGV[4])
            redis.call('HINCRBY', KEYS[2], ARGV[2], -1)
            return 1
            """;

}
