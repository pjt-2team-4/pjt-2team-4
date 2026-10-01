package com.rookies6.myspringboot4project.user.service;

import com.rookies6.myspringboot4project.user.dto.SignupDTO;

public interface AuthService {
    SignupDTO.Response signup(SignupDTO.Request request);

}
