package com.neusoft.elk.logback;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

/**
 * 修改自开源项目logback-redis-appender
 * https://github.com/kmtong/logback-redis-appender
 * 放方便后续redis整合及细节调整，将代码从jar中迁移并 修改过期方法
 * 
 * @author kmtong
 * @editor ding
 * @version 1.0
 * @date 2016年1月24日
 */
public class RedisAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    JedisPool pool;

    // keep this for config compatibility for now
    JSONEventLayout jsonlayout;

    Layout<ILoggingEvent> layout;

    // logger configurable options
    String host = "localhost";
    int port = Protocol.DEFAULT_PORT;
    String key = null;
    int timeout = Protocol.DEFAULT_TIMEOUT;
    String password = null;
    int database = Protocol.DEFAULT_DATABASE;

    public RedisAppender() {
        jsonlayout = new JSONEventLayout();
    }

    @Override
    protected void append(ILoggingEvent event) {
        Jedis client = pool.getResource();
        try {
            String json = layout == null ? jsonlayout.doLayout(event) : layout.doLayout(event);
            client.rpush(key, json);
        } catch (Exception e) {
            e.printStackTrace();
            client.close();
            client = null;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public Layout<ILoggingEvent> getLayout() {
        return layout;
    }

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    public String getSource() {
        return jsonlayout.getSource();
    }

    public void setSource(String source) {
        jsonlayout.setSource(source);
    }

    public String getSourceHost() {
        return jsonlayout.getSourceHost();
    }

    public void setSourceHost(String sourceHost) {
        jsonlayout.setSourceHost(sourceHost);
    }

    public String getSourcePath() {
        return jsonlayout.getSourcePath();
    }

    public void setSourcePath(String sourcePath) {
        jsonlayout.setSourcePath(sourcePath);
    }

    public String getTags() {
        if (jsonlayout.getTags() != null) {
            Iterator<String> i = jsonlayout.getTags().iterator();
            StringBuilder sb = new StringBuilder();
            while (i.hasNext()) {
                sb.append(i.next());
                if (i.hasNext()) {
                    sb.append(',');
                }
            }
            return sb.toString();
        }
        return null;
    }

    public void setTags(String tags) {
        if (tags != null) {
            String[] atags = tags.split(",");
            jsonlayout.setTags(Arrays.asList(atags));
        }
    }

    public String getType() {
        return jsonlayout.getType();
    }

    public void setType(String type) {
        jsonlayout.setType(type);
    }

    public void setMdc(boolean flag) {
        jsonlayout.setProperties(flag);
    }

    public boolean getMdc() {
        return jsonlayout.getProperties();
    }

    public void setLocation(boolean flag) {
        jsonlayout.setLocationInfo(flag);
    }

    public boolean getLocation() {
        return jsonlayout.getLocationInfo();
    }

    public void setCallerStackIndex(int index) {
        jsonlayout.setCallerStackIdx(index);
    }

    public int getCallerStackIndex() {
        return jsonlayout.getCallerStackIdx();
    }

    public void addAdditionalField(AdditionalField p) {
        jsonlayout.addAdditionalField(p);
    }

    @Override
    public void start() {
        super.start();
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setTestOnBorrow(true);
        pool = new JedisPool(config, host, port, timeout, password, database);
    }

    @Override
    public void stop() {
        super.stop();
        pool.destroy();
    }

}
