package com.hmdp.service.impl;

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
        if (shopCache!=null){
            Shop shop = JSONUtil.toBean(shopCache, Shop.class);
            return Result.ok(shop);
        }
        // 缓存不命中，查数据库，存入缓存
        Shop shop = getById(id);
        if (shop == null) {
            return Result.fail("商品不存在");
        }
        String jsonStr = JSONUtil.toJsonStr(shop);
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id,jsonStr);
        return Result.ok(shop);
    }
}
