package com.LunaLink.application.web.mapperImpl;

import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.infrastructure.mapper.User.UserMapper;
import com.LunaLink.application.web.dto.UserDTO.ResponseUserDTO;
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
                user.getLogin(),
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

}

