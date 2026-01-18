# webflux部分概念
    1. webflux与mvc是对等的关系，其底层基于netty + reactor + spring，是一个全异步非阻塞的web响应式框架。
    2. 响应式框架，底层是异步 + 本地缓存的消息队列 + 事件回调。
    3. 优点是能用少量资源处理大量的请求。