### 断路器模块

###### 示例说明
   - 配置RestTemplate
   - 通过@HystrixCommand注解，来使用断路器功能，如果服务不可用，可以配置fallbackMethod属性，来提示服务的当前状态