package com.ibmecmall.repository;

import com.ibmecmall.model.CardModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CardRepository extends JpaRepository<CardModel, Integer> {
}