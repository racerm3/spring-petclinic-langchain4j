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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Wick Dynex
 */
@Controller
class PetSearchController {

	private final PetRepository pets;

	public PetSearchController(PetRepository pets) {
		this.pets = pets;
	}

	@GetMapping("/pets/find")
	public String initFindForm(Model model) {
		model.addAttribute("pet", new Pet());
		return "pets/findPets";
	}

	@GetMapping("/pets")
	public String processFindForm(@RequestParam(defaultValue = "1") int page, Pet pet, BindingResult result,
			Model model) {
		// allow parameterless GET request for /pets to return all records
		// find pets by name
		Page<Pet> petsResults = findPaginatedForPetsName(page, pet.getName());
		if (petsResults.isEmpty()) {
			// no pets found
			result.rejectValue("name", "notFound", "not found");
			return "pets/findPets";
		}

		if (petsResults.getTotalElements() == 1) {
			// 1 pet found
			pet = petsResults.iterator().next();
			return "redirect:/owners/" + pet.getOwner().getId();
		}

		// multiple pets found
		model.addAttribute("pet", pet);
		return addPaginationModel(page, model, petsResults);
	}

	private String addPaginationModel(int page, Model model, Page<Pet> paginated) {
		List<Pet> listPets = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listPets", listPets);
		return "pets/petsList";
	}

	private Page<Pet> findPaginatedForPetsName(int page, String name) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return pets.findByNameStartingWith(name, pageable);
	}

}
