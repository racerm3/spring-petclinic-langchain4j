package org.springframework.samples.petclinic.chat;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Functions that are invoked by the LLM will use this bean to query the system of record
 * for information such as listing owners and vets, or adding pets to an owner.
 *
 * @author Oded Shopen
 * @author Antoine Rey
 */
@Component
public class AssistantTool {

	private final OwnerRepository ownerRepository;

	private final PetRepository petRepository;

	public AssistantTool(OwnerRepository ownerRepository, PetRepository petRepository) {
		this.ownerRepository = ownerRepository;
		this.petRepository = petRepository;
	}

	/**
	 * This tool is available to {@link Assistant}
	 */
	@Tool("current date, today")
	String currentDate() {
		return LocalDate.now().toString();
	}

	@Tool("List the owners that the pet clinic has: ownerId, name, address, phone number, pets")
	@NonNull
	public List<Owner> getAllOwners() {
		Pageable pageable = PageRequest.of(0, 100);
		Page<Owner> ownerPage = ownerRepository.findAll(pageable);
		return ownerPage.getContent();
	}

	@Tool("List all pets registered in the pet clinic, regardless of owner")
	public List<String> getAllPets() {
		return petRepository.findAll()
			.stream()
			.map(pet -> pet.getName() + " (" + (pet.getType() != null ? pet.getType().getName() : "unknown type") + ")"
					+ " – owner: "
					+ (pet.getOwner() != null ? pet.getOwner().getFirstName() + " " + pet.getOwner().getLastName()
							: "unknown"))
			.toList();
	}

	@Tool("Add a pet with the specified petTypeId and birthDate (yyyy-MM-dd), to an owner identified by the ownerId")
	@NonNull
	public Owner addPetToOwner(@P("the type id of the pet") @NonNull Integer petTypeId,
			@P("the name of the pet") @NonNull String petName,
			@P("the birth date of the pet in yyyy-MM-dd format") @NonNull String birthDate,
			@P("the id of the owner") @NonNull Integer ownerId) {
		Owner owner = ownerRepository.findById(ownerId).orElseThrow();
		PetType petType = ownerRepository.findPetTypes()
			.stream()
			.filter(pt -> pt.getId().equals(petTypeId))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Unknown petTypeId: " + petTypeId));
		Pet pet = new Pet();
		pet.setName(petName);
		pet.setType(petType);
		pet.setBirthDate(LocalDate.parse(birthDate));
		owner.addPet(pet);
		this.ownerRepository.save(owner);
		return owner;
	}

	@Tool("List all pairs of petTypeId and pet type name")
	@NonNull
	public List<PetType> populatePetTypes() {
		return this.ownerRepository.findPetTypes();
	}

	@Tool("""
			Add a new pet owner to the pet clinic. \
			The Owner must include a first name and a last name as two separate words, \
			plus an address, a city, and a 10-digit phone number""")
	@NonNull
	public Owner addOwnerToPetclinic(@P("first name of the owner") @NonNull String firstName,
			@P("last name of the owner") @NonNull String lastName,
			@P("street address of the owner") @NonNull String address, @P("city of the owner") @NonNull String city,
			@P("10-digit telephone number of the owner") @NonNull String telephone) {
		Owner owner = new Owner();
		owner.setFirstName(firstName);
		owner.setLastName(lastName);
		owner.setAddress(address);
		owner.setCity(city);
		owner.setTelephone(telephone);
		return ownerRepository.save(owner);
	}

}
