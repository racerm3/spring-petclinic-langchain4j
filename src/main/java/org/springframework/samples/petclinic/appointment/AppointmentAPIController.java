package org.springframework.samples.petclinic.appointment;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
class AppointmentAPIController {

	private final OwnerRepository owners;

	private final PetRepository pets;

	private final VetRepository vets;

	public AppointmentAPIController(OwnerRepository owners, PetRepository pets, VetRepository vets) {
		this.owners = owners;
		this.pets = pets;
		this.vets = vets;
	}

	@GetMapping("/owners/{ownerId}/pets")
	public List<SearchResult> getOwnerPets(@PathVariable("ownerId") Integer ownerId) {
		return pets.findByOwner_Id(ownerId)
			.stream()
			.map(p -> new SearchResult(p.getId(), p.getName()))
			.collect(Collectors.toList());
	}

	@GetMapping("/owners")
	public List<SearchResult> searchOwners(@RequestParam("query") String query) {
		return owners.findByFirstNameOrLastName(query, PageRequest.of(0, 10))
			.getContent()
			.stream()
			.map(o -> new SearchResult(o.getId(), o.getFirstName() + " " + o.getLastName()))
			.collect(Collectors.toList());
	}

	@GetMapping("/pets")
	public List<SearchResult> searchPets(@RequestParam("query") String query,
			@RequestParam(value = "ownerId", required = false) Integer ownerId) {
		if (ownerId != null) {
			return pets.findByNameContainingIgnoreCaseAndOwner_Id(query, ownerId, PageRequest.of(0, 10))
				.getContent()
				.stream()
				.map(p -> new SearchResult(p.getId(), p.getName()))
				.collect(Collectors.toList());
		}
		return pets.findByNameContainingIgnoreCase(query, PageRequest.of(0, 10))
			.getContent()
			.stream()
			.map(p -> new SearchResult(p.getId(),
					p.getName() + " (" + p.getOwner().getFirstName() + " " + p.getOwner().getLastName() + ")"))
			.collect(Collectors.toList());
	}

	@GetMapping("/vets")
	public List<SearchResult> searchVets(@RequestParam("query") String query) {
		return vets.findByFirstNameOrLastName(query, PageRequest.of(0, 10))
			.getContent()
			.stream()
			.map(v -> new SearchResult(v.getId(), v.getFirstName() + " " + v.getLastName()))
			.collect(Collectors.toList());
	}

	static class SearchResult {

		private final Integer id;

		private final String text;

		public SearchResult(Integer id, String text) {
			this.id = id;
			this.text = text;
		}

		public Integer getId() {
			return id;
		}

		public String getText() {
			return text;
		}

	}

}
