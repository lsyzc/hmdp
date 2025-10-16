package com.hmdp.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.Map;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;
import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        String token = request.getHeader("authorization");
//        if (token == null) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            return false;
//        }
//        String tokenKey = LOGIN_USER_KEY + token;
//        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(tokenKey);
//        if (userMap.isEmpty()) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            return false;
//        }
//        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
//        UserHolder.saveUser(userDTO);
//        return true;
        // 前面拦截器如果token有效就会存储了user
        if (UserHolder.getUser() == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }else return true;
//        HttpSession session = request.getSession();
//        Object user = session.getAttribute("user");
//        if (user == null) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            return false;
//        }
//        UserHolder.saveUser(userDTO);
//        return true;
    }
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserHolder.removeUser();
    }
}
