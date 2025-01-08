package com.mysite.sbb.answer;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionDto;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api/v1/answers")
@RequiredArgsConstructor
@RestController
public class AnswerController {
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;

    @GetMapping
    public Page<AnswerDto> getAnswers(
            @RequestParam("question_id") Integer questionId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "sort", defaultValue = "createDate") String sort
    ) {
        Question question = questionService.getQuestion(questionId);
        Page<Answer> answers = answerService.getAnswers(question, page, sort);

        return convertPageToDto(answers);
    }

    @GetMapping("/{id}")
    public AnswerDto getAnswer(@PathVariable("id") Integer id) {
        Answer answer = answerService.getAnswer(id);
        return new AnswerDto(answer);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<String> create(
            @RequestBody @Valid AnswerForm answerForm,
            @RequestParam("question_id") Integer questionId,
            Principal principal
    ) {
        try {
            Question question = questionService.getQuestion(questionId);
            SiteUser siteUser = userService.getUser(principal.getName());

            answerService.create(question, answerForm.getContent(), siteUser);
            return new ResponseEntity<>("Success", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping
    public ResponseEntity<String> modify(@RequestBody @Valid AnswerForm answerForm, @RequestParam("id") Integer id, Principal principal) {
        try {
            Answer answer = answerService.getAnswer(id);
            if (!answer.getAuthor().getUsername().equals(principal.getName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
            }

            answerService.modify(answer, answerForm.getContent());
            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") Integer id, Principal principal) {
        try {
            Answer answer = answerService.getAnswer(id);
            if (!answer.getAuthor().getUsername().equals(principal.getName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
            }

            answerService.delete(answer);
            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public ResponseEntity<String> vote(@PathVariable("id") Integer id, Principal principal) {
        Answer answer = answerService.getAnswer(id);
        SiteUser siteUser = userService.getUser(principal.getName());
        answerService.vote(answer, siteUser);

        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<AnswerDto>> recent() {
        List<Answer> answers = answerService.getRecentAnswers();

        return new ResponseEntity<>(
                answers.stream()
                        .map(AnswerDto::new)
                        .collect(Collectors.toList()),
                HttpStatus.OK
        );
    }

    private Page<AnswerDto> convertPageToDto(Page<Answer> answerPage) {
        List<AnswerDto> answerDtos = answerPage.getContent().stream()
                .map(AnswerDto::new)
                .collect(Collectors.toList());

        return new PageImpl<>(answerDtos, answerPage.getPageable(), answerPage.getTotalElements());
    }
}
