# 基于RBAC+部门的权限管理框架

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
1) applicationContext.xml里移除 <import resource="redis.xml" />
2) 修改RedisPool.java 类取消被spring管理
3）修改SysCacheService.java 类移除RedisPool.java的使用

2、如果想在正式环境使用，需要注意哪些
1）如果是集群部署，需要配置session共享，保证登录一次就可以，体验好
可以参考一下：http://blog.csdn.net/pingnanlee/article/details/68065535
2）确认一下项目里超级管理员的权限的规则
代码位置：SysCoreService.java类里的isSuperAdmin()
3）新增管理员的密码处理
SysUserService.java里的save() 方法里需要移除 password = "12345678";
同时，MailUtil里的发信参数要补全，并在SysUserService.java里的save()里 sysUserMapper.insertSelective(user) 之前调用
这是默认给的逻辑，可以根据项目实际情况调整

## A.项目类简介：

### 1 权限管理开发

#### 1.1 工具相关

JsonData：后端数据请求返回体，success方法和fail方法作为返回，加上@RequestBody转为Json

SpringExceptionResolver：全局异常处理类，使用modelAndView返回数据

ParamException、PermissionException：自定义异常,，通过new，参数传入自定义错误信息即可

BeanValidator：基于validator的校验类，使用TestVo、TestController来测试，在idea中的terminal使用curl url访问

JsonMapper：基于jackson convert的Json转换工具

ApplicationContextHelper：基于applicationContext的获取Spring上下文助手类，使用TestController#validate测试

HttpInterceptor：基于HandlerInterceptorAdapter的Http请求前后监听，可用来做请求时间统计；输出参数、url信息；preHandle方法可以进行登录验证，不过，我们交给shiro处理了；

#### 1.2 用户相关管理

##### 1.2.1 部门管理部分

DeptParam：部门参数，用来前后端交互；需要id、name、parentId、seq、remark，需要做参数校验

SysDeptController、SysDeptService、SysDeptMapper：部门三分层

LevelUtil：层级的工具类

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

##### 1.2.2 用户管理部分

- 功能：可以查看用户分配了哪些权限、哪些角色

SysUserController、SysUserService、SysUserMapper：用户三分层
扩展业务：SysUserController#acls方法可获取用户对应的权限模块及其权限点和角色list

UserParam：用户参数
关于密码：用户可以通过手机号和邮箱注册，然后系统通过邮件形式发送密码；也可以在注册页面让用户自己输入密码
PasswordUtil：生成密码的算法randomPassword()
MD5Util：加密

UserController：用户的非系统操作，即登录登出；
bug:这里类上不要加上@RequestMapping(前缀路径)，否则登录方法里面的“跳转到登录页面”会失败，因为默认加上前缀，而登录页面想要显示的话是不能加的

AdminController：登录之后跳转到此controller的index方法

PageQuery：分页查询
PageResult：分页结果

RequestHolder：使用ThreadLocal存储request信息和用户信息，隔离每个线程，高并发时可以达到线程安全的目的；算是一个**亮点**，以后想取用户信息和request信息，通过此类即可，如新增操作的setOperator，这样就不用每个add、update方法都要传用户参数了；
LoginFilter：登录拦截，调用RequestHolder的add方法；remove则交给HttpInterceptor#afterCompletion调用

IpUtil：可以获取操作人的ip

Mail：邮件bean
MailUtil：发送邮件

**亮点**：新增的时候不会传id，更新的时候会传id，所以xml中需要判断，如果id!=null,要id != #{id}，排除掉本身，如SysUserMapper的countByMail，这样才不会将要更新的值返回，导致更新失败；当然，也可以多写一个方法，区分新增和更新，但是这样代码更少，起到代码复用的作用：SysUserService#update

#### 1.3 权限相关管理

- 功能：可以看到哪些用户、角色拥有某权限

##### 1.3.1 权限模块

SysAclModuleController、SysAclModuleService、SysAclModuleMapper：权限模块
AclModuleParam：权限模块参数
SysTreeService：添加返回权限模块树方法
AclModuleLevelDto：权限模块树层级树返回结构，组合了List< AclModuleLevelDto >，即子权限模块

开发类似部门管理部分

SysAclController、SysAclService、SysAclMapper：权限点
AclParam：权限参数
扩展业务：SysAclController#acls方法可获取指定权限点对应的用户和角色

#### 1.4 角色相关管理

- 功能：可以看到角色分配的权限、用户

SysRoleController、SysRoleService、SysRoleMapper：角色三分层

RoleParam：角色参数

注：1.4.1和1.4.2的两种树结构都不需要定义参数，直接传roleId和对应的afterAclIds或afterUserIds即可
当然，想定义也可以，不过字段只有两个，没必要

##### 1.4.1 角色与权限树

应该显示所有权限模块及其权限点；如果点中的角色拥有某些权限点，则应该属于勾选状态

SysTreeService添加roleTree方法，由SysRoleController的roleTree调用，返回角色权限树结构
roleTree中，使用stream流，将List< SysAcl > 转换为Set< Integer >(当前用户已分配的权限点转换为对应的权限id)；

AclDto：返回权限点
AclModuleLevelDto：添加权限点列表，即List< AclDto >
sysCoreService：获取角色、用户权限的service

角色权限树算法：SysTreeService#roleTree

> 1.获取当前用户和角色已分配的权限点，转换为用户权限id和角色权限id的set集合
>
> 2.封装系统权限点列表：**将当前用户和角色已分配的角色点的hasAcl、checked字段设置为true，交给前端使用,hasAcl可以获取当前用户拥有的所有权限，就不用一个个看所有角色去人工计算所有权限了**
>
> 3.将系统权限点列表转换为角色权限树结构：
> 获取KV为权限模块id：权限点列表的moduleIdAclMap；获取权限模块树
> 使用递归封装好角色权限树：根据权限模块树遍历每一层的权限模块，根据moduleIdAclMap取出当前权限模块的权限点列表，排序后set给当前权限模块即可
>
> 4.最后返回此角色权限树，不仅显示权限模块及其对应的权限点，还显示相应的勾选(checked为true)

**亮点**：关于角色权限模块中AclDto的hasAcl字段设计

> 某个用户展示角色权限树时：
> 当前设计是比较好的：列出所有权限模块和权限点，当前用户如果有某个权限点，则使此字段为true；这样权限就是透明的，清晰可见
> 还有一种设计方案是只列出用户所有角色的所有权限点，这样的弊端是，进行权限分配时，可能造成一种误解：某个管理员可能会以为自己的某个角色拥有所有权限，而分配的过去的值也就没有所有权限，有些功能就无法使用了

StringUtil：splitToListInt方法，其中的最后一步使用Stream将List< String >转换为List< Integer >，即将前端传来的勾选的aclIds转换为List< Integer >

SysRoleAclService：角色权限服务，如修改用户权限点，使用先删后增的方案

##### 1.4.2 角色与用户树

SysRoleController为接口controller、SysRoleUserService、sysRoleUserMapper、SysUserMapper

SysRoleController#users使用Stream流过滤status！=1的用户，即状态不正常的用户不显示

### 2 权限拦截开发

AclControllerFilter：权限拦截器
Splitter用来将String转换为List< String >，并做一些如除去空格的处理
由SysCoreService#hasUrlAcl来判断是否有权限访问url

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

权限拦截时可以使用cache，即判断当前用户是否可以访问一个url，里面涉及许多查询数据库的动作

#### 缓存添加

SysCoreService#hasUrlAcl中的：
getCurrentUserAclListFromCache()：获取当前用户的所有权限
SysCoreService#getCurrentUserAclListFromCache即缓存

## C.权限更新操作记录

SysLogController、SysLogService、SysLogMapper：记录三分层

LogType：更新的表类型

SearchLogParam：权限记录搜索参数，里面的时间为String类型

SearchLogDto：权限搜索返回结构，里面的时间为Date类型，需要将SearchLogParam的String转换为Date，因为数据库是timestamp类型

SysLogService的几个saveXxxLog方法由RBAC相关Service的save、update调用，如增加部门时，也调用了saveDeptLog保存增加部门日志

关于SysLog的targetId字段设计也算是个小**亮点**吧

## D.规范：

项目中所有请求json数据，都使用.json结尾；所有请求page页面，都使用.page结尾

部门层级以 (上级部门.上级部门id) 形式保存

更新操作中，往往把之前的数据before查询出来，是为了日志处理

端口号为8888

权限拦截规则：
1.当前用户的某一个权限点有访问此url的权限，那么我们就返回true代表通过拦截；
2.若url的权限点都是无效的，则也返回true，代表谁都可以访问此url接口
若需要自定义拦截规则，则写在SysCoreService#hasUrlAcl里即可，这是一个最大的扩展点，如需要部门leader，leader对其下属有什么特殊权限，就可以在这里配置

目前是在角色上进行角色分配，如果想在部门上进行角色分配，只需要添加一张部门权限表，做好关系的关联，修改一下规则即可，也是一个扩展点

Cache可以根据自己的需要添加常用的数据，算是一个小扩展点

自设计**亮点**：可以设计一个类Operator，包含operator、operateTime、operateIp三个字段，让module里的类扩展此类，就能减少工作量，且一系列的setOperatXXX操作都可以单独抽取一个方法出来，达到代码复用，如SysLogService里的还原操作中的对象的setOperator、setOperateIp、setOperateTime方法

## E.bug:

xml引入要为Spring的，不能是其他的

getTimeOut()：mybatis和mybatis-spring版本不兼容

LoginFilter#doFilter

插入时需要返回id，交给SysLogService的saveXxxLog方法的targetId字段使用

- 课程来源慕课网