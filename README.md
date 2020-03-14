本项目文档由北京邮电大学周晨曦撰写
# 一、项目介绍：
## 总体介绍：
  Gmall商城是一个**全品类B2C电商平台**，包含**网站前台和管理后台**两大部分。网站前台包含主站频道首页、搜索/查询、商品详情、购物车（以上无需用户登陆即可访问）、结算、订单详情、支付等服务。管理后台包含平台属性管理以及商品SPU、SKU管理功能。
**网站前台和管理后台**
![项目介绍](https://sm.ms/delete/l738n1qtPXm6OCAzgj2IfVpkWu)
## 主要技术：
1. 框架：SpringBoot+Mybatis+zookeeper+Dubbo;前后端分离
2. 前端技术：网站后台 Nodejs 网站前台采用Vue.js 和模板技术 thymeleaf 
3. 图片存储：FastDFS
4. 消息中间件技术： ActiveMQ
5. 搜索中间件技术： elasticsearch 
6. 缓存中间件技术： redis 
7. 单点登录中间件 ：CAS
# 二、管理后台模块：
ManageWeb(8081)+ManageService(8071)
 管理后台供管理员对平台属性/平台属性值进行增删改查，并对商品sku/spu进行增删改查。
## 代码简要说明：
**CatalogController.java 作用**：提供商品三级分类catalog接口。

注意点：  @CrossOrigin注解作用：解决前后端分离时端口不一致所产生的跨域问题；
         @Reference注解：通过dubbo远程调用Service服务
				 
**AttrController.java 作用**：提供平台属性列表接口。根据页面提供的三级分类id（如手机），查询该分类id下所有的平台属性名称。并调用attrService服务实现平台属性及平台属性值的增删改查。

注意点：平台属性与平台属性值是一对多关系。在调用dao层查询到平台属性后，应继续为每个平台属性查询平台属性值。并将值赋给平台属性。

**SkuController.java、SpuController.java 作用**：提供商品Spu和Sku的增删改查。
添加商品Spu时需要上传商品Spu图片+Spu名称/描述+商品销售属性及销售属性值（与平台属性不同，由商家自行根据实际情况自行上传）。添加Sku时需要上传sku名称/价格等信息，并且选择Spu给定的图片及销售属性值+平台属性值。

注意点：通过Nginx+FastDFS实现图片的存储，并返回一个url访问图片，将该url存入数据库。

# 三、商品详情模块：
ItemWeb(8082)+ManageService(8071)

商品详情模块提供商品页面的详情展示。并且可以根据商品销售属性切换商品。

## 业务分析：

  该页面的高访问量，虽然只是一个查询操作，但是由于频繁的访问所以我们必须对其性能进行最大程度的优化。其次，参考京东商城，可以通过选择商品的销售属性值来切换商品sku。
由于manageService中已有商品信息，因此，可以直接调用manageService服务+ItemWeb控制器。

**优化方案**：

①Thymeleaf实现页面静态化，而静态的HTML页面可以部署在nginx中，从而提高并发能力，减小tomcat压力。
②使用Redis缓存。

## 思路说明：
1. 通过给定sku，获得spu的商品销售属性+销售属性值；
```ruby

SELECT 
	sa.*, sav.*, if(ssav.sku_id,1,0) as isChecked
FROM
	pms_product_sale_attr sa
INNER JOIN pms_product_sale_attr_value sav ON sa.product_id = sav.product_id
AND sa.sale_attr_id = sav.sale_attr_id
AND sa.product_id = 66
LEFT JOIN pms_sku_sale_attr_value ssav ON sav.id = ssav.sale_attr_value_id
AND ssav.sku_id = ?
```
2. 通过给定sku，获得spu的商品销售属性+销售属性值；
在用户进入某一个spu领域后，通过选定销售属性值得到商品skuId。
为优化访问速度，生成一个hashmap。其中，key为销售属性值id的组合，value为商品skuId。因此从后台数据库查询出该spu下的所有skuId和属性值关联。并转成Json串。

```ruby
<select  id ="selectSkuSaleAttrValueListBySpu" parameterType="long" resultMap="skuSaleAttrValueMap">
        SELECT  skv.*
        FROM sku_sale_attr_value skv
        INNER JOIN  sku_info  sk  ON skv.sku_id =sk.id
        where  sk.spu_id=#{spuId}   
ORDER BY skv.sku_id , skv.sale_attr_id
    </select>
```
3. 最后将得到的{saleAttrValue：skuId}列表放到前端的隐藏域即可。
## 优化说明：

 为避免直接查询数据库，使用reids缓存机制。

## 缓存逻辑：

用户发起查询请求，item-web模块首先调用manage-service服务，在redis中查询缓存。如果缓存中有数据则直接返回，若无则继续查询数据库，将查询到的结果同步到缓存，并返回结果。
 
## 缓存在高并发和安全压力下的一些问题及解决方法：

## 缓存穿透

是利用redis和mysql的机制(redis缓存一旦不存在，就访问mysql)，直接绕过缓存访问mysql，而制造的db请求压力。（利用不存在的key去攻击mysql数据库）
一般在代码中防止该现象的发生
解决：为了防止缓存穿透将，null或者空字符串值设置给redis

## 缓存击穿

是某一个热点key在高并发访问的情况下，突然失效，导致大量的并发打进mysql数据库的情况

解决：在正常的访问情况下，如果缓存失效，如果保护mysql，重启缓存的过程

使用redis数据库的分布式锁，解决mysql的访问压力问题

1. 第一种分布式锁：redis自带一个分布式锁,set px nx。只有在key不存在时才成功。
2. 第二种分布式锁：redisson框架，一个redis的带有juc的lock功能的客户端的实现(既有jedis的功能，又有juc的锁功能)

## 缓存雪崩

缓存时采用了相同的过期时间，导致缓存在某一时刻同时失效，导致的db崩溃

解决：设置不同的缓存失效时间

核心代码展示：
 
 ```ruby
 String token = UUID.randomUUID().toString();//设置随机数，作为该线程近似唯一的标识，以防止出现删锁时，由于该线程拿到的锁已经释放了，但操作还						   //没结束，最后删锁删到其他线程的锁了。
 String setnx = jedis.set("sku:" + skuId + ":lock", token, "nx", "px", 10 * 1000);
    PmsSkuInfo skuByIdFromDB = null;
    if (!StringUtils.isBlank(setnx) && setnx.equals("OK")) {
	//获得分布式锁，访问mysql
	skuByIdFromDB = getSkuByIdFromDB(skuId);
	//mysql查询结果存入redis
	if (skuByIdFromDB != null) {
	    jedis.set("sku:" + skuId + ":info", JSON.toJSONString(skuByIdFromDB));
	} else {
	    jedis.setex("sku:" + skuId + ":info", 3 * 60, JSON.toJSONString(""));
	}
	String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
	Long eval = (Long) jedis.eval(script, Collections.singletonList("sku:" + skuId + ":lock"), Collections.singletonList(token));

    }
	Thread.sleep(1000);
	return getSkuById(skuId);
    }
```

注意点：

线程获得锁之后，需要将锁删除，如果在判断出该锁就是当前线程的锁，即将删除之前正好线程卡了，锁过期了。锁又变成其他线程的了，删锁还会删成别人的。
## 利用lua脚本在查询判断该锁就是当前线程的锁的同时删除锁。
	
# 四、搜索模块：

SearchWeb(8083)+SearchService(8074):

采用elasticsearch服务，主要基于倒排索引（索引是词语：对应的文档），相关性基于tf-idf；
idf:某一特定词语的IDF，可以由总文件数除以包含该词语的文件数，再将得到的商取对数得到。

通过jest客户端进行elasticsearch的整合，jest客户端（以Rest Api为主）可以直接使用dsl语句拼成的字符串，直接传给服务端，然后返回json字符串再解析。通过JSON工具类直接将查询得到的结果转成Bean。

查询只需调用dsl的封装工具类。不再赘述。
模块包括搜索功能及面包屑功能。
## 业务分析：
   通过搜索框查询，将查询参数传入elasticsearch搜索，得到返回的数据结果。并根据得到的数据，抽取检索结果所包含的平台属性+平台属性值的集合返回给页面。对平台属性集合进一步处理，去掉当前条件中参数给出的valueId所在的属性组。删除平台属性的同时，制作被删的这个平台属性对应的面包屑。面包屑bean名为PmsSearchCrumb，包含urlParam和valueId/name属性。每个属性值的面包屑，都有其对应的urlParam值。
   
**面包屑的制作**：

1. 当点击面包屑后=面包屑的url是当前请求-被点击面包屑的新请求
2. 当点击属性列表后=属性列表url是当前请求+被点击的属性列表的新请求
3. 当前请求的url的参数就是pmsSearchParam是所提交的参数。

# 五、购物车模块：

CartWeb(8084)+CartService(8074)

购物车模块要能过存储顾客所选的的商品，记录下所选商品，还要能随时更新，当用户决定购买时，用户可以选择决定购买的商品进入结算页面。功能要求：
1. 要持久化，保存到数据库中。
2. 利用缓存提高性能。
3. 未登录状态也可以存入购物车，一旦用户登录要进行合并操作。

## 业务分析：
1. 添加购物车：
本系统中设计成未登陆状态也可以将商品添加到购物车中。当用户登陆之后，通过消息队列让cookie中的购物车数据与数据库中的购物车数据合并。

因此，在商品详情页添加商品到购物车时，首先根据商品skuId获得商品skuInfo后，将skuInfo转化成omsCartItem。并判断用户是否登陆。如未登录，则先获得cookie数据，并将商品与cookie数据中的购车商品做比较，如果相同则增加数量，如果cookie中不包含本商品则新增该商品到cookie中，并更新cookie。
如已登陆，从db中查出购物车数据,根据memberId和skuId查单条数据，若为空则新增，不为空则更新数量。
①如果此时cookie里却还有购物车，说明需要把cookie中的购物车合并进来，同时把cookie中的清空。
②登陆时，发送消息，当监听到该消息时，调用购物车合并服务。最后加入redis缓存中，加到缓存后，查询购物车列表即可直接从缓存中读取。

2. 购物车列表：

如果用户已登录从缓存中取值，如果缓存没有，加载数据库。如果用户未登录从cookie中取值。

3. 修改购物车：

用户每次勾选购物车的多选框，都要把当前状态保存起来。由于可能会涉及更频繁的操作，所以这个勾选状态不必存储到数据库中。保留在缓存状态即可。如果用户未登陆，保存在cookie中即可。

4. 结算：

用户在未登录且购物车中有商品的情况下，点击结算不能直接跳到结算页面，要让用户强制登录后，检查cookie并进行合并后再重定向到结算页面。

# 六、用户认证模块：

passport(8085)

## 单点登陆方式介绍：
**同域下的单点登录：**
 存在cookie和session的跨域问题。
那么我们如何解决这两个问题呢？针对第一个问题，sso登录以后，可以将Cookie的域设置为顶域，即.a.com，这样所有子域的系统都可以访问到顶域的Cookie。我们在设置Cookie时，只能设置顶域和自己的域，不能设置其他的域。我们在sso系统登录了，这时再访问app1，Cookie也带到了app1的服务端（Server），app1的服务端怎么找到这个Cookie对应的Session呢？这里就要把3个系统的Session共享，如图所示。共享Session的解决方案有很多，例如：Spring-Session。这样第2个问题也解决了。
同域下的单点登录就实现了，但这还不是真正的单点登录。

**SSO：**
1. 用户访问web应用，被拦截器拦截，通过反射获得接口方法的注解。若注解为空，直接放行。
2. 拦截器从cookie中获得oldtoken，如果url中带有新token则将token更新为新token。
3. 调用认证中心的verify接口，验证token用户身份。
若token为空/验证失败，且注解参数loginSuccess = true，则必须登陆才可以访问（如结算、订单、支付等），则重定向到登陆界面。并将当前url作为ReturnURL传入参数中。用户登陆后，将token写入url及缓存中，跳转回原页面，并被拦截器拦截。
若token为空/验证失败，且注解参数loginSuccess = false，无需登陆也可访问。放行。
若验证成功，将用户id写入request中，并更新token到cookie中。设定cookie的作用域为顶级域。放行。
此后用户可以凭借该token访问其他web。

**认证中心作用：**
1. 给用户颁发通行证(token)
2. 验证其他业务功能接收token(用户访问所携带的)的真伪
**拦截器作用：**
若需要登陆，调用认证中心。
若验证成功，将token写入cookie；将用户id写入request。

## 社交登陆方式介绍：

以微博第三方登陆为例
1. 用户在passport认证中心准备登录时可以点击第三方社交登录按钮
跳转到
https://api.weibo.com/oauth2/authorize?client_id=YOUR_CLIENT_ID&response_type=code&redirect_uri=YOUR_REGISTERED_REDIRECT_URI
引导用户进入第三方网站的授权页面

2. 用户在授权完成后
http://网站回调地址?code=fef987b3f9ad1169955840b467bfc661
第三方网站将调用我们在第三方网站所创建的应用的回调地址，将授权码写入到我们的服务器中
我们网站需要将授权码保存到我们的数据库中

3. 我们通过授权码code发送post请求到第三方网站，换取access_token
https://api.weibo.com/oauth2/access_token?client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&grant_type=authorization_code&redirect_uri=YOUR_REGISTERED_REDIRECT_URI&code=CODE
换取来的token，和相关信息写入我们用户数据库

4. 在用户使用的过程中通过access_token获取用户数据(第三方网站的用户数据)
https://api.weibo.com/2/users/show.json?access_token=2.00S4TE5GH4psNBf534795d1677YqPB&uid=1
通过第三方社交登录的用户在本网站的信息需要补全，在该用户使用网站的级别高的功能时，要求用户补全信息甚至进行实名认证

登陆后利用jwt生成token存入redis，添加到url中。

# 七、订单模块：	

在我们点击**结算**按钮时，后台的购物车数据结构没被删除，订单数据结构没生成，结算按钮不调用后台的service数据库服务，结算页面只是用来用户确认送货清单和选择收获地址信息的页面。

点击**提交订单**按钮时, 后台的购物车数据结构被删除，订单数据结构生成了。购物车数据转化为订单数据，购物车表删除数据，订单表新增数据提交订单时，是对服务器的写操作，一般不用表单提交，而是直接从缓存或者数据库中查询用户所要购买的商品，转化成订单。

## 业务分析：

**需要防止用户通过页面回退的方式重复提交同一个订单**：
 结算时根据memberId生成交易码，同时存入redis和页面中，User:memberId:tradeCode  :  随机字符串。在提交订单时检查交易码的同时利用lua脚本销毁。
**订单数据提交**：
1. 根据用户id获得要购买的商品列表(购物车)，和总价格
2. 验价，验库存（不替用户做决定）:
根据用户信息查询当前用户的购物车中的商品数据,循环将购物车中的商品对象封装成订单对象(订单详情),每次循环一个商品时，校验当前商品的库存和价格是否复合购买要求
3. 将订单和订单详情写入数据库，删除购物车的对应商品
4. 重定向到支付系统


# 八、支付模块：

## 支付流程：
1. 用户点击提交订单后，跳转到支付服务。选择支付方式：微信/支付宝。以支付宝为例。
2. 重定向到支付宝界面，此时保存用户支付信息，并发送延迟队列消息，30分钟内检查支付状态；如果支付成功则更新支付信息。
3. 用户在支付宝界面登陆并付款后，支付宝将异步回调并通知给应用，应用收到后返回“success”，支付完成。
4. 也可同步回调重定向到应用支付模块，支付成功后，根据回调请求获得支付宝中的参数，通过支付宝的paramsMap进行签名验证sign验签。验签成功后更新支付信息。
5. 跳到订单页面。

## 分布式事务：
1.Xa协议：Tcc(try - comfirm - cancel.)
2.基于消息的，采取最终一致性策略的分布式事务(消息队列MQ)
将消息的事务和普通事务放在一起，同时提交或者回滚
在一个事务正在进行的同时，发出消息给其他的业务
如果消息发送失败，或者消息的执行失败，则回滚消息，重复执行
反复执行失败后，记录失败信息，后期补充性的处理
在消息系统中开启事务，消息的事务是指，保证消息被正常消费，否则回滚的一种机制
作用：通过消息队列来达到异步解耦的效果，减少了程序之间的阻塞等待时间，降低了因为服务之间调用的依赖风险。
弊端：不确定性和延迟

## 延迟队列注意事项：如何实现延迟

1. 初始发送消息时，在消息里增加count内容。初始count=5；
2. 每次监听器消费消息，获得count，如果count=0，则订单过期无效。否则count--；
3. 调用支付宝订单状态的检查接口，若查询为空,用户未扫码登陆支付宝创建交易，继续发送延迟队列消息；如果检查未支付成功，则继续发送该延迟队列消息。如果检查支付成功，则更新订单状态,进行幂等性检查;并且关闭延迟队列；

支付成功后更新用户支付信息的注意事项：
1. 如果异步回调中已更新支付信息。同步回调再更新则多余。可以事先做一个幂等性检查支付状态。
2. 支付成功将引发系统其他服务，包括订单服务更新，库存服务，物流服务等。因此在更新支付信息的同时，发送消息队列消息，消息名为"PAYHMENT_SUCCESS_QUEUE"，内容为out_trade_no信息。
3. Order监听器收到消息后，订单状态修改的同时，给库存发送mq；发送mq是在service中，接受是在Listener中；

## 验签说明：
RSA非对称密钥。原理：超大质数相乘不可逆。应用中保存着和支付宝绑定的公钥，应用的私钥。上传了自己的公钥到支付宝。

# 九、秒杀方案
基于redis的秒杀方案：

## Redis事务相关命令介绍：
1. MULTI命令 
用于开启一个事务，它总是返回OK。MULTI执行之后,客户端可以继续向服务器发送任意多条命令， 这些命令不会立即被执行，而是被放到一个队列中，当 EXEC命令被调用时， 所有队列中的命令才会被执行。

2. EXEC命令 
负责触发并执行事务中的所有命令： 
如果客户端成功开启事务后执行EXEC，那么事务中的所有命令都会被执行。 
如果客户端在使用MULTI开启了事务后，却因为断线而没有成功执行EXEC,那么事务中的所有命令都不会被执行。 
需要特别注意的是：即使事务中有某条/某些命令执行失败了，事务队列中的其他命令仍然会继续执行——Redis不会停止执行事务中的命令，而不会像我们通常使用的关系型数据库一样进行回滚。

3. DISCARD命令 
当执行 DISCARD 命令时， 事务会被放弃， 事务队列会被清空，并且客户端会从事务状态中退出。

4. WATCH 命令 
可以为Redis事务提供 check-and-set （CAS）行为。被WATCH的键会被监视，并会发觉这些键是否被改动过了。 如果有至少一个被监视的键在 EXEC 执行之前被修改了， 那么整个事务都会被取消， EXEC 返回nil-reply来表示事务已经失败。

# 基于jedis的实现
jedis.watch(productKey);//保证一致性

Transaction tx = jedis.multi();//开启事务

tx.incrBy(productKey, -1);//扣减库存

List<Object> list = tx.exec();//执行事务

mq.send(order);//发出订单

# 限流算法：
1. 漏油算法：
漏桶算法是访问请求到达时直接放入漏桶，如当前容量已达到上限（限流值），则进行丢弃（触发限流策略）。漏桶以固定的速率进行释放访问请求（即请求通过），直到漏桶为空。
2. 令牌桶算法：
令牌桶算法是程序以r（r=时间周期/限流值）的速度向令牌桶中增加令牌，直到令牌桶满，请求到达时向令牌桶请求令牌，如获取到令牌则通过请求，否则触发限流策略。

