package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.LOGIN_CODE_KEY;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        boolean phoneInvalid = RegexUtils.isPhoneInvalid(loginForm.getPhone());
        if (phoneInvalid) {
            return Result.fail("手机号格式错误");
        }
        //Object code = session.getAttribute("code");
        String code = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + loginForm.getPhone());

        if (code == null || !code.equals(loginForm.getCode())) {
            return Result.fail("验证码输入有误");
        }
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getPhone, loginForm.getPhone());
        User user = getOne(userLambdaQueryWrapper);
        if (user == null) {
            // 注册新用户
            user = createUserWithPhone(loginForm.getPhone());
        }
        //session.setAttribute("user",user);
        String token = UUID.randomUUID().toString();
        //隐藏敏感信息后再保存
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(), CopyOptions.create().setIgnoreNullValue(true)
                .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        //session.setAttribute("user",userDTO);
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey,userMap);
        //设置token时间
        stringRedisTemplate.expire(tokenKey,30, TimeUnit.MINUTES);
        // 登录成功后删除验证码
        stringRedisTemplate.delete(LOGIN_CODE_KEY + loginForm.getPhone());
        return Result.ok(token);

    }
    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName("游客" + phone);
        save(user);
        return user;
    }
}
