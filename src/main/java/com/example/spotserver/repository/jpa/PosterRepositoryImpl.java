package com.example.spotserver.repository.jpa;

import com.example.spotserver.domain.*;
import com.example.spotserver.dto.request.PosterConditionRequest;
import com.example.spotserver.dto.response.PosterResponse;
import com.example.spotserver.dto.response.QPosterResponse;
import com.example.spotserver.repository.PosterRepositoryCustom;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
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
import java.util.Optional;
import static com.querydsl.jpa.JPAExpressions.select;


@Repository
@Transactional
public class PosterRepositoryImpl implements PosterRepositoryCustom {

    private final EntityManager entityManager;
    private final JPAQueryFactory jpaQueryFactory;

    @Autowired
    public PosterRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<PosterResponse> searchPosters(Long locationId, PosterConditionRequest conditionRequest) {

        QPoster poster = QPoster.poster;
        QComment comment = QComment.comment;
        QPosterLike posterLike = QPosterLike.posterLike;

        StringPath likeCount = Expressions.stringPath("like_count");

        Integer page = conditionRequest.getPage();
        if (page == null)
            page = 1;
        Integer size = conditionRequest.getSize();
        if (size == null)
            size = 5;

        Pageable pageable = PageRequest.of(page - 1, size);

        JPAQuery<PosterResponse> searchQuery = jpaQueryFactory
                .select(new QPosterResponse(
                        poster.id,
                        poster.writer.id,
                        poster.writer.name,
                        poster.title,
                        poster.content,
                        poster.regDate,
                        ExpressionUtils.as(
                                select(posterLike.count())
                                        .from(posterLike)
                                        .where(posterLike.poster.id.eq(poster.id)), "like_count"),
                        ExpressionUtils.as(
                                select(comment.count())
                                        .from(comment)
                                        .where(comment.poster.id.eq(poster.id)), "comment_count")
                ))
                .from(poster)
                .where(poster.location.id.eq(locationId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(poster.count())
                .from(poster)
                .where(poster.location.id.eq(locationId));

        String sort = conditionRequest.getSort();
        if (sort.equals("recent"))
            searchQuery.orderBy(poster.regDate.desc());
        else if (sort.equals("like"))
            searchQuery.orderBy(likeCount.desc());
        else
            searchQuery.orderBy(poster.regDate.desc());

        String search = conditionRequest.getSearch();
        if (search != null) {
            searchQuery
                    .where(poster.title.contains(search)
                            .or(poster.content.contains(search)));
            countQuery
                    .where(poster.title.contains(search)
                            .or(poster.content.contains(search)));
        }

        List<PosterResponse> posters = searchQuery.fetch();
        return PageableExecutionUtils.getPage(posters, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<PosterResponse> getPosterById(Long posterId) {

        QPoster poster = QPoster.poster;
        QComment comment = QComment.comment;
        QPosterLike posterLike = QPosterLike.posterLike;

        PosterResponse posterResponse = jpaQueryFactory
                .select(new QPosterResponse(
                        poster.id,
                        poster.writer.id,
                        poster.writer.name,
                        poster.title,
                        poster.content,
                        poster.regDate,
                        select(posterLike.count())
                                .from(posterLike)
                                .where(posterLike.poster.id.eq(posterId)),
                        select(comment.count())
                                .from(comment)
                                .where(comment.poster.id.eq(posterId))
                ))
                .from(poster)
                .where(poster.id.eq(posterId))
                .fetchOne();

        return Optional
                .ofNullable(posterResponse);
    }

    @Override
    public List<PosterResponse> getBestPosters() {
        QPoster poster = QPoster.poster;
        QComment comment = QComment.comment;
        QPosterLike posterLike = QPosterLike.posterLike;

        StringPath likeCount = Expressions.stringPath("like_count");

        List<PosterResponse> bestPosters = jpaQueryFactory
                .select(new QPosterResponse(
                        poster.id,
                        poster.writer.id,
                        poster.writer.name,
                        poster.title,
                        poster.content,
                        poster.regDate,
                        ExpressionUtils.as(
                                select(posterLike.count())
                                        .from(posterLike)
                                        .where(posterLike.poster.id.eq(poster.id)), "like_count"),
                        ExpressionUtils.as(
                                select(comment.count())
                                        .from(comment)
                                        .where(comment.poster.id.eq(poster.id)), "comment_count")
                ))
                .from(poster)
                .orderBy(likeCount.desc())
                .limit(5)
                .fetch();

        return bestPosters;
    }

    @Override
    public Page<PosterResponse> getLikePosters(Long memberId, Pageable pageable) {

        QPoster poster = QPoster.poster;
        QComment comment = QComment.comment;
        QPosterLike posterLike = QPosterLike.posterLike;

        List<PosterResponse> posters = jpaQueryFactory
                .select(new QPosterResponse(
                        poster.id,
                        poster.writer.id,
                        poster.writer.name,
                        poster.title,
                        poster.content,
                        poster.regDate,
                        ExpressionUtils.as(
                                select(posterLike.count())
                                        .from(posterLike)
                                        .where(posterLike.poster.id.eq(poster.id)), "like_count"),
                        ExpressionUtils.as(
                                select(comment.count())
                                        .from(comment)
                                        .where(comment.poster.id.eq(poster.id)), "comment_count")
                ))
                .from(posterLike)
                .leftJoin(poster).on(poster.id.eq(posterLike.poster.id))
                .where(posterLike.member.id.eq(memberId))
                .orderBy(posterLike.regDate.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(poster.count())
                .from(posterLike)
                .leftJoin(poster).on(poster.id.eq(posterLike.poster.id))
                .where(posterLike.member.id.eq(memberId));

        return PageableExecutionUtils.getPage(posters, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<PosterResponse> getWritePosters(Long memberId, Pageable pageable) {

        QPoster poster = QPoster.poster;
        QPosterLike posterLike = QPosterLike.posterLike;
        QComment comment = QComment.comment;

        List<PosterResponse> posters = jpaQueryFactory
                .select(new QPosterResponse(
                        poster.id,
                        poster.writer.id,
                        poster.writer.name,
                        poster.title,
                        poster.content,
                        poster.regDate,
                        ExpressionUtils.as(
                                select(posterLike.count())
                                        .from(posterLike)
                                        .where(posterLike.poster.id.eq(poster.id)), "like_count"),
                        ExpressionUtils.as(
                                select(comment.count())
                                        .from(comment)
                                        .where(comment.poster.id.eq(poster.id)), "comment_count")
                ))
                .from(poster)
                .where(poster.writer.id.eq(memberId))
                .orderBy(poster.regDate.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(poster.count())
                .from(poster)
                .where(poster.writer.id.eq(memberId));

        return PageableExecutionUtils.getPage(posters, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<PosterResponse> getPostersByWriteComments(Long memberId, Pageable pageable) {

        QPoster poster = QPoster.poster;
        QPosterLike posterLike = QPosterLike.posterLike;
        QComment comment = QComment.comment;

        // DISTINCT -> ORDER BY -> OFFSET, LIMIT
        // GROUP BY -> ORDER BY -> LIMIT
        List<PosterResponse> posters = jpaQueryFactory
                .select(new QPosterResponse(
                        poster.id,
                        poster.writer.id,
                        poster.writer.name,
                        poster.title,
                        poster.content,
                        poster.regDate,
                        ExpressionUtils.as(
                                select(posterLike.count())
                                        .from(posterLike)
                                        .where(posterLike.poster.id.eq(poster.id)), "like_count"),
                        ExpressionUtils.as(
                                select(comment.count())
                                        .from(comment)
                                        .where(comment.poster.id.eq(poster.id)), "comment_count")))
                .from(poster)
                .leftJoin(comment).on(comment.poster.id.eq(poster.id))
                .where(comment.writer.id.eq(memberId))
                .groupBy(poster.id)
                .orderBy(comment.regDate.max().desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(comment.poster.id.countDistinct())
                .from(comment)
                .where(comment.writer.id.eq(memberId));

        return PageableExecutionUtils.getPage(posters, pageable, countQuery::fetchOne);
    }
}