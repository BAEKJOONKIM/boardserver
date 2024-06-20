package com.fastcampus.boardserver.controller;

import com.fastcampus.boardserver.aop.LoginCheck;
import com.fastcampus.boardserver.dto.UserDTO;
import com.fastcampus.boardserver.dto.request.UserDeleteId;
import com.fastcampus.boardserver.dto.request.UserLoginRequest;
import com.fastcampus.boardserver.dto.request.UserUpdatePasswordRequest;
import com.fastcampus.boardserver.dto.response.LoginResponse;
import com.fastcampus.boardserver.dto.response.UserInfoResponse;
import com.fastcampus.boardserver.service.impl.UserServiceImpl;
import com.fastcampus.boardserver.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Log4j2
public class UserController {

    private final UserServiceImpl userService;

    @Autowired
    public UserController(UserServiceImpl userService){
        this.userService = userService;
    }

    @PostMapping("sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public void signUp(@RequestBody UserDTO userDTO){
        if(userDTO.hasNullDataBeforeRegister(userDTO)){
            throw new RuntimeException("회원가입 정보를 확인해주세요");
        }
        userService.register(userDTO);
    }

    @PostMapping("sign-in")
    public ResponseEntity login(@RequestBody UserLoginRequest userLoginRequest, HttpSession session){
        ResponseEntity<LoginResponse> responseEntity = null;

        String id = userLoginRequest.getUserId();
        String password = userLoginRequest.getPassword();
        LoginResponse loginResponse;

        UserDTO userInfo = userService.login(id, password);
        if(userInfo == null){
            responseEntity = new ResponseEntity<LoginResponse>(HttpStatus.NOT_FOUND);
            return responseEntity;
        }else if(userInfo != null){
            loginResponse = LoginResponse.success(userInfo);

            if(userInfo.getStatus() == UserDTO.Status.ADMIN){
                SessionUtil.setLoginAdminId(session, id);
            }else{
                SessionUtil.setLoginMemberId(session, id);
            }

            responseEntity = new ResponseEntity<LoginResponse>(loginResponse, HttpStatus.OK);
        }else{
            throw new RuntimeException("Login Error! 유저 정보가 없거나 지원되지 않는 유저입니다.");
        }

        return responseEntity;
    }

    @GetMapping("my-info")
    public UserInfoResponse memberInfo(HttpSession session){
        String id = SessionUtil.getLoginMemberId(session);
        if(id == null) id = SessionUtil.getLoginAdminId(session);

        UserDTO memberInfo = userService.getUserInfo(id);
        return new UserInfoResponse(memberInfo);
    }

    @PutMapping("logout")
    public void logout(HttpSession session){
        SessionUtil.clear(session);
    }

    @PatchMapping("password")
    @LoginCheck(type = LoginCheck.UserType.USER)
    public ResponseEntity<LoginResponse> updateUserPassword(String accountId, @RequestBody UserUpdatePasswordRequest passwordRequest
            , HttpSession session){
        ResponseEntity<LoginResponse> responseEntity = null;
        LoginResponse loginResponse = null;
        //String id = SessionUtil.getLoginMemberId(session);
        String id = accountId;
        String beforePassword = passwordRequest.getBeforePassword();
        String afterPassword = passwordRequest.getAfterPassword();

        try{
            userService.updatePassword(id, beforePassword, afterPassword);
            ResponseEntity.ok(new ResponseEntity<LoginResponse>(loginResponse, HttpStatus.OK));
        }catch (IllegalArgumentException e){
            log.error("updatePassword 실패", e);
            responseEntity = new ResponseEntity<LoginResponse>(HttpStatus.BAD_REQUEST);
        }

        return responseEntity;
    }

    @DeleteMapping
    public ResponseEntity<LoginResponse> deleteId(@RequestBody UserDeleteId userDeleteId
            ,HttpSession session){
        ResponseEntity<LoginResponse> responseEntity = null;
        LoginResponse loginResponse = null;
        String id = SessionUtil.getLoginMemberId(session);

        try{
            userService.deleteId(id, userDeleteId.getPassword());
            return new ResponseEntity<LoginResponse>(loginResponse, HttpStatus.OK);
        }catch (RuntimeException e){
            log.error("deleteId 실패");
            responseEntity = new ResponseEntity<LoginResponse>(HttpStatus.BAD_REQUEST);
        }

        return responseEntity;
    }

}
