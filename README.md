# 开始



## 接口

本 sdk 提供的接口

```java
public Object flatRequest(CasFlatRequest flatRequest) throws Exception;
```

GET 示例：

```json
{
    "method": "GET",
    "url": "/vms/linkageInfo!fetchCapabilitySet.action"
}
```

POST 示例：

```json
{
    "method": "POST",
    "url": "/vms/deviceInfo!fetchDeviceInfoListN.action",
    "values": {
        "start": "0",
        "limit": "20",
        "unitId": "1"
    }
}
```

首次请求的详细网络交互过程参考 **showAddDeviceWindowN.action.log**



## 构建

```shell
mvn clean package
```

成功构建，得到 `target/hik8200-cas-1.0.0-sdk.jar`



## 应用

如果项目需要使用本 sdk，需要做以下操作：

- 在项目的 `pom.xml` 所在目录下创建一个名称为 `libs` 的文件夹，并将 `hik8200-cas-1.0.0-sdk.jar` 拷贝到 `libs` 下。



- `pom.xml` 添加如下依赖：

```xml
<dependency>
    <groupId>eniso</groupId>
    <artifactId>hik8200-cas</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${pom.basedir}/libs/hik8200-cas-1.0.0-sdk.jar</systemPath>
</dependency>

<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.12.1</version>
</dependency>

<!-- Guava -->
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>28.1-jre</version>
</dependency>

<!-- Bouncy Castle -->
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcpkix-jdk15on</artifactId>
    <version>1.64</version>
</dependency>
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk15on</artifactId>
    <version>1.64</version>
</dependency>

<!-- OkHTTP3 -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>3.14.4</version>
</dependency>
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>logging-interceptor</artifactId>
    <version>3.14.4</version>
</dependency>

<!-- Retrofit2 -->
<dependency>
    <groupId>com.squareup.retrofit2</groupId>
    <artifactId>retrofit</artifactId>
    <version>2.6.2</version>
</dependency>
<dependency>
    <groupId>com.squareup.retrofit2</groupId>
    <artifactId>converter-scalars</artifactId>
    <version>2.6.2</version>
</dependency>

<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```



- pom.xml 构建（供参考）：

    ```xml
    <build>
        <resources>
            <resource>
                <directory>${pom.basedir}/libs</directory>
                <targetPath>BOOT-INF/lib/</targetPath>
                <includes>
                    <include>**/*.jar</include>
                </includes>
            </resource>
        </resources>
    </build>
    ```



- 需要在 application.yml 添加海康 cas 配置信息

    ```yml
    hik:
      cas:
        host: 'https://192.168.130.206:443/'
        username: 'admin'
        password: 'hik12345+'
    ```



- Application 配置

    ```java
    @ComponentScan(basePackages = {"eniso.common", "你的基包..."})
    ```

    例如：

    ```java
    @SpringBootApplication
    @ComponentScan(basePackages = {"eniso.common", "eniso.sample"})
    public class CasServiceApplication {
    
        public static void main(String[] args) {
            SpringApplication.run(CasServiceApplication.class, args);
        }
    
    }
    ```

