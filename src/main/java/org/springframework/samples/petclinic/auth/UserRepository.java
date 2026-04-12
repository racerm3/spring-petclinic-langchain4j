package org.springframework.samples.petclinic.auth;

import org.springframework.data.repository.Repository;

public interface UserRepository extends Repository<User, Integer> {

	User findByUsername(String username);

	void save(User user);

}
