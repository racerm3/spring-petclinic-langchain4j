package org.springframework.samples.petclinic.chat;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.router.QueryRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * This router determines whether retrieval from the embedding store is needed. It checks
 * if the query is related to any of the clinic's data (vets, owners, pets, visits, or
 * appointments).
 */
class ClinicQueryRouter implements QueryRouter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClinicQueryRouter.class);

	private static final PromptTemplate PROMPT_TEMPLATE = PromptTemplate.from("""
			Is the following query related to veterinarians, owners, pets, visits, or appointments of the pet clinic?
			Answer only 'yes' or 'no'.
			Query: {{it}}
			""");

	private final ContentRetriever contentRetriever;

	private final ChatModel chatModel;

	public ClinicQueryRouter(ChatModel chatModel, ContentRetriever contentRetriever) {
		this.chatModel = chatModel;
		this.contentRetriever = contentRetriever;
	}

	@Override
	public Collection<ContentRetriever> route(Query query) {
		Prompt prompt = PROMPT_TEMPLATE.apply(query.text());

		AiMessage aiMessage = chatModel.chat(prompt.toUserMessage()).aiMessage();
		LOGGER.debug("LLM decided: {}", aiMessage.text());

		if (aiMessage.text().toLowerCase().contains("yes")) {
			return singletonList(contentRetriever);
		}
		return emptyList();
	}

}
