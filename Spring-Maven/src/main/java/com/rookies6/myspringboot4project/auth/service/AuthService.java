package com.rookies6.myspringboot4project.auth.service;

import com.rookies6.myspringboot4project.auth.dto.SignupDTO;

public interface AuthService {
    SignupDTO.Response signup(SignupDTO.Request request);

}
