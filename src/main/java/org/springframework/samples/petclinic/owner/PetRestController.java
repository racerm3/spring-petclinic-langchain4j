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

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for <code>Pet</code> domain objects.
 *
 * @author Wick Dynex
 */
@RestController
@RequestMapping("/api/pets")
class PetRestController {

	private final PetRepository pets;

	private final OwnerRepository owners;

	public PetRestController(PetRepository pets, OwnerRepository owners) {
		this.pets = pets;
		this.owners = owners;
	}

	@GetMapping
	public List<Pet> getAllPets() {
		return this.pets.findAll();
	}

	@GetMapping("/{petId}")
	public Pet getPet(@PathVariable("petId") int petId) {
		return this.pets.findById(petId)
			.orElseThrow(() -> new IllegalArgumentException("Pet not found with id: " + petId));
	}

	@GetMapping("/types")
	public List<PetType> getPetTypes() {
		return this.owners.findPetTypes();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Pet createPet(@Valid @RequestBody Pet pet) {
		return this.pets.save(pet);
	}

	@PutMapping("/{petId}")
	public Pet updatePet(@PathVariable("petId") int petId, @Valid @RequestBody Pet pet) {
		return this.pets.findById(petId).map(existingPet -> {
			pet.setId(petId);
			return this.pets.save(pet);
		}).orElseThrow(() -> new IllegalArgumentException("Pet not found with id: " + petId));
	}

	@DeleteMapping("/{petId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletePet(@PathVariable("petId") int petId) {
		this.pets.deleteById(petId);
	}

}
