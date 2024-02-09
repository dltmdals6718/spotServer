package com.example.spotserver.repository.jpa;

import com.example.spotserver.domain.*;
import com.example.spotserver.dto.response.PosterResponse;
import com.example.spotserver.repository.PosterRepositoryCustom;
import com.querydsl.core.types.Projections;
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
        QLocation location = QLocation.location;
        QComment comment = QComment.comment;
        QPosterLike posterLike = QPosterLike.posterLike;

        List<PosterResponse> posters = jpaQueryFactory
                .select(Projections.constructor(PosterResponse.class,
                        poster.id,
                        poster.writer.id,
                        poster.title,
                        poster.content,
                        poster.regDate,
                        posterLike.count(),
                        comment.count()
                ))
                .from(poster)
                .innerJoin(location).on(location.id.eq(locationId))
                .leftJoin(comment).on(comment.poster.id.eq(poster.id))
                .leftJoin(posterLike).on(posterLike.poster.id.eq(poster.id))
                .groupBy(poster.id)
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
        QLocation location = QLocation.location;
        QComment comment = QComment.comment;
        QPosterLike posterLike = QPosterLike.posterLike;

        List<PosterResponse> posters = jpaQueryFactory
                .select(Projections.constructor(PosterResponse.class,
                        poster.id,
                        poster.writer.id,
                        poster.title,
                        poster.content,
                        poster.regDate,
                        posterLike.count(),
                        comment.count()
                ))
                .from(poster)
                .innerJoin(location).on(location.id.eq(locationId))
                .leftJoin(comment).on(comment.poster.id.eq(poster.id))
                .leftJoin(posterLike).on(posterLike.poster.id.eq(poster.id))
                .groupBy(poster.id)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(posterLike.count().desc())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(poster.count())
                .from(poster)
                .where(poster.location.id.eq(locationId));

        return PageableExecutionUtils.getPage(posters, pageable, countQuery::fetchOne);
    }

}
