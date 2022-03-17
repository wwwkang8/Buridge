package com.realestate.service.user.jwt;

import java.util.ArrayList;
import java.util.Optional;

import com.realestate.service.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtUserDetailService implements UserDetailsService {

  private final UserService userService;


  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

    // User 엔티티를 사용하여 이메일로 유저 정보를 조회.
    com.realestate.service.user.entity.User findUser = userService.findUserByEmail(email);

    // 스프링에서 제공하는 User 객체 생성.
    if (findUser.getEmail().equals(email)) {
      return new User(findUser.getEmail(), findUser.getPassword(), new ArrayList<>());
    } else {
      throw new UsernameNotFoundException("User not found with email: " + email);
    }
  }
}
