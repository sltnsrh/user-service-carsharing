package com.intern.carsharing.controller;

import com.intern.carsharing.model.dto.request.RequestUserUpdateDto;
import com.intern.carsharing.model.dto.response.ResponseUserDto;
import com.intern.carsharing.service.UserService;
import com.intern.carsharing.service.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
            description = "Allows to get a user info.",
            tags = {"Users"},
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Ok",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseUserDto.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "404", description = "Not Found"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ResponseUserDto> getUserInfo(
            @Parameter(description = "User id", example = "1")
            @PathVariable("id") Long id
    ) {
        return new ResponseEntity<>(userMapper.toDto(userService.get(id)), HttpStatus.OK);
    }

    @Operation(
            summary = "Update a user info",
            description = "Allows to update a info about user.",
            tags = {"Users"},
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Ok",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseUserDto.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not Found"),
                    @ApiResponse(responseCode = "405", description = "Method Not Allowed"),
                    @ApiResponse(responseCode = "409", description = "Conflict")
            })
    @PutMapping("/{id}")
    public ResponseEntity<ResponseUserDto> update(
            @Parameter(description = "User id", example = "1")
            @PathVariable("id") Long id,
            @Valid @RequestBody RequestUserUpdateDto requestDto
    ) {
        return new ResponseEntity<>(
                userMapper.toDto((userService.update(id, requestDto))),
                HttpStatus.OK
        );
    }
}
