package com.LunaLink.application.web.mapperImpl;

import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.infrastructure.mapper.User.UserMapper;
import com.LunaLink.application.web.dto.UserDTO.ResponseUserDTO;
import com.LunaLink.application.web.dto.UserDTO.UserSummaryDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public ResponseUserDTO toDTO(Users user) {
        if (user == null) {
            return null;
        }
        List<ResponseUserDTO.ReservationSummaryDTO> reservationDTOs =
                user.getReservations().stream()
                        .map(reservation -> new ResponseUserDTO.ReservationSummaryDTO(
                                reservation.getId(),
                                reservation.getDate(),
                                reservation.getSpace().getType().toString()
                        ))
                        .collect(Collectors.toList());
        return new ResponseUserDTO(
                user.getId(),
                user.getName(),
                user.getApartment(),
                user.getEmail(),
                user.getRole(),
                reservationDTOs
        );
    }

    @Override
    public List<ResponseUserDTO> toDTOList(List<Users> residents) {
        if (residents == null) {
            return null;
        }
        return residents.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserSummaryDTO toSummaryDTO(Users user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryDTO(user.getId(), user.getName(), user.getApartment(), user.getEmail());
    }

    @Override
    public List<UserSummaryDTO> toSummaryDTOList(List<Users> users) {
        if (users == null) {
            return null;
        }
        return users.stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
    }
}
