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
	
