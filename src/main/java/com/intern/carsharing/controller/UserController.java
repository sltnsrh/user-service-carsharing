package com.intern.carsharing.controller;

import com.intern.carsharing.model.dto.request.BalanceRequestDto;
import com.intern.carsharing.model.dto.request.CarRegistrationRequestDto;
import com.intern.carsharing.model.dto.request.ChangeCarStatusRequestDto;
import com.intern.carsharing.model.dto.request.ChangeStatusRequestDto;
import com.intern.carsharing.model.dto.request.UserUpdateRequestDto;
import com.intern.carsharing.model.dto.response.StatisticsResponseDto;
import com.intern.carsharing.model.dto.response.UserResponseDto;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.service.UserService;
import com.intern.carsharing.service.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "Get and update user info")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(
            summary = "Get a user info",
            description = "Allows to get a user info. Admin has access to all accounts, "
                    + "but users can get info only about themselves.",
            tags = {"Users"},
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Ok",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserResponseDto.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "404", description = "Not Found"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    @PreAuthorize("'ACTIVE' == authentication.details.status.statusType.name")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserInfo(
            @Parameter(description = "User id", example = "1")
            @PathVariable("id") Long id
    ) {
        return new ResponseEntity<>(userMapper.toDto(userService.get(id)), HttpStatus.OK);
    }

    @Operation(
            summary = "Update a user info",
            description = "Allows to update a info about user. Admin has access to all accounts, "
                    + "but users can update info only about themselves.",
            tags = {"Users"},
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Ok",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserResponseDto.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not Found"),
                    @ApiResponse(responseCode = "405", description = "Method Not Allowed"),
                    @ApiResponse(responseCode = "409", description = "Conflict")
            })
    @PreAuthorize("'ACTIVE' == authentication.details.status.statusType.name")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> update(
            @Parameter(description = "User id", example = "1")
            @PathVariable("id") Long id,
            @Valid @RequestBody UserUpdateRequestDto requestDto
    ) {
        return new ResponseEntity<>(
                userMapper.toDto((userService.update(id, requestDto))),
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "Change user status",
            description = "Allows to update a user status. Permission only for Admin",
            tags = {"Users"},
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Ok",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserResponseDto.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserResponseDto> changeStatus(
            @Parameter(description = "User id", example = "1")
            @PathVariable("id") Long id,
            @Valid @RequestBody ChangeStatusRequestDto requestDto
    ) {
        StatusType statusType = StatusType.valueOf(requestDto.getStatus().toUpperCase());
        return new ResponseEntity<>(
                userMapper.toDto(userService.changeStatus(id, statusType)),
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "Credit user balance",
            description = "Allows to put money on a user balance. Users with USER and CAR_OWNER "
                    + "roles can charge only their own balances. "
                    + "Users with ADMIN role can charge every existing balance",
            tags = {"Users"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })

    @PatchMapping("/{id}/to-balance")
    @PreAuthorize("'ACTIVE' == authentication.details.status.statusType.name")
    public ResponseEntity<String> toBalance(
            @PathVariable("id") Long id,
            @Valid @RequestBody BalanceRequestDto requestDto
    ) {
        return new ResponseEntity<>(userService.toBalance(id, requestDto), HttpStatus.OK);
    }

    @Operation(
            summary = "Debit user balance",
            description = "Allows to get money from a user balance. "
                    + "Users with ADMIN role can get from every existing balance",
            tags = {"Users"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })

    @PatchMapping("/{id}/from-balance")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> fromBalance(
            @PathVariable("id") Long id,
            @Valid @RequestBody BalanceRequestDto requestDto
    ) {
        return new ResponseEntity<>(userService.fromBalance(id, requestDto), HttpStatus.OK);
    }

    @Operation(
            summary = "Get trip user statistics",
            description = "Allows to get statistics about user trips.",
            tags = {"Users"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/{id}/statistics")
    public ResponseEntity<List<StatisticsResponseDto>> getStatistics(
            @PathVariable("id") Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateStart,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateEnd
    ) {
        return new ResponseEntity<>(userService.getTripStatistics(id, dateStart, dateEnd),
                HttpStatus.OK);
    }

    @Operation(
            summary = "Get car statistics",
            description = "Allows to get car statistics for car owners",
            tags = {"Users"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })

    @PreAuthorize("hasAuthority('CAR_OWNER')")
    @GetMapping("/{userId}/cars/{carId}")
    public ResponseEntity<Object> getCarStatistics(@PathVariable("userId") Long userId,
                                                   @PathVariable("carId") Long carId) {
        return new ResponseEntity<>(userService.getCarStatistics(userId, carId), HttpStatus.OK);
    }

    @Operation(
            summary = "Add a car to a rent",
            description = "Allows car owners to add a new car to a rent.",
            tags = {"Users"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })

    @PreAuthorize("hasAuthority('CAR_OWNER')")
    @PostMapping("/{userId}/cars")
    public ResponseEntity<Object> addCarToRent(
            @PathVariable("userId") Long userId,
            @RequestBody CarRegistrationRequestDto requestDto
    ) {
        return new ResponseEntity<>(userService.addCarToRent(userId, requestDto), HttpStatus.OK);
    }

    @Operation(
            summary = "Change your car status",
            description = "Allows car owners to change car status by car id.",
            tags = {"Users"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })

    @PreAuthorize("hasAuthority('CAR_OWNER')")
    @PatchMapping("/{userId}/cars/{carId}")
    public ResponseEntity<Object> changeCarStatus(
            @PathVariable("userId") Long userId,
            @PathVariable("carId") Long carId,
            @RequestBody ChangeCarStatusRequestDto requestDto
    ) {
        return new ResponseEntity<>(userService.changeCarStatus(userId, carId, requestDto),
                HttpStatus.OK);
    }
}
