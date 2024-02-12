package com.example.spotserver.repository.jpa;

import com.example.spotserver.domain.QLocation;
import com.example.spotserver.domain.QLocationLike;
import com.example.spotserver.dto.response.LocationResponse;
import com.example.spotserver.repository.LocationRepositoryCustom;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@Transactional
public class LocationRepositoryImpl implements LocationRepositoryCustom {

    private EntityManager entityManager;
    private JPAQueryFactory jpaQueryFactory;

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
                .select(Projections.constructor(LocationResponse.class,
                        location.id,
                        location.latitude,
                        location.longitude,
                        location.title,
                        location.address,
                        location.description,
                        locationLike.count().as("like_count")))
                .from(location)
                .leftJoin(locationLike).on(locationLike.location.id.eq(location.id))
                .groupBy(location.id)
                .orderBy(likeCount.desc())
                .limit(5)
                .fetch();

        return bestLocations;
    }
}
