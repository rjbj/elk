package com.neusoft.elk.logback;

/**
 * 修改自开源项目logback-redis-appender
 * https://github.com/kmtong/logback-redis-appender
 * 
 * @author kmtong
 */
public class AdditionalField {
    private String key;
    private String value;

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
