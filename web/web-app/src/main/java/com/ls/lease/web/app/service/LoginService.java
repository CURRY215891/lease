package com.ls.lease.web.app.service;

import com.ls.lease.web.app.vo.user.LoginVo;
import com.ls.lease.web.app.vo.user.UserInfoVo;

public interface LoginService {
    void getCode(String phone) throws Exception;

    String login(LoginVo loginVo);

    UserInfoVo getLoginUserById(Long userId);
}
