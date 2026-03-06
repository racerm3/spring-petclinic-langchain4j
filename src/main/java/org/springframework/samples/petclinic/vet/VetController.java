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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import java.util.Collection;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
class VetController {

	private final VetRepository vetRepository;

	private final SpecialtyRepository specialtyRepository;

	public VetController(VetRepository vetRepository, SpecialtyRepository specialtyRepository) {
		this.vetRepository = vetRepository;
		this.specialtyRepository = specialtyRepository;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("vet")
	public Vet findVet(@PathVariable(name = "vetId", required = false) Integer vetId) {
		return vetId == null ? new Vet() : this.vetRepository.findById(vetId)
			.orElseThrow(() -> new IllegalArgumentException("Vet not found with id: " + vetId));
	}

	@ModelAttribute("specialties")
	public Collection<Specialty> populateSpecialties() {
		return this.specialtyRepository.findAll();
	}

	@GetMapping("/vets.html")
	public String showVetList(@RequestParam(defaultValue = "1") int page,
			@RequestParam(required = false) String lastName, @RequestParam(required = false) Integer specialtyId,
			Model model) {
		boolean searchExecuted = (lastName != null || specialtyId != null);
		model.addAttribute("lastName", lastName);
		model.addAttribute("specialtyId", specialtyId);
		model.addAttribute("searchExecuted", searchExecuted);

		if (!searchExecuted) {
			return "vets/vetList";
		}

		// Here we are returning an object of type 'Vets' rather than a collection of
		// Vet
		// objects so it is simpler for Object-Xml mapping
		Vets vets = new Vets();
		Page<Vet> paginated = findPaginated(page, lastName, specialtyId);
		vets.getVetList().addAll(paginated.toList());
		return addPaginationModel(page, paginated, model);
	}

	@GetMapping("/vets/new")
	public String initCreationForm() {
		return "vets/createOrUpdateVetForm";
	}

	@PostMapping("/vets/new")
	public String processCreationForm(@Valid @NonNull Vet vet, BindingResult result,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "vets/createOrUpdateVetForm";
		}
		this.vetRepository.save(vet);
		redirectAttributes.addFlashAttribute("message", "New Veterinarian Created");
		return "redirect:/vets.html";
	}

	@GetMapping("/vets/{vetId}/edit")
	public String initUpdateForm() {
		return "vets/createOrUpdateVetForm";
	}

	@PostMapping("/vets/{vetId}/edit")
	public String processUpdateForm(@Valid @NonNull Vet vet, BindingResult result, @PathVariable("vetId") int vetId,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "vets/createOrUpdateVetForm";
		}
		vet.setId(vetId);
		this.vetRepository.save(vet);
		redirectAttributes.addFlashAttribute("message", "Veterinarian Updated");
		return "redirect:/vets.html";
	}

	private String addPaginationModel(int page, Page<Vet> paginated, Model model) {
		List<Vet> listVets = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listVets", listVets);
		return "vets/vetList";
	}

	private Page<Vet> findPaginated(int page, String lastName, Integer specialtyId) {
		int pageSize = 20;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		if ((lastName != null && !lastName.isEmpty()) || specialtyId != null) {
			return vetRepository.findByLastNameAndSpecialty(lastName, specialtyId, pageable);
		}
		return vetRepository.findAll(pageable);
	}

	@GetMapping({ "/vets" })
	public @ResponseBody Vets showResourcesVetList() {
		// Here we are returning an object of type 'Vets' rather than a collection of
		// Vet
		// objects so it is simpler for JSon/Object mapping
		Vets vets = new Vets();
		vets.getVetList().addAll(this.vetRepository.findAll());
		return vets;
	}

}
