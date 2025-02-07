package com.mysite.sbb.question;

import com.mysite.sbb.category.Category;
import com.mysite.sbb.user.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    Page<Question> findAll(Pageable pageable);

    Optional<Question> findById(Integer id);

    Question findBySubject(String subject);

    Question findBySubjectAndContent(String subject, String content);

    List<Question> findBySubjectLike(String subject);

    List<Question> findAllByAuthor(SiteUser user);

    @Query("select "
            + "distinct q "
            + "from Question q "
            + "left outer join SiteUser u1 on q.author=u1 "
            + "left outer join Answer a on a.question=q "
            + "left outer join SiteUser u2 on a.author=u2 "
            + "where "
            + "    q.category = :category and "
            + "    (q.subject like %:kw% "
            + "    or q.content like %:kw% "
            + "    or u1.username like %:kw% "
            + "    or a.content like %:kw% "
            + "    or u2.username like %:kw% )")
    Page<Question> findAllByCategoryAndKeyword(Category category, @Param("kw") String kw, Pageable pageable);
}
