package org.springframework.samples.petclinic.vet;

import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository class for <code>Specialty</code> domain objects.
 */
public interface SpecialtyRepository extends JpaRepository<Specialty, Integer> {

	@Transactional(readOnly = true)
	@Cacheable("specialties")
	List<Specialty> findAll();

}
