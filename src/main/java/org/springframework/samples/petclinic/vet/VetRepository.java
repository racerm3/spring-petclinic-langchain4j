/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.vet;

import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository class for <code>Vet</code> domain objects All method names are compliant
 * with Spring Data naming conventions so this interface can easily be extended for Spring
 * Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
public interface VetRepository extends JpaRepository<Vet, Integer> {

	/**
	 * Retrieve all <code>Vet</code>s from the data store.
	 * @return a <code>Collection</code> of <code>Vet</code>s
	 */
	@Override
	@Transactional(readOnly = true)
	@Cacheable("vets")
	@NonNull
	List<Vet> findAll();

	/**
	 * Save a <code>Vet</code> to the data store, either inserting or updating it.
	 * @param vet the entity to save
	 * @return the saved entity
	 */
	@Override
	@Transactional
	@CacheEvict(value = "vets", allEntries = true)
	@NonNull
	<S extends Vet> S save(@NonNull S vet);

	/**
	 * Count the number of <code>Vet</code>s in the data store.
	 */
	@Transactional(readOnly = true)
	@Query("SELECT COUNT(v) FROM Vet v")
	Integer countVets();

	@Override
	@Transactional(readOnly = true)
	@Cacheable("vets")
	@NonNull
	Page<Vet> findAll(@NonNull Pageable pageable);

	/**
	 * Retrieve all <code>Vet</code>s from data store in Pages by last name and specialty.
	 * @param lastName last name to search for
	 * @param specialtyId specialty ID to filter by
	 * @param pageable
	 * @return a Page of matching <code>Vet</code>s
	 */
	@Transactional(readOnly = true)
	@Query("SELECT DISTINCT v FROM Vet v LEFT JOIN v.specialties s WHERE "
			+ "(:lastName IS NULL OR LOWER(v.lastName) LIKE LOWER(CONCAT(:lastName, '%'))) AND "
			+ "(:specialtyId IS NULL OR s.id = :specialtyId)")
	Page<Vet> findByLastNameAndSpecialty(@org.springframework.data.repository.query.Param("lastName") String lastName,
			@org.springframework.data.repository.query.Param("specialtyId") Integer specialtyId, Pageable pageable);

}
