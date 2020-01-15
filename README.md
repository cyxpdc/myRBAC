# 基于RBAC+部门的安全框架

使用SSM+Redis+Filter开发，可基于此项目进行二次开发，如添加业务逻辑，扩展权限拦截功能等

导入IDEA，添加Tomcat发布，即可运行

搜索”亮点“可查询本项目的亮点；”功能“可以看到本项目的重要功能；”扩展点“可以看到可以扩展的地方

项目启动注意事项：

1、数据库配置：/resources/settings.properties

2、redis配置：/resources/redis.properties

3、项目登录页：/signin.jsp

4、登录使用用户名和密码：

username: admin@qq.com

password: 12345678

- 其他：

1、如果暂时不想使用redis，如何移除
1) applicationContext.xml里移除` <import resource="redis.xml" />`

2) 修改RedisPool.java 类取消被spring管理

3）修改SysCacheService.java 类移除RedisPool.java的使用

2、如果想在正式环境使用，需要注意哪些

1）如果是集群部署，需要配置session共享，保证登录一次就可以，体验好
可以参考一下：http://blog.csdn.net/pingnanlee/article/details/68065535

2）确认一下项目里超级管理员的权限的规则
代码位置：SysCoreService.java类里的isSuperAdmin()

3）新增管理员的密码处理
MailUtil里的发信参数要补全，通过邮箱发送密码是默认给的逻辑，可以根据项目实际情况调整

## A.项目类简介：

相关表所属的包为model，sys_user和sys_acl类似，sys_acl_module和sys_dept类似，sys_role_user和sys_role_acl类似，使用lombok的builder模式来创建实例

表的时间字段使用DateTime：https://mp.weixin.qq.com/s/k_ljSyGpwtsnAkIQu5ZtnA

### 1 权限管理开发

#### 1.1 工具相关

JsonData：后端数据请求返回体，success方法和fail方法返回此结构，加上@RequestBody转为Json

SpringExceptionResolver：全局异常处理类，使用modelAndView返回数据

ParamException、PermissionException：自定义异常,，通过new，参数传入自定义错误信息即可

BeanValidator：基于Validator的校验类，使用TestVo、TestController来测试，在idea中的terminal使用curl url访问

JsonMapper：基于jackson convert的Json转换工具

ApplicationContextHelper：基于applicationContext的获取Spring上下文助手类，使用TestController#validate测试（BeanPostProcessor：https://blog.csdn.net/baidu_19473529/article/details/81057974、ApplicationContextAware：https://blog.csdn.net/baidu_19473529/article/details/81072524）

HttpInterceptor：基于HandlerInterceptorAdapter的Http请求前后监听，可用来做请求时间统计；输出参数、url信息；preHandle方法可以进行登录验证，不过，我们交给框架处理了；

#### 1.2 用户相关管理

![1560650676587](../markdownPicture/assets/1560650676587.png)

##### 1.2.1 部门管理部分

DeptParam：部门参数，用来前后端交互；需要id、name、parentId、seq、remark，需要做参数校验

SysDeptController、SysDeptService、SysDeptMapper：部门三分层

LevelUtil：层级的工具类，当前层级的组成规则为上级层次+上级id；root则为0，不需要上级层次

###### 树结构开发：

DeptLevelDto：部门层级树返回结构

SysTreeService：部门树Service层，deptTree()生成部门层级树结构，由SysDeptController#tree调用

> 重难点：递归生成树的算法：
>
> 1.
> 定义好部门层级保存规范：即以 (上级部门.上级部门id) 形式保存，root层保存0；
> 定义好部门树的数据结构：即部门层级类DeptLevelDto
>
> 2.获取所有部门：使用List保存；然后使用部门List初步封装DeptLevelDtoList
>
> 3.
> 使用Multimap，键为level，值为对应的所有DeptLevelDto，即保存DeptLevelDtoList的数据；
> 使用rootList保存第一级DeptLevelDto
>
> 4.按照seq字段排序
>
> 5.使用递归处理所有层级的数据(从第一层rootList开始)：使用Multimap获取当前层级的下一层级DeptLevelDto，用来给当前层级的DeptLevelDto赋值，最后返回List< DeptLevelDto >，转换为JsonData交给前端

也就是将List< SysDept >转换为List< DeptLevelDto >，然后所有数据存在Multimap< String, DeptLevelDto >里面，key为层级，value为对应的List列表，接着就是递归，每次递归都是处理一层，从根开始处理

##### 1.2.2 用户管理部分

- 功能：可以查看用户分配了哪些权限、哪些角色

SysUserController、SysUserService、SysUserMapper：用户三分层
扩展业务：SysUserController#acls方法可获取用户对应的权限模块及其权限点和角色List

```markdown
获取用户对应的权限模块及其权限点（也是List的树结构）：
首先根据用户id获取用户的角色id列表List<Integer>，然后根据用户的角色id列表获取权限点id列表List<Integer>，接着根据权限点id列表获取权限点List<SysAcl>；
将List<SysAcl>封装为List<AclDto>，然后将其转换为树结构：
获取权限模块树列表List<AclModuleLevelDto>，根据List<AclDto>封装Multimap<Integer, AclDto>，KV为权限模块id：权限点列表，然后根据这两者来给给每个权限模块赋值对应的权限点：
遍历当前权限模块树列表，根据权限模块树id从Multimap<Integer, AclDto>中取出对应的权限点列表，给每个权限模块赋值对应的权限点列表，注意每个权限模块下有子权限模块和权限点，需要for+递归处理（类似transformDeptTree，部门下有子部门，每次递归处理一个子部门,for处理所有同级部门，这里则是每次递归处理一个子权限模块，for处理所有同级权限模块）
```

```markdown
获取用户的角色List：这个简单，直接根据用户id和sys_role_user表查出对应的角色id列表，再根据角色id列表查出对应的角色即可（sql核心：in）
```

UserParam：用户参数
关于密码：用户可以通过手机号和邮箱注册，然后系统通过邮件、短信形式发送密码；也可以在注册页面让用户自己输入密码，这里使用第一种中的邮箱
PasswordUtil：生成密码的算法randomPassword()
MD5Util：加密

UserController：用户的非系统操作，即登录登出；
bug:这里类上不要加上@RequestMapping(前缀路径)，否则登录方法里面的“跳转到登录页面”会失败，因为默认加上前缀，而登录页面想要显示的话是不能加的

AdminController：登录之后跳转到此controller的index方法

PageQuery：分页查询，这里可以对limit进行优化：
如SysUserMapper#getPageByDeptId
`SELECT * FROM sys_user WHERE dept_id = #{deptId} ORDER BY username ASC LIMIT #{page.offset}, #{page.pageSize}`
大limit+whereorderby下可以优化为：利用表的覆盖索引+联合索引来加速分页查询：https://blog.csdn.net/h2604396739/article/details/80535546
`SELECT * FROM sys_user WHERE id >= (SELECT id FROM sys_user WHERE dept_id = #{deptId} ORDER BY username ASC LIMIT {page.offset}, 1) LIMIT #{page.pageSize}`，加‘dept_id ,username ’索引：`alter table sys_user add index idx_deptid_username(dept_id ,username )`

PageResult：分页结果

RequestHolder：使用ThreadLocal存储request信息和用户信息，隔离每个线程，高并发时可以达到线程安全的目的；算是一个**亮点**，以后想取用户信息和request信息，通过此类即可，如新增操作的setOperator，这样就不用每个service的save、update方法都要传用户参数了；
LoginFilter：登录拦截，调用RequestHolder的add方法；remove则交给HttpInterceptor#afterCompletion调用

IpUtil：可以获取操作人的ip

Mail：邮件bean
MailUtil：发送邮件

**亮点**：新增的时候不会传id，更新的时候会传id，所以xml中需要增加if判断，如果id!=null,要id != #{id}，排除掉本身，如SysUserMapper的countByMail，这样才不会将要更新的值返回，导致更新失败；当然，也可以多写一个方法，区分新增和更新，但是这样代码更少，起到代码复用的作用：SysUserService#update

#### 1.3 权限相关管理

- 功能：可以看到哪些用户、角色拥有某权限

##### 1.3.1 权限模块

SysAclModuleController、SysAclModuleService、SysAclModuleMapper：权限模块
AclModuleParam：权限模块参数
SysTreeService：添加返回权限模块树方法
AclModuleLevelDto：权限模块树层级树返回结构，组合了List< AclModuleLevelDto >，即子权限模块
这部分开发类似部门管理部分，树结构也可以抽取公共代码进行代码的优化：将SysAclModule和SysDept的属性共同属性抽取出来做一个父类SysTree，将两者联系起来，然后将此父类作为方法参数来做重构，实现代码的优化，以后每多一种树结构类，就继承SysTree，直接调用方法即可，这里并没有优化，因为只有两种树结构，没必要

SysAclController、SysAclService、SysAclMapper：权限点，这里的权限点分页同样可以进行优化
AclParam：权限参数
扩展业务：SysAclController#acls方法可获取指定权限点对应的用户和角色

#### 1.4 角色相关管理

- 功能：可以看到角色分配的权限、用户

SysRoleController、SysRoleService、SysRoleMapper：角色三分层

RoleParam：角色参数

注：1.4.1和1.4.2的两种树结构都不需要定义参数，直接传roleId和对应的afterAclIds/afterUserIds即可
当然，想定义也可以，不过字段只有两个，没必要

##### 1.4.1 角色与权限树

应该显示所有权限模块及其权限点；如果点中的角色拥有某些权限点，则应该属于勾选状态

SysTreeService添加roleTree方法，由SysRoleController的roleTree调用，返回角色权限树结构
roleTree中，使用stream流，将List< SysAcl > 转换为Set< Integer >(当前用户已分配的权限点转换为对应的权限id)；

AclDto：返回权限点
AclModuleLevelDto：添加权限点列表，即List< AclDto >
sysCoreService：获取角色、用户权限的service

角色权限树算法：SysTreeService#roleTree

>1.获取当前用户和角色已分配的权限点，转换为用户权限id和角色权限id的set集合
>
>2.封装系统权限点列表：**将当前用户和角色已分配的角色点的hasAcl、checked字段设置为true，交给前端使用,hasAcl可以获取当前用户拥有的所有权限，就不用一个个看所有角色去人工计算所有权限了；而checked则可以将当前角色拥有的权限点进行勾选**
>
>3.将系统权限点列表转换为角色权限树结构：
>获取KV为权限模块id：权限点列表的moduleIdAclMap；获取权限模块树
>使用递归封装好角色权限树：根据权限模块树遍历每一层的权限模块，根据moduleIdAclMap取出当前权限模块的权限点列表，排序后set给当前权限模块即可
>
>4.最后返回此角色权限树，不仅显示权限模块及其对应的权限点，还显示相应的勾选(checked为true)

也就是获取”权限模块列表“功能所得到的权限模块树结构和所有权限点，将当前用户和角色所拥有的权限点的hasAcl、checked字段进行设置，然后将权限点设置进对应的权限模块即可

**亮点**：关于角色权限模块中AclDto的hasAcl字段设计

> 某个用户展示角色权限树时：
> 当前设计是比较好的：列出所有权限模块和权限点，当前用户如果有某个权限点，则使此字段为true；这样权限就是透明的，清晰可见
> 还有一种设计方案是只列出用户所有角色的所有权限点，这样的弊端是，进行权限分配时，可能造成一种误解：某个管理员可能会以为自己的某个角色拥有所有权限，而分配的过去的值也就没有所有权限，有些功能就无法使用了

StringUtil：splitToListInt方法，其中的最后一步使用Stream将List< String >转换为List< Integer >，即将前端传来的勾选的aclIds转换为List< Integer >

SysRoleAclService：角色权限服务，如修改用户权限点，使用先删后增的方案

##### 1.4.2 角色与用户树

SysRoleController为接口controller、SysRoleUserService、sysRoleUserMapper、SysUserMapper

SysRoleController#users方法可以获取当前角色的已选和未选用户：先从数据库获取当前角色已选的用户列表和所有用户，即可得到当前角色未选的用户列表，返回给前端即可

SysRoleController#changeUsers方法可以修改当前角色对应的用户

### 2 权限拦截开发

AclControllerFilter：权限拦截器
Splitter用来将String转换为List< String >，并做一些如除去空格的处理
由SysCoreService#hasUrlAcl来判断当前用户是否有权限访问url，只要当前用户有一个此url的权限，即当前用户所有角色的所有权限中有一个此url的权限即返回true，否则如果当前url的权限都是无效，也返回true，若有一个有效则返回false

关于request的几个返回url方法区别：
<https://blog.csdn.net/qq_42390424/article/details/83757336>

SysAclMapper#getByUrl方法需要注意，如果为动态url，需要用正则表达式

## B.缓存

将权限进行缓存，这样就不用每个请求都去查询数据库，可以大大提高性能

redis.xml：Spring关联redis的配置

redis.properties：redis的配置

RedisPool：封装redis池，@Service表示交给Spring管理，组合进SysCacheService使用，其内部使用了	ShardedJedisPool，在redis.xml中进行了配置

SysCacheService：缓存处理服务

CacheKeyConstants：缓存前缀

SysCacheService#generateCacheKey：使用Joiner类可以对一个集合操作，使用Joiner.on对每一项进行拆开

#### 缓存场景分析

RBAC模型接口不能使用缓存，因为我们需要保证其准确性；如果加上了cache，每次更新数据库时，如果cache没有同步更新，则会出错，除非使cache同步更新，即update时，删除cache数据，查询时重新添加cache
且其使用也不频繁，不需要，反而一直增删改，每次都要更新缓存，得不偿失

权限拦截时可以使用cache，即判断当前用户是否可以访问一个url，里面涉及许多查询数据库的动作，对性能帮助很大

#### 缓存添加

SysCoreService#hasUrlAcl中的：
getCurrentUserAclListFromCache()：获取当前用户的所有权限
SysCoreService#getCurrentUserAclListFromCache即缓存

## C.权限更新操作记录

SysLogController、SysLogService、SysLogMapper：记录三分层

LogType：更新的表类型，可用枚举代替

SearchLogParam：权限记录搜索参数，里面的时间为String类型

SearchLogDto：权限搜索返回结构，里面的时间为Date类型，需要将SearchLogParam的String转换为Date，因为数据库是timestamp类型

SysLogService的几个saveXxxLog方法由RBAC相关Service的save、update调用，如增加部门时，也调用了saveDeptLog保存增加部门日志

关于SysLog的targetId字段设计也算是个小**亮点**吧

## D.规范：

项目中所有请求json数据，都使用.json结尾；所有请求page页面，都使用.page结尾

部门层级以 (上级部门.上级部门id) 形式保存

更新操作中，往往把之前的数据before查询出来，是为了日志处理

端口号为8888

目前是在角色上进行角色分配，如果想在部门上进行角色分配，只需要添加一张部门权限表，做好关系的关联，修改一下规则即可，也是一个扩展点

Cache可以根据自己的需要添加常用的数据，算是一个小扩展点

自设计**亮点**：可以设计一个类Operator，包含operator、operateTime、operateIp三个字段，让module里的类扩展此类，就能减少工作量，且一系列的setOperatXXX操作都可以单独抽取一个方法出来，达到代码复用，如SysLogService里的还原操作中的对象的setOperator、setOperateIp、setOperateTime方法

加密：提供了MD5算法

## E.bug:

xml引入要为Spring的，不能是其他的

getTimeOut()：mybatis和mybatis-spring版本不兼容

LoginFilter#doFilter

插入时需要返回id，交给SysLogService的saveXxxLog方法的targetId字段使用

- 课程来源慕课网

## F:幂等框架

要求有token才能提交，且只能提交一次，防止重复提交表单（可能是因为网络，可能是因为黑客攻击）

每次调用接口时都需要生成token，且使用完后token就会删掉，这样就达到幂等性的目的了

也就是说有两种原因需要幂等：

> 1 表单重复提交问题，会重复调用API（使用AOP前置通知+注解解决）
>
> 2 rpc远程调用的时候网络发生延迟，如果有重试机制的话可能也会重复调用API

需要保证token临时且唯一（15-120分钟）

### 1.1 常量

```java
public interface ConstantUtils {
	public static final String EXTAPIHEAD = "head";//token来自请求头
	public static final String EXTAPIFROM = "from";//token来自表单的隐藏域
}
```

### 1.2 Redis操作

使用RedisPool

### 1.3 RedisTokenUtils工具类

封装了生成token和获取token的逻辑

### 1.4 自定义Api幂等注解和切面

```java
//解决接口幂等性问题的注解
//需要支持网络延迟和表单重复提交两种原因
@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiIdempotent {
	String type();//用来表示token来自请求头还是RPC
}
```

### 1.5 封装生成token注解

```java
//用户到了提交表单的页面时会生成token
@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiToken {
}
```

### 1.6 编写ExtApiAopIdempotent

ExtApiAopIdempotent会使用@Before前置通知来生成token，将生成的token进行`getRequest().setAttribute("token", redisTokenUtils.getToken());`，前端可以将其放入隐藏域中，然后使用@Around环绕通知，进行四步骤的验证：

```markdown
1.获取令牌 存放在请求头或隐藏域中
2.判断令牌是否在缓存中有对应的令牌
3.如何缓存没有该令牌的话，直接报错（请勿重复提交）
4.如何缓存有该令牌的话，则删除该令牌，可以执行业务逻辑
```

使用：表单重复问题类似如下

```java
@RestController
public class TestController {

    //index页面上有表单，此表单有请求域，那么请求此url时就会生成token传给隐藏域（由@ExtApiToken完成）
    //也就是说@ApiToken注解的目的只是替代req.setAttribute("token",redisToken.getToken())而已，这样业务开发人员就不用自己写了，直接打注解就行（ApiAopIdempotent#apiToken()方法实现了此逻辑）
	@RequestMapping("/index")
	@ApiToken
	public String index(HttpServletRequest req) {
		return "index";
	}

	@RequestMapping("/addTest")
	@ApiIdempotent(value = ConstantUtils.EXTAPIFROM)
	public String addTest(Entity entity) {
		//...
	}
}
```

API幂等：

```java
@RestController
public class TestController {

	@Autowired
	private RedisTokenUtils redisTokenUtils;

	//测试时先调用此url获取token，在postman中手动输入测试
    //从redis中获取Token
	@RequestMapping("/redisToken")
	public String RedisToken() {
		return redisTokenUtils.getToken();
	}

	// 验证Token
	@RequestMapping(value = "/addTestApiIdempotent", produces = "application/json; charset=utf-8")
	@ExtApiIdempotent(value = ConstantUtils.EXTAPIHEAD)
	public String addTestApiIdempotent(@RequestBody Entity entity, HttpServletRequest request) {
		//...
	}
}
```

