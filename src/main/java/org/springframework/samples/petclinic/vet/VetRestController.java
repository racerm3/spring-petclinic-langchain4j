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
import org.springframework.lang.NonNull;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for <code>Vet</code> domain objects.
 *
 * @author Wick Dynex
 */
@RestController
@RequestMapping("/api/vets")
@SuppressWarnings("null")
class VetRestController {

	private final VetRepository vets;

	public VetRestController(VetRepository vets) {
		this.vets = vets;
	}

	@GetMapping
	public List<Vet> getAllVets() {
		return this.vets.findAll();
	}

	@GetMapping("/{vetId}")
	public Vet getVet(@PathVariable("vetId") int vetId) {
		return this.vets.findById(vetId)
			.orElseThrow(() -> new IllegalArgumentException("Vet not found with id: " + vetId));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@NonNull
	public Vet createVet(@Valid @RequestBody @NonNull Vet vet) {
		return this.vets.save(vet);
	}

	@PutMapping("/{vetId}")
	@NonNull
	public Vet updateVet(@PathVariable("vetId") int vetId, @Valid @RequestBody @NonNull Vet vet) {
		return this.vets.findById(vetId).map(existingVet -> {
			vet.setId(vetId);
			return this.vets.save(vet);
		}).orElseThrow(() -> new IllegalArgumentException("Vet not found with id: " + vetId));
	}

	@DeleteMapping("/{vetId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteVet(@PathVariable("vetId") int vetId) {
		this.vets.deleteById(vetId);
	}

}
