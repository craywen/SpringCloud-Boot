package com.springboot.service;

import com.springboot.pojo.UserOnline;

import java.util.List;

/**
 * Created by craywen on 2019/8/21.
 */
public interface SessionService {
        List<UserOnline> list();
        boolean forceLogout(String sessionId);
}
