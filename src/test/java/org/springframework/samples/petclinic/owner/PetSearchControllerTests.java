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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for the {@link PetSearchController}
 */
@WebMvcTest(value = PetSearchController.class,
		excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
				value = org.springframework.samples.petclinic.system.WebMvcConfig.class,
				type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE))
@SuppressWarnings("null")
class PetSearchControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PetRepository pets;

	@Test
	void testInitFindForm() throws Exception {
		mockMvc.perform(get("/pets/find"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/findPets"))
			.andExpect(model().attributeExists("pet"));
	}

	@Test
	void testProcessFindFormSuccess() throws Exception {
		Pet pet = new Pet();
		pet.setId(1);
		Owner owner = new Owner();
		owner.setId(1);
		pet.setOwner(owner);

		Page<Pet> petsPage = new PageImpl<>(Collections.singletonList(pet));
		given(this.pets.findByNameStartingWithIgnoreCase(anyString(), any(PageRequest.class))).willReturn(petsPage);

		mockMvc.perform(get("/pets").param("name", "Leo"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/1"));
	}

	@Test
	void testProcessFindFormMultipleResults() throws Exception {
		PetType dog = new PetType();
		dog.setName("dog");

		Pet pet1 = new Pet();
		pet1.setId(1);
		pet1.setType(dog);
		Owner owner1 = new Owner();
		owner1.setId(1);
		pet1.setOwner(owner1);

		Pet pet2 = new Pet();
		pet2.setId(2);
		pet2.setType(dog);
		Owner owner2 = new Owner();
		owner2.setId(2);
		pet2.setOwner(owner2);

		Page<Pet> petsPage = new PageImpl<>(Lists.newArrayList(pet1, pet2), PageRequest.of(0, 5), 2);
		given(this.pets.findByNameStartingWithIgnoreCase(anyString(), any(PageRequest.class))).willReturn(petsPage);

		mockMvc.perform(get("/pets"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/petsList"))
			.andExpect(model().attributeExists("listPets"));
	}

	@Test
	void testProcessFindFormNoResults() throws Exception {
		given(this.pets.findByNameStartingWithIgnoreCase(anyString(), any(PageRequest.class)))
			.willReturn(new PageImpl<>(Collections.emptyList()));

		mockMvc.perform(get("/pets").param("name", "UnknownPet"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/findPets"))
			.andExpect(model().attributeHasFieldErrors("pet", "name"));
	}

}
