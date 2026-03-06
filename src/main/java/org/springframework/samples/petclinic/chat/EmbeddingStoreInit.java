package org.springframework.samples.petclinic.chat;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Component;

/**
 * Loads clinic data into an Embedding Store for the purpose of RAG functionality.
 *
 * @author Oded Shopen
 * @author Antoine Rey
 */
@Component
public class EmbeddingStoreInit {

	private final RagEmbeddingService ragEmbeddingService;

	private final VetRepository vetRepository;

	private final OwnerRepository ownerRepository;

	private final PetRepository petRepository;

	public EmbeddingStoreInit(RagEmbeddingService ragEmbeddingService, VetRepository vetRepository,
			OwnerRepository ownerRepository, PetRepository petRepository) {
		this.ragEmbeddingService = ragEmbeddingService;
		this.vetRepository = vetRepository;
		this.ownerRepository = ownerRepository;
		this.petRepository = petRepository;
	}

	@EventListener
	public void loadVetDataToEmbeddingStoreOnStartup(ApplicationStartedEvent event) {
		Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
		Page<Vet> vetsPage = vetRepository.findAll(pageable);

		Page<Owner> ownersPage = ownerRepository.findAll(pageable);
		Page<Pet> petsPage = petRepository.findAll(pageable);

		ragEmbeddingService.ingestVets(vetsPage.getContent());
		ragEmbeddingService.ingestOwners(ownersPage.getContent());
		ragEmbeddingService.ingestPets(petsPage.getContent());
	}

}
