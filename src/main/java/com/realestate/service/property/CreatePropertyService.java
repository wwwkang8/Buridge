package com.realestate.service.property;

import com.realestate.service.property.address.entity.PropertyAddressRepository;
import com.realestate.service.property.entity.Property;
import com.realestate.service.property.entity.PropertyRepository;
import com.realestate.service.user.entity.User;
import com.realestate.service.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CreatePropertyService implements CreatePropertyUseCase {

  private final PropertyRepository propertyRepository;
  private final PropertyAddressRepository propertyAddressRepository;
  private final UserRepository userRepository;

  /**
   * 매물 정보로 매물을 등록하고 반환합니다.
   *
   * @param command 등록할 매물 정보
   * @return 등록된 매물
   * @throws IllegalArgumentException 회원 식별자로 회원 정보를 찾을 수 없을 경우
   */
  @Transactional
  @Override
  public Property create(CreatePropertyCommand command) {
    User findUser = findUserById(command.getUserId());
    Property savedProperty = propertyRepository.save(command.toEntity(findUser));
    propertyAddressRepository.save(command.toAddressEntity(savedProperty));
    return savedProperty;
  }

  private User findUserById(long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("회원 정보가 존재하지 않습니다.[회원 번호 : %d]", userId)
        ));
  }
}
