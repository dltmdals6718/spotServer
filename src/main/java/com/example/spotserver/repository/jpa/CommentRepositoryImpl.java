package com.example.spotserver.repository.jpa;

import com.example.spotserver.domain.QComment;
import com.example.spotserver.domain.QCommentLike;
import com.example.spotserver.domain.QMember;
import com.example.spotserver.domain.QMemberImage;
import com.example.spotserver.dto.request.CommentConditionRequest;
import com.example.spotserver.dto.response.CommentResponse;
import com.example.spotserver.dto.response.QCommentResponse;
import com.example.spotserver.repository.CommentRepositoryCustom;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@Transactional
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private EntityManager entityManager;
    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    public CommentRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<CommentResponse> getComments(Long posterId, CommentConditionRequest commentConditionRequest) {

        QCommentLike commentLike = QCommentLike.commentLike;
        QComment comment = QComment.comment;
        QMember member = QMember.member;
        QMemberImage memberImage = QMemberImage.memberImage;

        StringPath likeCount = Expressions.stringPath("like_count");

        JPAQuery<CommentResponse> commentQuery = jpaQueryFactory
                .select(new QCommentResponse(
                        comment.id,
                        member.id,
                        member.name,
                        memberImage.storeFileName,
                        comment.content,
                        comment.regDate,
                        commentLike.count().as("like_count")
                ))
                .from(comment)
                .join(member).on(member.id.eq(comment.writer.id))
                .leftJoin(memberImage).on(memberImage.member.id.eq(member.id))
                .leftJoin(commentLike).on(commentLike.comment.id.eq(comment.id))
                .where(comment.poster.id.eq(posterId))
                .groupBy(comment.id, memberImage.storeFileName);

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(comment.count())
                .from(comment)
                .where(comment.poster.id.eq(posterId));


        Pageable pageable = null;
        Integer page = commentConditionRequest.getPage();
        Integer size = commentConditionRequest.getSize();
        String sort = commentConditionRequest.getSort();

        if (page == null)
            page = 1;
        if (size == null)
            size = 5;

        pageable = PageRequest.of(page - 1, size);

        commentQuery
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        if (sort == null || sort.equals("recent"))
            commentQuery.orderBy(comment.regDate.desc());
        else if (sort.equals("like"))
            commentQuery.orderBy(likeCount.desc());
        else
            commentQuery.orderBy(comment.regDate.desc());

        List<CommentResponse> comments = commentQuery.fetch();

        return PageableExecutionUtils.getPage(comments, pageable, countQuery::fetchOne);

    }

}
