package com.atguigu.gmall.interceptors;

import com.atguigu.gmall.annotations.LoginRequired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    public boolean preHandle(HttpServletRequest request,HttpServletResponse response, Object handler ) throws Exception{

        // 通过反射获得被拦截请求中的方法的注解，如果有LoginRequired注解则需要拦截判断，若无则不需要拦截；

        HandlerMethod hm = (HandlerMethod) handler;
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);
        if(methodAnnotation==null){
            // 该方法无需拦截
            return true;
        }
            // 拦截代码
            boolean loginSuccess = methodAnnotation.loginSuccess();
            if (loginSuccess) {
                // loginSuccess 默认为true，必须得登入成功后拦截器放行；
                
            } else {// loginSuccess 如果为false，则登陆失败也可以放行：如购物车；但必须判断是否登陆成功，如果成功并返回token

            }

        return true;

    }
}
