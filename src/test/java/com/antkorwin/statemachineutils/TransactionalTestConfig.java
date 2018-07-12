package com.antkorwin.statemachineutils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created on 12.07.2018.
 *
 * Transaction Persistence layer configuration,
 * for testing transactional methods in wrappers and service.
 *
 * @author Korovin Anatoliy
 */
@TestConfiguration
@EnableJpaRepositories(considerNestedRepositories = true)
@EntityScan("com.antkorwin.statemachineutils")
public class TransactionalTestConfig {

    @Repository
    public interface FooRepository extends JpaRepository<Foo, Long> {

    }

    @Entity
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Foo {
        @Id
        @GeneratedValue
        private Long id;

        @Column(nullable = false)
        private String field;
    }

    @Service
    public class TestService {
        @Autowired
        private FooRepository fooRepository;

        public Foo ok() {
            Foo foo = new Foo();
            foo.setField("123");
            return fooRepository.save(foo);
        }

        public void fail() {
            fooRepository.save(new Foo());
        }

        public long size() {
            return fooRepository.count();
        }

        public void clear() {
            fooRepository.deleteAll();
        }
    }
}