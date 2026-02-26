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

import java.util.List;
import org.springframework.lang.NonNull;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for <code>Owner</code> domain objects.
 *
 * @author Wick Dynex
 */
@RestController
@RequestMapping("/api/owners")
@SuppressWarnings("null")
class OwnerRestController {

	private final OwnerRepository owners;

	public OwnerRestController(OwnerRepository owners) {
		this.owners = owners;
	}

	@GetMapping
	public List<Owner> getAllOwners() {
		return this.owners.findAll();
	}

	@GetMapping("/{ownerId}")
	public Owner getOwner(@PathVariable("ownerId") int ownerId) {
		return this.owners.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@NonNull
	public Owner createOwner(@Valid @RequestBody @NonNull Owner owner) {
		return this.owners.save(owner);
	}

	@PutMapping("/{ownerId}")
	@NonNull
	public Owner updateOwner(@PathVariable("ownerId") int ownerId, @Valid @RequestBody @NonNull Owner owner) {
		return this.owners.findById(ownerId).map(existingOwner -> {
			owner.setId(ownerId);
			return this.owners.save(owner);
		}).orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
	}

	@DeleteMapping("/{ownerId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteOwner(@PathVariable("ownerId") int ownerId) {
		this.owners.deleteById(ownerId);
	}

}
