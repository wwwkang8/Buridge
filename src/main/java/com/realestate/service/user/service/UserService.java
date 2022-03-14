package com.realestate.service.user.service;

import java.io.IOException;

import com.realestate.service.user.constant.Role;
import com.realestate.service.user.constant.Status;
import com.realestate.service.user.dto.UserInfoDto;
import com.realestate.service.user.dto.UserSignupDto;
import com.realestate.service.user.entity.User;
import com.realestate.service.user.exception.UserNotFoundException;
import com.realestate.service.user.repository.UserRepository;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;


  @Value("${spring.sendgrid.api-key}")
  private String sendGridApiKey;

  private static final int MAX_SECRET_CODE = 999999;
  private static final int MIN_SECRET_CODE = 100000;



  /**.
   * 회원가입 : 이메일, 비밀번호를 입력하면 회원가입
   * */
  @Transactional
  public User signup(UserSignupDto userSignupDto) {

    // 비밀번호 암호화 처리
    String encodedPassword = passwordEncoder.encode(userSignupDto.getPassword());

    log.info("암호화된 비밀번호 : " + encodedPassword);


    //User 객체 빌드하기
    User user = User.builder()
        .email(userSignupDto.getEmail())
        .password(encodedPassword)
        .nickName(userSignupDto.getNickName())
        .status(Status.ACTIVE)
        .role(Role.NORMAL)
        .build();

    log.info("User 이메일 : " + user.getEmail());
    log.info("User 비밀번호 : " + encodedPassword);
    log.info("User 닉네임 : " + user.getNickName());
    log.info("User 상태 : " + user.getStatus());
    log.info("User 권한 : " + user.getRole());


    return userRepository.save(user);
  }

  /**.
   * 인증번호 전송 : 이메일 주소로 인증번호 전송.
   * */
  public String sendValidationCode(UserInfoDto userInfoDto) {

    /** 인증번호 6자리 발행 */
    int secretCode = generateSecretCode(userInfoDto);

    /** 인증번호 이메일 전송 */
    String scssYn = sendEmail(userInfoDto, secretCode);

    return scssYn;

  }

  /**
   * 비밀번호 변경 : 새로운 비밀번호로 변경.
   * */
  public User changePassword(UserInfoDto userInfoDto) {

    User findUser = findUserByEmail(userInfoDto.getEmail());


    if (userInfoDto.getSecretCode() != findUser.getValidationCode()) {
      log.info("인증코드(userInfoDto.getSecretCode()) : " + userInfoDto.getSecretCode());
      log.info("인증코드(findUser.getValidationCode()) : " + findUser.getValidationCode());

      throw new IllegalArgumentException(
          String.format("유효하지 않은 인증코드입니다. [인증코드 : %s]",userInfoDto.getSecretCode()));
    }

    String encodedPassword = passwordEncoder.encode(userInfoDto.getPassword());

    findUser.updateUserPassword(encodedPassword);

    User passwordChangedUser = userRepository.save(findUser);

    log.info("changePassword 유저 : " + passwordChangedUser.getEmail());

    return passwordChangedUser;
  }


  /**.
   * 인증코드 발행 : 비밀번호 변경을 위한 인증코드 발행
   * */
  public int generateSecretCode(UserInfoDto userInfoDto) {

    /** 6자리 난수 발생 : Math.random() 0.0<= x < 1.0 반환 --> 100,000<= x <1,000,000 숫자 채번 */
    int secretCode = (int)(Math.random() * (MAX_SECRET_CODE - MIN_SECRET_CODE)) + MIN_SECRET_CODE;

    log.info("secretCode : " + secretCode);

    /** 이메일로 사용자 조회 */
    User user = userRepository.findUserByEmail(userInfoDto.getEmail())
                          .orElseThrow(() -> new IllegalArgumentException(
                              String.format("회원정보가 존재하지 않습니다.[이메일 : %s]", userInfoDto.getEmail())
                          ));


    /** 사용자의 인증코드 업데이트 */
    user.updateUserValidationCode(secretCode);

    /** 인증코드가 업데이트 된 User 엔티티를 갱신 */
    userRepository.save(user);

    return secretCode;
  }

  /**.
   * 이메일 전송 : 사용자에게 비밀번호 인증코드 이메일 전송
   * */
  public String sendEmail(UserInfoDto userInfoDto, int secretCode) {

    String scssYn = "N"; // 메일 송신여부 N으로 초기세팅;

    /** 이메일 객체 생성 : 송신자, 수신자, 제목, 이메일 내용 */
    Email fromEmail = new Email("");
    Email toEmail = new Email("");
    String subject = "@@@@@ Buridge password validation code @@@@@@";
    String txt = "Validation code is " + secretCode;
    Content content = new Content("text/plain", txt);
    Mail mail = new Mail(fromEmail, subject, toEmail, content);

    /** 이메일 전송 */
    SendGrid sg = new SendGrid(sendGridApiKey);
    Request request = new Request();
    try {
      request.setMethod(Method.POST);
      request.setEndpoint("mail/send");
      request.setBody(mail.build());
      Response response = sg.api(request);

      log.info(String.valueOf(response.getStatusCode()));
      log.info(response.getBody());
      log.info(String.valueOf(response.getHeaders()));
      log.info(userInfoDto.getEmail() + " 비밀번호 인증코드 전송완료");

      scssYn = "Y";

    } catch (IOException e) {
      log.info("SendGrid IO Exception");
      scssYn = "N";
      e.printStackTrace();
    }

    return scssYn;


  }

  /**.
   * 사용자 조회 : 이메일을 사용하여 사용자 조회
   */
  public User findUserByEmail(String email) {

    return userRepository.findUserByEmail(email).orElseThrow(
        () -> new UserNotFoundException(email));

  }




}
