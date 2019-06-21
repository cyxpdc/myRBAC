package com.mmall.util;

import org.apache.commons.lang3.StringUtils;

public class LevelUtil {

    public final static String SEPARATOR = ".";

    public final static String ROOT = "0";

    /**
     * 计算当前层级，组成规则为上级层次+上级id，root为0，不用上级层次
     * 例子：如同为0.1，则为同一级，其上级为0.1
     * 0
     * 0.1
     * 0.1.2
     * 0.1.3
     * 0.4
     * @param parentLevel
     * @param parentId
     * @return
     */
    public static String calculateLevel(String parentLevel, int parentId) {
        if (StringUtils.isBlank(parentLevel)) {
            return ROOT;
        } else {
            return StringUtils.join(parentLevel, SEPARATOR, parentId);
        }
    }
}
