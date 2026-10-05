package com.rookies6.myspringboot4project.config.userinfo;

import com.rookies6.myspringboot4project.user.entity.User;

import lombok.Getter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


public class UserInfoUserDetails
        implements UserDetails {


    private final String email;

    private final String password;


    @Getter
    private final User user;


    public UserInfoUserDetails(User user) {

        this.user = user;

        this.email = user.getEmail();

        this.password = user.getPassword();
    }


    @Override
    public Collection<? extends GrantedAuthority>
    getAuthorities() {

        return List.of();
    }


    @Override
    public String getPassword() {

        return password;
    }


    @Override
    public String getUsername() {

        return email;
    }


    @Override
    public boolean isAccountNonExpired() {

        return true;
    }


    @Override
    public boolean isAccountNonLocked() {

        return true;
    }


    @Override
    public boolean isCredentialsNonExpired() {

        return true;
    }


    @Override
    public boolean isEnabled() {

        return true;
    }
}
