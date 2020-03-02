### MongoDB整合模块

   - 定义数据库对应类
   - 实现MongoRepository接口，来自定义数据查询机制
   - 定义数据查询服务，使用相应的接口进行访问即可
   - 配置spring.data.mongodb.uri的地址为：mongodb://localhost:27017/springboot-db
   - 如果有用户名以及密码，进行相应配置