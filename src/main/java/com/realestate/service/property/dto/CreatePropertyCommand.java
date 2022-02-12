package com.realestate.service.property.dto;

import java.time.LocalDate;

import com.realestate.service.property.address.entity.PropertyAddress;
import com.realestate.service.property.constant.ResidentialType;
import com.realestate.service.property.constant.StructureType;
import com.realestate.service.property.entity.Property;
import com.realestate.service.property.entity.PropertyFloor;
import com.realestate.service.property.entity.PropertyInformation;
import com.realestate.service.property.entity.PropertyPrice;
import com.realestate.service.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreatePropertyCommand {

  private final long userId;
  private final PropertyInformationCommand propertyInformationCommand;
  private final PropertyAddressCommand propertyAddressCommand;

  /**
   * CreatePropertyCommand 를 생성하여 리턴합니다.
   */
  public CreatePropertyCommand(long userId,
                               PropertyInformationCommand propertyInformationCommand,
                               PropertyAddressCommand propertyAddressCommand) {
    this.userId = userId;
    this.propertyInformationCommand = propertyInformationCommand;
    this.propertyAddressCommand = propertyAddressCommand;
  }

  @Getter
  public static class PropertyInformationCommand {
    private final String title;
    private final String content;
    private final int area;
    private final StructureType structureType;
    private final Long sellPrice;
    private final Long deposit;
    private final Integer monthlyPrice;
    private final Integer adminPrice;
    private final ResidentialType residentialType;
    private final int floor;
    private final int topFloor;
    private final Boolean availableParking;
    private final LocalDate moveInDate;
    private final LocalDate estDate;

    /**
     * PropertyInformationCommand 를 생성하여 리턴합니다.
     */
    @Builder
    public PropertyInformationCommand(String title,
                                      String content,
                                      int area,
                                      StructureType structureType,
                                      Long sellPrice,
                                      Long deposit,
                                      Integer monthlyPrice,
                                      Integer adminPrice,
                                      ResidentialType residentialType,
                                      int floor,
                                      int topFloor,
                                      Boolean availableParking,
                                      LocalDate moveInDate,
                                      LocalDate estDate) {
      this.title = title;
      this.content = content;
      this.area = area;
      this.structureType = structureType;
      this.sellPrice = sellPrice;
      this.deposit = deposit;
      this.monthlyPrice = monthlyPrice;
      this.adminPrice = adminPrice;
      this.residentialType = residentialType;
      this.floor = floor;
      this.topFloor = topFloor;
      this.availableParking = availableParking;
      this.moveInDate = moveInDate;
      this.estDate = estDate;
    }
  }

  @Getter
  public static class PropertyAddressCommand {
    private final String city;
    private final String address;
    private final String roadAddress;
    private final String zipcode;
    private final double latitude;
    private final double longitude;

    /**
     * PropertyAddressCommand 를 생성하여 리턴합니다.
     */
    @Builder
    public PropertyAddressCommand(String city,
                                  String address,
                                  String roadAddress,
                                  String zipcode,
                                  double latitude,
                                  double longitude) {
      this.city = city;
      this.address = address;
      this.roadAddress = roadAddress;
      this.zipcode = zipcode;
      this.latitude = latitude;
      this.longitude = longitude;
    }
  }


  /**
   * 매물 주소 엔티티를 반환합니다.
   */
  public PropertyAddress toEntity(Property property) {
    return PropertyAddress.builder()
        .property(property)
        .city(propertyAddressCommand.getCity())
        .address(propertyAddressCommand.getAddress())
        .roadAddress(propertyAddressCommand.getRoadAddress())
        .zipcode(propertyAddressCommand.getZipcode())
        .latitude(propertyAddressCommand.getLatitude())
        .longitude(propertyAddressCommand.getLongitude())
        .build();
  }

  /**
   * 매물 엔티티를 반환합니다.
   */
  public Property toEntity(User user) {
    return Property.builder()
        .user(user)
        .title(propertyInformationCommand.getTitle())
        .content(propertyInformationCommand.getContent())
        .propertyInformation(createPropertyInformation())
        .build();
  }

  private PropertyInformation createPropertyInformation() {
    return PropertyInformation.builder()
        .propertyPrice(createPropertyPrice())
        .propertyFloor(createPropertyFloor())
        .structureType(propertyInformationCommand.getStructureType())
        .availableParking(propertyInformationCommand.getAvailableParking())
        .moveInDate(propertyInformationCommand.getMoveInDate())
        .estDate(propertyInformationCommand.getEstDate())
        .area(propertyInformationCommand.getArea())
        .residentialType(propertyInformationCommand.getResidentialType())
        .build();
  }

  private PropertyPrice createPropertyPrice() {
    return PropertyPrice.builder()
        .adminPrice(propertyInformationCommand.getAdminPrice())
        .monthlyPrice(propertyInformationCommand.getMonthlyPrice())
        .deposit(propertyInformationCommand.getDeposit())
        .sellPrice(propertyInformationCommand.getSellPrice())
        .build();
  }

  private PropertyFloor createPropertyFloor() {
    return new PropertyFloor(propertyInformationCommand.getFloor(),
        propertyInformationCommand.getTopFloor());
  }

}
