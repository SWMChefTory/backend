package com.cheftory.api;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Rollback(false)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DataJpaTest
public abstract class DbContextTest {

}