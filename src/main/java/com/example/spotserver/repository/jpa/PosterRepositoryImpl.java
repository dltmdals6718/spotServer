package com.example.spotserver.repository.jpa;

import com.example.spotserver.domain.*;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


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
    public Page<PosterResponse> searchPostersByRecent(Long locationId, Pageable pageable) {
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
                        JPAExpressions
                                .select(posterLike.count())
                                .from(posterLike)
                                .where(posterLike.poster.id.eq(poster.id)),
                        JPAExpressions
                                .select(comment.count())
                                .from(comment)
                                .where(comment.poster.id.eq(poster.id))
                ))
                .from(poster)
                .where(poster.location.id.eq(locationId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(poster.regDate.desc())
                .fetch();

        // COUNT 쿼리를 따로 날려서 조인을 탈 필요가 없는 경우 성능 이점.
        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(poster.count())
                .from(poster)
                .where(poster.location.id.eq(locationId));

        // new PageImpl<>()와 달리 CountQuery 최적화 가능.
        return PageableExecutionUtils.getPage(posters, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<PosterResponse> searchPostersByLike(Long locationId, Pageable pageable) {

        QPoster poster = QPoster.poster;
        QComment comment = QComment.comment;
        QPosterLike posterLike = QPosterLike.posterLike;

        StringPath likeCount = Expressions.stringPath("like_count");

        List<PosterResponse> posters = jpaQueryFactory
                .select(new QPosterResponse(
                        poster.id,
                        poster.writer.id,
                        poster.writer.name,
                        poster.title,
                        poster.content,
                        poster.regDate,
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(posterLike.count())
                                        .from(posterLike)
                                        .where(posterLike.poster.id.eq(poster.id)), "like_count"),
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(comment.count())
                                        .from(comment)
                                        .where(comment.poster.id.eq(poster.id)), "comment_count")
                ))
                .from(poster)
                .where(poster.location.id.eq(locationId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(likeCount.desc())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(poster.count())
                .from(poster)
                .where(poster.location.id.eq(locationId));

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
                        JPAExpressions
                                .select(posterLike.count())
                                .from(posterLike)
                                .where(posterLike.poster.id.eq(posterId)),
                        JPAExpressions
                                .select(comment.count())
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
                                JPAExpressions
                                        .select(posterLike.count())
                                        .from(posterLike)
                                        .where(posterLike.poster.id.eq(poster.id)), "like_count"),
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(comment.count())
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
                                JPAExpressions
                                        .select(posterLike.count())
                                        .from(posterLike)
                                        .where(posterLike.poster.id.eq(poster.id)), "like_count"),
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(comment.count())
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
}
