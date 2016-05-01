package com.buterfleoge.rabbit.interceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author xiezhenzong
 *
 */
public class AccountInterceptor extends AuthInterceptor {

    private static final Pattern ACCOUNT_PAGE_PATTERN = Pattern.compile("/account/([\\d]+)");

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 20;
    }

    @Override
    protected boolean shouldPreHandle(String path) {
        return path.startsWith("/account");
    }

    @Override
    protected boolean hasAuth(Long accountid, HttpServletRequest request, String path) {
        Long reqAccountid = null;
        Matcher matcher = ACCOUNT_PAGE_PATTERN.matcher(path);
        if (matcher.find()) {
            String temp = matcher.group(1);
            System.out.println(temp);
            reqAccountid = Long.parseLong(temp);
        } else {
            reqAccountid = getAccountidFromReq(request);
        }
        return accountid.equals(reqAccountid);
    }

}
