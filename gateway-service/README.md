 1.spring cloud gateway --->注册到nacos注册中心 -->搭建nacos集群 
--> gateway中配置nacos配置 -->启动Gateway查看nacos中是否有服务注册

2.权限校验
第一种：我们通过gateway 引入我们的oauth2的一些相关依赖，通过继承覆写一些核心方法，完成我们token的校验；
为了能够顺利的校验token，我们还需要引入 mysql的链接配置。（稍微有点复杂）。这种方式其实还是不错的，虽然
oauth2的一些相关代码稍微复杂些，但是他的验证是十分健全的；虽然mysql链接配置是比redis慢了点，但是我们后续可以
改成redis的。也能体现出一定优势。（第一种方式，无论是引入 mysql还是 redis，我都不太喜欢，gateway他就是一个
网关--转发、限流、校验等等，尽量不要在网关做第三方中间件的使用）
响应式编程（Mono） --- 有兴趣的小伙伴可以去了解下

第二种：我们通过 check_token接口（命令式编程--rest调用，等待结果返回）进行token的验证（调用oauth2服务器，网关直接转发请求获取结果，进行判断就行了）

========第一种：我们通过gateway 引入我们的oath2的一些相关依赖===========
1. 先得进行 权限的 校验 AccessManager。（有一些访问路径是不需要进行token校验的，有一些是需要token校验的，对于
需要token校验的，我们进行真实的校验即可）

问：token校验说起来很容易，但是这个token校验是需要和mysql db进行交互的呀？我就需要用到 db 的 datasource，
有了datasource，我们才能够进行和db的交互。

2. 进行我们的 securityConfig 类创建，融入我们的 datasource 用来进行db的交互；融入第一步创建的权限管理类
AccessManager。 第二个类，更像是一个管理融合的这么一个角色。 而且，我们还需要考虑securityConfig有没有其他
职责呢？
 2.1 首先，spring could gateway 是基于webFlux的响应式编程，允许httpBaisc 吗？
 2.2 我们通过页面的一些访问，如果干过前端的小伙伴，并且使用过ajax的伙伴，都知道：每次进行后台调用的时候，前端
 代码先发送一个 HTTPMthod.OPTIONS(放行), 然后才会真实的发送 GET或者POST（校验）.

 问：datasource融入之后，我们怎么访问db呀？ JPA? MYBAITS? 还是个啥呢？

 3. 我们通过响应式的 JDBC的查询方式，进行我们的DB的交互

 ========第一种：测试===========
伙伴们还记不记得我们初始第一次使用 gateway访问 user service 的那个代码啊；我们用
http://localhost:9090/user-service 访问，直接可以打印 hello！

我们就用这个接口测试现在的 继承了 oauth2的代码。 预期是：
没有token 就返回 401， 拒绝访问；
有token 正确，返回 hello！
有token 不正确，返回error信息！

一定要先打开 nacos； 然后启动 gateway 、 oauth2、user


========第二种 ： 通过 feign 进行 check token 接口的调用，验证token===========
1. 创建FeignClient调用 oauth2-service
2. 创建我们 Global filter，在这个filter中接收token 调用check_token接口进行验证；
 Global filter还能干很多事情，诸如：有一些不需要拦截的 api；转发请求的时候添加一些headers
 （personId，tracing id等等）
 2.1 我们的 fitler一定要 使用 我们的注解 @Component
 2.2 @EnableFeignClients 需要配置到启动类上
 2.3 如果“在gateway中”，通过Autowired我们引入 feignclient，会发生死锁。springcloud gateway人家是
 基于netty的，人加是webFlux的， 人家是响应式的编程。你这么唐突的引入feignclient，在代码设计层面Loaded RoutePredicateFactory
 会造成死锁，导致无法启动
  可是gateway这东西，很有可能会需要进行rest形式的一些调用啊，比如现在的我们通过filter去checktoken，这是
  很常见的一个需要，很合理，你不让我用吗？
  这个问题解决，只能错峰，
  @Autowired
  private Oauth2ServiceClient oauth2ServiceClient; 需要礼让我们的springcloud gateway的webflux相关
  的加载。@Lazy
 2.4 block()/blockFirst()/blockLast() are blocking, which is not supported in thread reactor-http-nio-3
  reactor 响应式编程就是基于 reactor的。为什么在  Map<String, Object> result = oauth2ServiceClient.checkToken(token); 报错呢？
  你用openfeign的rest形式进行checktoken的调用，就是命令式编程，你这不跟响应式编程对这干呢么。
 2.5 feign.codec.DecodeException: No qualifying bean of type
  'org.springframework.boot.autoconfigure.http.HttpMessageConverters' available:
  expected at least 1 bean which qualifies as autowire candidate.
  HttpMessageConverters 这个是我们http请求的msg转化器，既然2.4说明了，openfeign和gateway的调用机制有冲突，那么我们在2.4解决了这个
  同步异步问题，但是没结果msg转化问题，所以还需要继续解决msg convert。 我们需要引入 config 类了。