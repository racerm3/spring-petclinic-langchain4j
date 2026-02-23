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

import java.util.Optional;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the {@link VetController}
 */

@WebMvcTest(VetController.class)
@Import(SpecialtyFormatter.class)
@DisabledInNativeImage
@DisabledInAotMode
class VetControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private VetRepository vets;

	@MockitoBean
	private SpecialtyRepository specialtyRepository;

	private Vet james() {
		Vet james = new Vet();
		james.setFirstName("James");
		james.setLastName("Carter");
		james.setId(1);
		return james;
	}

	private Vet helen() {
		Vet helen = new Vet();
		helen.setFirstName("Helen");
		helen.setLastName("Leary");
		helen.setId(2);
		Specialty radiology = new Specialty();
		radiology.setId(1);
		radiology.setName("radiology");
		helen.addSpecialty(radiology);
		return helen;
	}

	@BeforeEach
	void setup() {
		given(this.vets.findAll()).willReturn(Lists.newArrayList(james(), helen()));
		given(this.vets.findAll(any(Pageable.class)))
			.willReturn(new PageImpl<Vet>(Lists.newArrayList(james(), helen())));
		given(this.vets.findById(1)).willReturn(Optional.of(james()));
		given(this.specialtyRepository.findAll()).willReturn(Lists.newArrayList(radiology()));
	}

	private Specialty radiology() {
		Specialty radiology = new Specialty();
		radiology.setId(1);
		radiology.setName("radiology");
		return radiology;
	}

	@Test
	void testShowVetListHtml() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/vets.html?page=1"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("listVets"))
			.andExpect(model().attributeExists("specialties"))
			.andExpect(view().name("vets/vetList"));

	}

	@Test
	void testShowVetListSearch() throws Exception {
		given(this.vets.findByLastNameAndSpecialty(any(), any(), any(Pageable.class)))
			.willReturn(new PageImpl<Vet>(Lists.newArrayList(helen())));

		mockMvc.perform(MockMvcRequestBuilders.get("/vets.html?page=1&lastName=Leary&specialtyId=1"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("lastName", "Leary"))
			.andExpect(model().attribute("specialtyId", 1))
			.andExpect(model().attributeExists("listVets"))
			.andExpect(view().name("vets/vetList"));
	}

	@Test
	void testShowResourcesVetList() throws Exception {
		ResultActions actions = mockMvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
		actions.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.vetList[0].id").value(1));
	}

	@Test
	void testShowVetListSearchCaseInsensitive() throws Exception {
		given(this.vets.findByLastNameAndSpecialty(any(), any(), any(Pageable.class)))
			.willReturn(new PageImpl<Vet>(Lists.newArrayList(helen())));

		// Search with lowercase "leary" should match "Leary" due to case-insensitivity
		mockMvc.perform(MockMvcRequestBuilders.get("/vets.html?page=1&lastName=leary&specialtyId=1"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("lastName", "leary"))
			.andExpect(model().attributeExists("listVets"))
			.andExpect(view().name("vets/vetList"));
	}

	@Test
	void testInitCreationForm() throws Exception {
		mockMvc.perform(get("/vets/new"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("vet"))
			.andExpect(model().attributeExists("specialties"))
			.andExpect(view().name("vets/createOrUpdateVetForm"));
	}

	@Test
	void testProcessCreationFormSuccess() throws Exception {
		mockMvc
			.perform(MockMvcRequestBuilders.post("/vets/new")
				.param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("specialties", "radiology"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/vets.html"));

		ArgumentCaptor<Vet> captor = ArgumentCaptor.forClass(Vet.class);
		verify(this.vets).save(captor.capture());
		assertThat(captor.getValue().getSpecialties()).hasSize(1);
		assertThat(captor.getValue().getSpecialties().get(0).getName()).isEqualTo("radiology");
	}

	@Test
	void testProcessCreationFormHasErrors() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/vets/new").param("firstName", "Joe").param("lastName", "")) // Last
																													// name
																													// is
																													// blank
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("vet"))
			.andExpect(model().attributeHasFieldErrors("vet", "lastName"))
			.andExpect(view().name("vets/createOrUpdateVetForm"));
	}

	@Test
	void testInitUpdateForm() throws Exception {
		given(this.vets.findById(1)).willReturn(Optional.of(james()));

		mockMvc.perform(get("/vets/1/edit"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("vet"))
			.andExpect(model().attributeExists("specialties"))
			.andExpect(view().name("vets/createOrUpdateVetForm"));
	}

	@Test
	void testProcessUpdateFormSuccess() throws Exception {
		mockMvc
			.perform(MockMvcRequestBuilders.post("/vets/1/edit")
				.param("firstName", "James")
				.param("lastName", "Carter")
				.param("specialties", "radiology"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/vets.html"));

		verify(this.vets).save(any(Vet.class));
	}

}
