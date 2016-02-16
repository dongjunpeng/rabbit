package com.buterfleoge.rabbit.controller.aop;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;

/**
 * 
 * @author xiezhenzong
 * 
 */
public class ControllerAspect {

    private static final Logger LOG = Logger.getLogger(ControllerAspect.class);

    public void log(MethodInvocation invocation) {
        long startTime = System.currentTimeMillis();
        long endTime = 0;
        try {
            invocation.proceed();
            

        } catch (Exception e) {
        } catch (Throwable t) {

        }
        
        
        
        
    }

}

