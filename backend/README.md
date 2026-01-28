# v0.1后端代码更新说明：

这是一个尚在开发中的版本，仅为接口展示统一起见，提前提交。

此版本大幅削减无实际意义的冗余代码，优化了数据库，包-类关系，提高了代码可读性。

项目主题仍然采用SpringBoot3构建，数据库交互采用JPA，加密使用Spring-Security，登录校验，权限认证使用SA-Token，添加了OpenAPI文档和Swagger UI。

此外，数据库mysql在docker部署，简化了迁移难度，未来正式版本我会上传正式的docker-compose。目前数据库构建请参考entity实体类的定义。