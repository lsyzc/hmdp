package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        String shopCache = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_KEY + id);
        if (StrUtil.isNotBlank(shopCache)){
            Shop shop = JSONUtil.toBean(shopCache, Shop.class);
            return Result.ok(shop);
        }
        // 当缓存为空字符串时，商品不存在（缓存穿透，缓存空字符串）
        if (shopCache != null) {
            return Result.fail("商品不存在");
        }
        // 缓存不命中,且未缓存空字符串时，查数据库，存入缓存
        Shop shop = getById(id);
        if (shop == null) {
            //数据库不存在时，缓存空字符串，解决缓存穿透问题
            stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id,"",RedisConstants.CACHE_NULL_TTL,TimeUnit.MINUTES);
            return Result.fail("商品不存在");
        }
        String jsonStr = JSONUtil.toJsonStr(shop);
        //stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id,jsonStr);
        //为缓存设置超时时间
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id,jsonStr,RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        return Result.ok(shop);
    }

    @Override
    @Transactional
    public Result updateShop(Shop shop) {
        //先更新数据库再删缓存
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("id 不能为空");
        }
        updateById(shop);
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + id);
        return Result.ok();
    }
}
