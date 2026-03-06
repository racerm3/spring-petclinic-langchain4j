package org.springframework.samples.petclinic.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Helper service to ingest clinic data into the RAG embedding store.
 */
@Service
public class RagEmbeddingService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RagEmbeddingService.class);

	private final InMemoryEmbeddingStore<TextSegment> embeddingStore;

	private final EmbeddingModel embeddingModel;

	private final ObjectMapper objectMapper;

	public RagEmbeddingService(InMemoryEmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel,
			ObjectMapper objectMapper) {
		this.embeddingStore = embeddingStore;
		this.embeddingModel = embeddingModel;
		this.objectMapper = objectMapper;
	}

	public void ingestVets(List<Vet> vets) {
		ingestList(vets, "vets");
	}

	public void ingestOwners(List<Owner> owners) {
		ingestList(owners, "owners");
	}

	public void ingestPets(List<Pet> pets) {
		ingestList(pets, "pets");
	}

	private void ingestList(List<?> entities, String entityType) {
		if (entities == null || entities.isEmpty()) {
			return;
		}

		String json = convertListToJson(entities, entityType);
		if (json == null || json.isEmpty()) {
			return;
		}

		EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
			.documentSplitter(new DocumentByLineSplitter(1000, 200))
			.embeddingModel(embeddingModel)
			.embeddingStore(embeddingStore)
			.build();

		ingestor.ingest(Document.from(json));
	}

	private String convertListToJson(List<?> entities, String entityType) {
		try {
			StringBuilder jsonArray = new StringBuilder();
			for (Object entity : entities) {
				String jsonElement = objectMapper.writeValueAsString(entity);
				jsonArray.append(jsonElement).append("\n");
			}
			return jsonArray.toString();
		}
		catch (JsonProcessingException e) {
			LOGGER.error("Problems encountered when generating JSON from the {} list", entityType, e);
			return null;
		}
	}

}
