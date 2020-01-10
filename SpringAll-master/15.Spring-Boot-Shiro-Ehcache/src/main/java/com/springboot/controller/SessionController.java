package com.springboot.controller;

import com.springboot.pojo.ResponseBo;
import com.springboot.pojo.UserOnline;
import com.springboot.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by craywen on 2019/8/21.
 */
@Controller
@RequestMapping("/online")
public class SessionController {

    @Autowired
    SessionService sessionService;

    @RequestMapping("index")
    public String online() {
        return "online";
    }

    @ResponseBody
    @RequestMapping("list")
    public List<UserOnline> list() {
        return sessionService.list();
    }

    @ResponseBody
    @RequestMapping("forceLogout")
    public ResponseBo forceLogout(String id) {
        try {
            sessionService.forceLogout(id);
            return ResponseBo.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBo.error("踢出用户失败");
        }

    }
}
