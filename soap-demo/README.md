### WebService模块

##### 示例说明
   - 配置cxf，配置相应的Endpoint
   - 定义示例类
   - 定义WebService的服务接口，使用@WebService来注解接口，必要时，可以对相应属性进行配置
       - 使用@WebMethod来对接口中的类进行声明，标注该方法为WebService暴露方法
       - 使用@WebParam来对参数进行说明，@WebResult来表示方法的返回值
   - 实现服务接口，并对WebService注解的相应属性进行定义，以便清晰阅读
       - 实现相应方法

   - 客户端调用
       - 使用HttpClient调用WebService服务，使用其代理类工厂方式即可