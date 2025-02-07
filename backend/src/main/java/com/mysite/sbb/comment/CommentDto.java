package com.mysite.sbb.comment;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentDto {
    private Integer id;
    private String content;
    private LocalDateTime createDate;
    private String authorName;
    private Integer questionId;

    public CommentDto(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.createDate = comment.getCreateDate();
        this.authorName = comment.getAuthor().getUsername();

        if (comment.getQuestion() != null) {
            this.questionId = comment.getQuestion().getId();
        } else if (comment.getAnswer() != null) {
            this.questionId = comment.getAnswer().getQuestion().getId();
        }
    }
}
