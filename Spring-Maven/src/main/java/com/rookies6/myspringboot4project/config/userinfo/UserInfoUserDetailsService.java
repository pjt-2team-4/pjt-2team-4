package com.rookies6.myspringboot4project.config.userinfo;

import com.rookies6.myspringboot4project.user.entity.User;
import com.rookies6.myspringboot4project.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserInfoUserDetailsService
        implements UserDetailsService {


    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(
            String username
    ) throws UsernameNotFoundException {

        User user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "user not found "
                                                        + username
                                        )
                        );


        return new UserInfoUserDetails(user);
    }
}
