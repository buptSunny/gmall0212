# 一、项目介绍：
## 总体介绍：
  Gmall商城是一个**全品类B2C电商平台**，包含**网站前台和管理后台**两大部分。网站前台包含主站频道首页、搜索/查询、商品详情、购物车（以上无需用户登陆即可访问）、结算、订单详情、支付等服务。管理后台包含平台属性管理以及商品SPU、SKU管理功能。
**网站前台和管理后台**
![项目介绍](https://sm.ms/delete/l738n1qtPXm6OCAzgj2IfVpkWu)
## 主要技术：
1. 框架：SpringBoot+Mybatis+zookeeper+Dubbo;前后端分离
2. 前端技术：网站后台 Vue.js+ElementUI 网站前台采用Vue.js 和模板技术 thymeleaf 
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
 
 
