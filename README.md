##  简单RPC框架
### 一.说明
1. 题目来源:[15年阿里中间件初赛](http://code.taobao.org/p/race/wiki/index/)
2. 目前通过的功能测试包括:<code>testNormalApiCall(), testNormalSpringCall(), testRpcContext(), testRpcContext(), testTimeoutCall()</code>
3. 还有很多隐藏的bug，后面会陆续完善尚未通过的测试
### 二.使用
#### 服务端：
1. <code>mvn clean package</code>
2. <code>./server.sh yourport</code>
#### 客户端：
1. <code>mvn clean package</code>
2. <code>./functionTest.sh remoteaddress remoteport</code> 

