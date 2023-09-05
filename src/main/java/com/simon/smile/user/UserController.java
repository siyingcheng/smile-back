package com.simon.smile.user;

import com.simon.smile.common.Result;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.base-url}/users")
public class UserController {
    private final UserService userService;

    private final UserToUserDtoConverter userToUserDtoConverter;

    public UserController(UserService userService, UserToUserDtoConverter userToUserDtoConverter) {
        this.userService = userService;
        this.userToUserDtoConverter = userToUserDtoConverter;
    }

    @GetMapping("/{id}")
    public Result findUserById(@PathVariable Integer id) {
        return Result.success()
                .setCode(HttpStatus.OK.value())
                .setMessage("Find user success")
                .setData(userToUserDtoConverter.convert(userService.findById(id)));
    }

    @GetMapping
    public Result findUsers() {
        List<UserDto> userDtoList = userService.findAll()
                .stream()
                .map(userToUserDtoConverter::convert)
                .collect(Collectors.toList());
        return Result.success()
                .setCode(HttpStatus.OK.value())
                .setMessage("Find all users success")
                .setData(userDtoList);
    }

    @PostMapping
    public Result createUser(@RequestBody @Valid AppUser appUser) {
        validatePassword(appUser.getPassword());
        AppUser savedUser = userService.create(appUser);
        return Result.success()
                .setCode(HttpStatus.OK.value())
                .setMessage("Create user success")
                .setData(userToUserDtoConverter.convert(savedUser));
    }


    private void validatePassword(String password) {
        /**
         * - (?=.*[0-9])：at least a number
         * - (?=.*[a-z])：at least a lower letter
         * - (?=.*[A-Z])：at least a upper letter
         * - (?=\\S+$)：no spaces
         * - .{8,20}：at least 8 characters, at most 20 characters
         */
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,20}$";
        if (!password.matches(passwordRegex)) {
            throw new IllegalArgumentException("Password is not strong enough; 1. At least a number; 2. A least a lower letter; 3. At least a upper letter; 4. No spaces; 5. At least 8 characters, at most 20 characters");
        }
    }
}
