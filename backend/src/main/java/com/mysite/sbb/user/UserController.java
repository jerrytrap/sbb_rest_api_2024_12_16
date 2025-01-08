package com.mysite.sbb.user;

import com.mysite.sbb.answer.AnswerDto;
import com.mysite.sbb.comment.CommentDto;
import com.mysite.sbb.global.ResponseDto;
import com.mysite.sbb.question.QuestionDto;
import com.mysite.sbb.util.DataNotFoundException;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.comment.Comment;
import com.mysite.sbb.comment.CommentService;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.util.LoginFailException;
import com.mysite.sbb.util.UserConflictException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.InputMismatchException;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final CommentService commentService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseDto<UserResponseDto> getUser(Principal principal) {
        try {
            SiteUser siteUser = userService.getUser(principal.getName());

            return new ResponseDto<>(
                    200,
                    "success",
                    new UserResponseDto(
                            siteUser.getUsername(),
                            siteUser.getEmail()
                    )
            );
        } catch (Exception e) {
            throw new LoginFailException(e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseDto<Void> signup(@RequestBody @Valid UserCreateForm userCreateForm) {
        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            return new ResponseDto<>(400, "2개의 패스워드가 일치하지 않습니다.");
        }

        try {
            userService.create(userCreateForm.getUsername(), userCreateForm.getEmail(), userCreateForm.getPassword1());
        } catch (DataIntegrityViolationException e) {
            throw new UserConflictException("아이디나 이메일이 이미 존재합니다.");
        }

        return new ResponseDto<>(HttpStatus.CREATED.value(), "회원가입 성공");
    }

    @GetMapping("/status")
    public ResponseEntity<String> checkLogin(HttpSession session) {
        SecurityContext securityContext = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");

        if (securityContext != null) {
            Authentication authentication = securityContext.getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                return new ResponseEntity<>(authentication.getName(), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("unauthenticated", HttpStatus.UNAUTHORIZED);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/questions")
    public List<QuestionDto> getQuestionsByUser(@RequestBody UserRequestDto user, Principal principal) {
        if (!user.getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "조회 권한이 없습니다.");
        }

        SiteUser siteUser = userService.getUser(principal.getName());
        List<Question> questionList = questionService.getQuestions(siteUser);

        return questionList
                .stream()
                .map(QuestionDto::new)
                .collect(Collectors.toList());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/answers")
    public List<AnswerDto> getAnswersByUser(@RequestBody UserRequestDto user, Principal principal) {
        if (!user.getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "조회 권한이 없습니다.");
        }

        SiteUser siteUser = userService.getUser(principal.getName());
        List<Answer> answerList = answerService.getAnswers(siteUser);

        return answerList
                .stream()
                .map(AnswerDto::new)
                .collect(Collectors.toList());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/comments")
    public List<CommentDto> getCommentsByUser(@RequestBody UserRequestDto user, Principal principal) {
        if (!user.getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "조회 권한이 없습니다.");
        }

        SiteUser siteUser = userService.getUser(principal.getName());
        List<Comment> commentList = commentService.getComments(siteUser);

        return commentList
                .stream()
                .map(CommentDto::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/email")
    public ResponseEntity<String> getTempPassword(@RequestBody EmailRequestDto emailDto) {
        try {
            SiteUser user = userService.getUserByEmail(emailDto.getEmail());
            userService.sendTemporaryPassword(user);
            return new ResponseEntity<>("success", HttpStatus.OK);
        } catch (DataNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/password_change")
    public ResponseEntity<String> passwordChange(
            @RequestBody PasswordRequestDto passwordRequestDto,
            Principal principal) {
        try {
            SiteUser user = userService.getUser(principal.getName());
            userService.changePassword(user, passwordRequestDto.getCurrentPassword(), passwordRequestDto.getNewPassword());
            return new ResponseEntity<>("success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
