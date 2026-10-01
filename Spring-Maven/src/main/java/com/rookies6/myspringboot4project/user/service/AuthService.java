package com.rookies6.myspringboot4project.user.service;

import com.rookies6.myspringboot4project.user.dto.LoginDTO;
import com.rookies6.myspringboot4project.user.dto.SignupDTO;
import com.rookies6.myspringboot4project.user.dto.TokenDTO;

public interface AuthService {

    SignupDTO.Response signup(SignupDTO.Request request);

    TokenDTO.Response login(LoginDTO.Request request);
}
