package com.example.spotserver.repository.jpa;

import com.example.spotserver.domain.QLocation;
import com.example.spotserver.domain.QLocationLike;
import com.example.spotserver.domain.QMember;
import com.example.spotserver.dto.request.LocationConditionRequest;
import com.example.spotserver.dto.response.LocationResponse;
import com.example.spotserver.dto.response.PosterResponse;
import com.example.spotserver.dto.response.QLocationResponse;
import com.example.spotserver.repository.LocationRepositoryCustom;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
@Transactional
public class LocationRepositoryImpl implements LocationRepositoryCustom {

    private EntityManager entityManager;
    private JPAQueryFactory jpaQueryFactory;
    private Double scale = 0.01;

    @Autowired
    public LocationRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<LocationResponse> getBestLocations() {

        StringPath likeCount = Expressions.stringPath("like_count");

        QLocation location = QLocation.location;
        QLocationLike locationLike = QLocationLike.locationLike;

        List<LocationResponse> bestLocations = jpaQueryFactory
                .select(new QLocationResponse(
                        location.id,
                        location.latitude,
                        location.longitude,
                        location.title,
                        location.address,
                        location.description,
                        location.regDate,
                        locationLike.count().as("like_count")
                ))
                .from(location)
                .leftJoin(locationLike).on(locationLike.location.id.eq(location.id))
                .where(location.approve.isTrue())
                .groupBy(location.id)
                .orderBy(likeCount.desc())
                .limit(5)
                .fetch();

        return bestLocations;
    }

    @Override
    public Page<LocationResponse> searchLocations(Double latitude,
                                                  Double longitude,
                                                  LocationConditionRequest conditionRequest) {

        int defaultSize = 10;
        int defaultPage = 1;

        QLocation location = QLocation.location;
        QLocationLike locationLike = QLocationLike.locationLike;

        StringPath likeCount = Expressions.stringPath("like_count");

        Pageable pageable = null;

        Integer page = conditionRequest.getPage();
        Integer size = conditionRequest.getSize();
        String sort = conditionRequest.getSort();
        String search = conditionRequest.getSearch();
        Boolean approve = conditionRequest.getApprove();

        JPAQuery<LocationResponse> searchQuery = jpaQueryFactory
                .select(new QLocationResponse(
                        location.id,
                        location.latitude,
                        location.longitude,
                        location.title,
                        location.address,
                        location.description,
                        location.regDate,
                        locationLike.count().as("like_count")
                ))
                .from(location)
                .leftJoin(locationLike).on(locationLike.location.id.eq(location.id))
                .where(location.latitude.between(latitude - scale, latitude + scale)
                        .and(location.longitude.between(longitude - scale, longitude + scale))
                        .and(location.approve.eq(approve)))
                .groupBy(location.id);

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(location.count())
                .from(location)
                .where(location.latitude.between(latitude - scale, latitude + scale)
                        .and(location.longitude.between(longitude - scale, longitude + scale))
                        .and(location.approve.eq(approve)));

        if (page != null || size != null || sort != null || search != null) {

            if (page == null)
                page = defaultPage;
            if (size == null)
                size = defaultSize;

            pageable = PageRequest.of(page - 1, size);
            searchQuery
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset());

        }

        if (sort == null || sort.equals("recent"))
            searchQuery.orderBy(location.regDate.desc());
        else if (sort.equals("like"))
            searchQuery.orderBy(likeCount.desc());
        else
            searchQuery.orderBy(location.regDate.desc());

        if (search != null) {
            searchQuery
                    .where(location.title.contains(search)
                            .or(location.description.contains(search)));
            countQuery
                    .where(location.title.contains(search)
                            .or(location.description.contains(search)));
        }

        List<LocationResponse> locations = searchQuery.fetch();

        return pageable != null ?
                PageableExecutionUtils.getPage(locations, pageable, countQuery::fetchOne) :
                new PageImpl<>(locations);
    }

    @Override
    public Optional<LocationResponse> getLocationById(Long locationId) {

        QLocation location = QLocation.location;
        QLocationLike locationLike = QLocationLike.locationLike;

        LocationResponse locationResponse = jpaQueryFactory
                .select(new QLocationResponse(
                        location.id,
                        location.latitude,
                        location.longitude,
                        location.title,
                        location.address,
                        location.description,
                        location.regDate,
                        locationLike.count().as("like_count")
                ))
                .from(location)
                .leftJoin(locationLike).on(locationLike.location.id.eq(location.id))
                .groupBy(location.id)
                .where(location.id.eq(locationId))
                .fetchOne();

        return Optional
                .ofNullable(locationResponse);
    }


    @Override
    public Page<LocationResponse> getLikeLocations(Long memberId, Pageable pageable) {

        QLocation location = QLocation.location;
        QLocationLike locationLike = QLocationLike.locationLike;

        List<LocationResponse> locations = jpaQueryFactory
                .select(new QLocationResponse(
                        location.id,
                        location.latitude,
                        location.longitude,
                        location.title,
                        location.address,
                        location.description,
                        location.regDate,
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(locationLike.count())
                                        .from(locationLike)
                                        .where(locationLike.location.id.eq(location.id)), "like_count")
                ))
                .from(locationLike)
                .leftJoin(location).on(location.id.eq(locationLike.location.id))
                .where(locationLike.member.id.eq(memberId))
                .orderBy(locationLike.regDate.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(location.count())
                .from(locationLike)
                .leftJoin(location).on(location.id.eq(locationLike.location.id))
                .where(locationLike.member.id.eq(memberId));

        return PageableExecutionUtils.getPage(locations, pageable, countQuery::fetchOne);
    }
}
