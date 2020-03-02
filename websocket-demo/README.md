## WebSocket简单示例

WebSocket 是通过一个 socket 来实现双工异步通信能力的，但直接使用 WebSocket ( 或者 SockJS：WebSocket 协议的模拟，增加了当前浏览器不支持使用 WebSocket 的兼容支持) 协议开发程序显得十分繁琐，所以使用它的子协议 STOMP。

### （广播式）示例步骤
   - 通过实现**WebSocketMessageBrokerConfigurer**来进行配置，注册一个STOMP节点，以及配置一个广播式消息代理
   - 编写客户端消息与服务端消息发送类
   - 通过@MessageMapping注解，来映射浏览器发送给服务的消息地址
   - 通过@SendTo注解，将服务器收到的消息发送至相应的浏览器地址
   - 编写WebSocket的连接-断开连接以及从服务器接收消息的脚本，进行测试
