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
package org.springframework.samples.petclinic.owner;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository class for <code>Pet</code> domain objects All method names are compliant
 * with Spring Data naming conventions so this interface can easily be extended for Spring
 * Data.
 *
 * @author Wick Dynex
 */
public interface PetRepository extends JpaRepository<Pet, Integer> {

	/**
	 * Retrieve {@link Pet}s from the data store by name, returning all pets whose name
	 * <i>starts</i> with the given name.
	 * @param name Value to search for
	 * @param pageable pageable information
	 * @return a Collection of matching {@link Pet}s (or an empty Collection if none
	 * found)
	 */
	Page<Pet> findByNameStartingWith(String name, Pageable pageable);

}
