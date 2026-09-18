package com.example.tradelens.repository;


import com.example.tradelens.entity.Execution;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ExecutionRepository extends JpaRepository<Execution, Long>{

}
