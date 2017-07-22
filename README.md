##  简单RPC框架
### 一.说明
1. 题目来源:[15年阿里中间件初赛](http://code.taobao.org/p/race/wiki/index/)
2. 目前通过的功能测试包括:<code>testNormalApiCall(), testNormalSpringCall(), testRpcContext(), testRpcContext()</code>
3. 还有很多隐藏的bug，后面会陆续完善尚未通过的测试
### 二.使用
####服务端：
1. <code>mvn clean package</code>
2. 在server.sh中修改相应端口参数, <code>./server.sh</code>
#### 客户端：
1. <code>mvn clean package</code>
2. 在funtionTest.sh中修改相应的地址参数，端口参数.<code>./functionTest.sh</code> 

