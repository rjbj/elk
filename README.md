# logback+redis+logstash+elasearch+kibana集成 #
### 版本说明 ###
1. 系统版本：centos7.3
2. elk版本:6.1.3
3. 主机IP：192.168.111.139
4. redis版本：2.8.2

### demo地址 ###

https://github.com/system-integration/elk

### es安装 ###

> 开放以下端口

	firewall-cmd --zone=public --add-port=9200/tcp --permanent && firewall-cmd --reload
	firewall-cmd --zone=public --add-port=6379/tcp --permanent && firewall-cmd --reload
	firewall-cmd --zone=public --add-port=5601/tcp --permanent && firewall-cmd --reload

> 创建elk普通用户

	#root执行
	useradd elk -d /elk

> 修改系统配置

	#root执行

	cat >> /etc/sysctl.conf <<EOS
	vm.max_map_count=655360
	EOS

	sysctl -p

	cat >> /etc/security/limits.conf <<EOF
	elk soft nofile 65535
	elk hard nofile 131072
	elk soft nproc 4096
	elk hard nproc 4096
	EOF

	cat >> /etc/profile <<EOF
	ulimit -u 65535
	ulimit -n 4096
	ulimit -d unlimited 
	ulimit -m unlimited 
	ulimit -s unlimited 
	ulimit -t unlimited 
	ulimit -v unlimited
	EOF

	source /etc/profile

> elk用户安装jdk

	export JAVA_HOME=~/java
	export PATH=$JAVA_HOME/bin:$PATH
	export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar

> 创建目录

	#启动脚本目录
	mkdir -p /elk/bin

	#日志目录
	mkdir -p /elk/log

	#数据目录
	mkdir -p /elk/data

![](https://i.imgur.com/tTWCqmr.png)

> 解压安装es修改配置如下

	# 配置文件路径 /work/elasticsearch-6.1.3/config/elasticsearch.yml

	network.host: 192.168.111.139
	path.data: /elk/data
	path.logs: /elk/log
	

> 制作启动脚本


	cat >> /elk/bin/es-start.sh <<EOF
	nohup /elk/elasticsearch-6.1.3/bin/elasticsearch &> /dev/null &
	EOF

	chmod +x /elk/bin/es-start.sh
	
> 启动，查看启动日志

	/elk/bin/es-start.sh && tail -200f /elk/log/elasticsearch.log

> 查看服务

	http://192.168.111.139:9200/
	

![](https://i.imgur.com/Eq5DLg4.png)


### redis安装 ###

> 启动redis 6379端口

### logstash安装 ###

> 制作脚本

	cat >> /elk/logstash-6.1.3/config/server.conf <<EOF
	input {
	redis {
	        host => "192.168.111.139"
	        port => 6379
	        data_type => "list"
	        key => "logstash"
	        threads => 5
	        codec => "json"
	    }
	}
	filter {
	        json {
	            source => "message"
	        }
	}
	output {
	#通过不同type区分不同系统
	if [type] =~ "app1" {
	
		#输出到本地文件
	    file {
	        path => "/elk/app1.log"
	        codec => line
	        {
	        format => "[%{@timestamp}]--[%{level}]--[%{host}]--%{message}"
	        }
	    }
	    elasticsearch {
	        action => "index"
	        hosts  => ["192.168.111.139:9200"]
	        index => "%{type}-%{+YYYY.MM.dd}"
	        }
	
	}
	if [type] =~ "app2" {
	
	}
	}

	EOF

	cat >> /elk/bin/logstash-start.sh <<EOF
	nohup /elk/logstash-6.1.3/bin/logstash -f /elk/logstash-6.1.3/config/server.conf &> /elk/log/logstash.log &
	EOF	

	chmod +x /elk/bin/logstash-start.sh

> 启动，查看启动日志

	/elk/bin/logstash-start.sh && tail -200f /elk/log/logstash.log


### Kibana安装 ###

> 修改kibana配置

	vim /elk/kibana-6.1.3-linux-x86_64/config/kibana.yml

	server.host: "192.168.111.139"
	elasticsearch.url: "http://192.168.111.139:9200"

> 制作脚本

	cat >> /elk/bin/kibana-start.sh <<EOF
	nohup /elk/kibana-6.1.3-linux-x86_64/bin/kibana &> /elk/log/kibana.log &
	EOF

	chmod +x /elk/bin/kibana-start.sh

> 启动，查看启动日志

	/elk/bin/kibana-start.sh && tail -200f /elk/log/kibana.log

> 访问配置

	http://192.168.111.139:5601

### 新建spring boot项目 ###

> pom.xml

	<?xml version="1.0" encoding="UTF-8"?>
	<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	    <modelVersion>4.0.0</modelVersion>
	
	    <groupId>com.example</groupId>
	    <artifactId>demo</artifactId>
	    <version>0.0.1-SNAPSHOT</version>
	    <packaging>jar</packaging>
	
	    <name>elk-demo</name>
	    <description>Demo project for Spring Boot</description>
	
	    <parent>
	        <groupId>org.springframework.boot</groupId>
	        <artifactId>spring-boot-starter-parent</artifactId>
	        <version>2.0.0.RELEASE</version>
	        <relativePath /> <!-- lookup parent from repository -->
	    </parent>
	
	    <properties>
	        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
	        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
	        <java.version>1.8</java.version>
	    </properties>
	
	    <dependencies>
	        <dependency>
	            <groupId>org.springframework.boot</groupId>
	            <artifactId>spring-boot-starter-web</artifactId>
	        </dependency>
	
	        <dependency>
	            <groupId>org.springframework.boot</groupId>
	            <artifactId>spring-boot-starter-test</artifactId>
	            <scope>test</scope>
	        </dependency>
	        <dependency>
	            <groupId>org.springframework.boot</groupId>
	            <artifactId>spring-boot-configuration-processor</artifactId>
	        </dependency>
	        <dependency>
	            <groupId>redis.clients</groupId>
	            <artifactId>jedis</artifactId>
	        </dependency>
	        <dependency>
	      <groupId>ch.qos.logback</groupId>
	      <artifactId>logback-classic</artifactId>
	    </dependency>
	    </dependencies>
	
	    <build>
	        <plugins>
	            <plugin>
	                <groupId>org.springframework.boot</groupId>
	                <artifactId>spring-boot-maven-plugin</artifactId>
	            </plugin>
	        </plugins>
	    </build>
	
	
	</project>

> logback.xml

	<?xml version="1.0" encoding="UTF-8"?>
	<configuration scan="true" scanPeriod="60 seconds" debug="false">
	    <appender name="LOGSTASH" class="com.example.demo.logback.RedisAppender">
	        <host>192.168.111.44</host>
	        <port>6378</port>
	        <type>test2</type>
	        <key>test</key>
	    </appender>
	    <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
	        <!-- <filter class="ch.qos.logback.classic.filter.ThresholdFilter"> 
	            <level>info</level> </filter> -->
	        <encoder>
	            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
	        </encoder>
	    </appender>
	    <appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
	        <!-- 不丢失日志.默认的,如果队列的80%已满,则会丢弃TRACT、DEBUG、INFO级别的日志 -->
	        <discardingThreshold>0</discardingThreshold>
	        <!-- 更改默认的队列的深度,该值会影响性能.默认值为256 -->
	        <queueSize>5120</queueSize>
	        <appender-ref ref="LOGSTASH" />
	    </appender>
	    <root name="com.example.demo" level="info">
	        <appender-ref ref="ASYNC" />
	        <appender-ref ref="console" />
	    </root>
	</configuration>  


> application.properties

	logging.config=classpath:logback.xml
	#redis host
	elk.redis.host=192.168.111.139
	#redis port
	elk.redis.port=6379
	#系统唯一标识（区分不同系统）
	elk.service.type=app1
	#日志级别
	elk.log.level=info

### 测试 ###

> 启动spring boot程序

<br>

> 查看日志文件

	cd /elk && tail -200f /elk/app1.log
![](https://i.imgur.com/P2ZTeV9.png)

> 配置kibana

	![](https://i.imgur.com/6CfRrWZ.png)

	