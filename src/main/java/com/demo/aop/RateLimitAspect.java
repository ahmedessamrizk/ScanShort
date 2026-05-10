package com.demo.aop;

import com.demo.dtos.request.LoginRequest;
import com.demo.exceptions.custom.TooManyRequestException;
import com.demo.services.RateLimitService;
import com.demo.annotations.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    private final RateLimitService rateLimitService;
    //Intercept methods that has rate limit annotation above it or above its controller.
    @Around("@annotation(rateLimit) || @within(rateLimit)")
    public Object handle(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        //Get the actual @RateLimit, where method level takes priority over class level
        RateLimit effectiveRateLimit = getEffectiveRateLimit(joinPoint, rateLimit);

        //Extract identifiers from the request
        HttpServletRequest request = getCurrentRequest();
        String ip = request.getRemoteAddr();
        String email = extractEmail(joinPoint);

        //Check IP limit
        boolean isIpAllowed = rateLimitService.isAllowed(
                "ip:" + ip,
                effectiveRateLimit.maxAttempts() * 5,
                effectiveRateLimit.windowSeconds(),
                effectiveRateLimit.blockSeconds());

        if(!isIpAllowed) throw new TooManyRequestException("Too many requests for this IP!");

        //If email exist, check email limit
        if(email != null){
            boolean isEmailAllowed = rateLimitService.isAllowed(
                    "email:" + email,
                    effectiveRateLimit.maxAttempts(),
                    effectiveRateLimit.windowSeconds(),
                    effectiveRateLimit.blockSeconds());

            if (!isEmailAllowed) throw new TooManyRequestException("Too many attempts for this email");
        }

        //If no limits, proceed the method
        return joinPoint.proceed();
    }

    //If method annotation exist return it over class annotation.
    private RateLimit getEffectiveRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        //Check if there is annotation above that method or not
        //If there -> return it, Other -> then rateLimit of course contain class annotation only
        RateLimit methodAnnotation = method.getAnnotation(RateLimit.class);
        return methodAnnotation != null? methodAnnotation : rateLimit;
    }

    //Get current HTTP request from Spring's request context
    private HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes())
                .getRequest();
    }

    //Extract email from method if exists
    private String extractEmail(ProceedingJoinPoint joinPoint) {
        for(Object arg: joinPoint.getArgs()){
            if(arg instanceof LoginRequest request){
                return request.email();
            }
        }

        return null;
    }
}
