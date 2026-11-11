# My-Bok 项目约定

## 代码位置

| 部分 | 路径 | 说明 |
|---|---|---|
| 后端 | `D:\develop\code\My-Bok` | 当前仓库，Maven 多模块 |
| **前端** | **`D:\develop\code\My-Bok-Vue`** | **Vue 3 项目，独立目录（不在本仓库内）** |

写前端代码一律放 `D:\develop\code\My-Bok-Vue`，不要写进后端仓库。

## 后端模块

```
com.gsy:bok（父 POM，packaging=pom）
├── bok-common   公共基础设施：Result、JwtUtil、全局异常、Nacos/OpenFeign/LoadBalancer/Sentinel
├── bok-api      Feign 接口契约
├── bok-user     用户服务（8081）：MyBatis-Plus、MySQL、Redis、RabbitMQ、Seata
└── bok-gateway  网关（8080）：WebFlux，路由 + JWT 鉴权
```

## 技术栈版本

- Spring Boot 4.1.2 / Spring Framework 7
- Spring Cloud 2025.1.0-M1 / Spring Cloud Alibaba 2025.1.0.0
- MyBatis-Plus 3.5.18.1、MySQL 8、Redis 7、RabbitMQ 3.13
- Nacos 3.1.0、Seata 2.5.0、Sentinel 1.8.9

## 早期本地环境（Docker Desktop）

| 容器 | 端口 |
|---|---|
| bok-mysql | 3311 |
| bok-nacos | 8848（API）/ 8849（控制台） |
| bok-redis | 6379 |
| bok-seata | 8850 |
| bok-sentinel | 8851 |
| bok-rabbitmq | 5672 / 15672 |

## 约定

- 配置按 `application.yml`（公共）+ `application-dev.yml` / `application-prod.yml`（环境相关）拆分，dev/prod 两个文件不入库，仓库里只留 `*.example.yml` 模板。
- 生产配置走环境变量占位符，不要把地址/密码写死在仓库里。
