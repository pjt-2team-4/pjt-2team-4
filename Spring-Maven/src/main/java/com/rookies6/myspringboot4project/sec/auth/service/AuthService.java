package com.rookies6.myspringboot4project.sec.auth.service;

import com.rookies6.myspringboot4project.sec.auth.dto.SignupDTO;

public interface AuthService {
    SignupDTO.Response signup(SignupDTO.Request request);

}
