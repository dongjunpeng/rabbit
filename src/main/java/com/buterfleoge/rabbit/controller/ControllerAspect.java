package com.buterfleoge.rabbit.controller;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import com.buterfleoge.rabbit.RabbitWebContext;
import com.buterfleoge.whale.Constants;
import com.buterfleoge.whale.Constants.Status;
import com.buterfleoge.whale.Utils;
import com.buterfleoge.whale.log.AccessLogger;
import com.buterfleoge.whale.type.protocol.Error;
import com.buterfleoge.whale.type.protocol.Request;
import com.buterfleoge.whale.type.protocol.Response;

/**
 *
 *
 * @author xiezhenzong
 *
 */
@Aspect
@Component
public class ControllerAspect {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerAspect.class);

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void requestMapping() {
    }

    @Pointcut("execution(* com.buterfleoge.rabbit.controller.*Controller.*(..))")
    public void methodPointcut() {
    }

    @Around("requestMapping() && methodPointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Request request = findRequest(pjp.getArgs());
        String reqid = createReqid();
        request.setReqid(reqid);

        Object response = null;
        int status = Status.OK;
        try {
            response = pjp.proceed();
            if (response != null && ClassUtils.isAssignableValue(Response.class, response)) {
                status = ((Response) response).getStatus();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            status = Status.SYSTEM_ERROR;
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Class<?> returnType = signature.getMethod().getReturnType();
            if (ClassUtils.isAssignable(Response.class, returnType)) {
                Response r = new Response(Status.SYSTEM_ERROR);
                r.addError(new Error("系统异常, 请稍后重试或者联系客服： 18510248672"));
                response = r;
            } else {
                throw e;
            }
        } finally {
            long startTime = RabbitWebContext.getStartTime();
            // @formatter:off
            StringBuilder builder = new StringBuilder("[reqid=").append(reqid)
                    .append("][path=").append(RabbitWebContext.getRequestURI())
                    .append("][remote=").append(RabbitWebContext.getRemoteIp())
                    .append("][local=").append(Constants.LOCAL)
                    .append("][request=").append(request)
                    .append("][reponse=").append(response)
                    .append("][starttime=").append(startTime)
                    .append("][timecost=").append(System.currentTimeMillis() - startTime)
                    .append("][status=").append(status).append("]");
            // @formatter:on
            AccessLogger.log(builder.toString());
        }
        return response;
    }

    private String createReqid() {
        // @formatter:off
        StringBuilder builder = new StringBuilder("[path=").append(RabbitWebContext.getRequestURI())
                .append("][remote=").append(RabbitWebContext.getRemoteIp())
                .append("][local=").append(Constants.LOCAL)
                .append("][starttime=").append(RabbitWebContext.getStartTime())
                .append("][random=").append(Math.random()).append("]");
        // @formatter:on
        return Utils.stringMD5(builder.toString());
    }

    private Request findRequest(Object[] args) {
        for (Object arg : args) {
            if (ClassUtils.isAssignableValue(Request.class, arg)) {
                return (Request) arg;
            }
        }
        throw new RuntimeException("Wrong method argument, args: " + args);
    }

}
