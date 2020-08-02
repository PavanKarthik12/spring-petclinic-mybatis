/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.samples.petclinic.rest;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.rest.serializer.JacksonCustomPetDeserializer;
import org.springframework.samples.petclinic.rest.serializer.JacksonCustomPetSerializer;
import org.springframework.samples.petclinic.rest.support.MockMvcBase;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Test class for {@link PetRestController}
 *
 * @author Vitaliy Fedoriv
 */
public class PetRestControllerTests extends MockMvcBase {

  @MockBean protected ClinicService clinicService;

  private List<Pet> pets;

  @Before
  public void initPets() {
    SimpleModule s = new SimpleModule();
    s.addSerializer(Pet.class, new JacksonCustomPetSerializer());
    s.addDeserializer(Pet.class, new JacksonCustomPetDeserializer());
    this.objectMapper.registerModule(s);

    pets = new ArrayList<Pet>();

    Owner owner = new Owner();
    owner.setId(1);
    owner.setFirstName("Eduardo");
    owner.setLastName("Rodriquez");
    owner.setAddress("2693 Commerce St.");
    owner.setCity("McFarland");
    owner.setTelephone("6085558763");

    PetType petType = new PetType();
    petType.setId(2);
    petType.setName("dog");

    Pet pet = new Pet();
    pet.setId(3);
    pet.setName("Rosy");
    pet.setBirthDate(new Date());
    pet.setOwnerId(owner.getId());
    pet.setType(petType);
    pets.add(pet);

    pet = new Pet();
    pet.setId(4);
    pet.setName("Jewel");
    pet.setBirthDate(new Date());
    pet.setOwnerId(owner.getId());
    pet.setType(petType);
    pets.add(pet);
  }

  @Test
  @WithMockUser(roles = "OWNER_ADMIN")
  public void testGetPetSuccess() throws Exception {
    given(this.clinicService.findPetById(3)).willReturn(pets.get(0));
    this.mockMvc
        .perform(get("/api/pets/3").accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.id").value(3))
        .andExpect(jsonPath("$.name").value("Rosy"));
  }

  @Test
  @WithMockUser(roles = "OWNER_ADMIN")
  public void testGetPetNotFound() throws Exception {
    given(this.clinicService.findPetById(-1)).willReturn(null);
    this.mockMvc
        .perform(get("/api/pets/-1").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "OWNER_ADMIN")
  public void testGetAllPetsSuccess() throws Exception {
    given(this.clinicService.findAllPets()).willReturn(pets);
    this.mockMvc
        .perform(get("/api/pets/").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.[0].id").value(3))
        .andExpect(jsonPath("$.[0].name").value("Rosy"))
        .andExpect(jsonPath("$.[1].id").value(4))
        .andExpect(jsonPath("$.[1].name").value("Jewel"));
  }

  @Test
  @WithMockUser(roles = "OWNER_ADMIN")
  public void testGetAllPetsNotFound() throws Exception {
    pets.clear();
    given(this.clinicService.findAllPets()).willReturn(pets);
    this.mockMvc
        .perform(get("/api/pets/").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "OWNER_ADMIN")
  public void testCreatePetSuccess() throws Exception {
    Pet newPet = pets.get(0);
    newPet.setId(999);
    ObjectMapper mapper = new ObjectMapper();
    String newPetAsJSON = mapper.writeValueAsString(newPet);
    this.mockMvc
        .perform(
            post("/api/pets/")
                .content(newPetAsJSON)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser(roles = "OWNER_ADMIN")
  public void testCreatePetError() throws Exception {
    Pet newPet = pets.get(0);
    newPet.setId(null);
    newPet.setName(null);
    ObjectMapper mapper = new ObjectMapper();
    String newPetAsJSON = mapper.writeValueAsString(newPet);
    this.mockMvc
        .perform(
            post("/api/pets/")
                .content(newPetAsJSON)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "OWNER_ADMIN")
  public void testUpdatePetSuccess() throws Exception {
    given(this.clinicService.findPetById(3)).willReturn(pets.get(0));
    Pet newPet = pets.get(0);
    newPet.setName("Rosy I");
    ObjectMapper mapper = new ObjectMapper();
    String newPetAsJSON = mapper.writeValueAsString(newPet);
    this.mockMvc
        .perform(
            put("/api/pets/3")
                .content(newPetAsJSON)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(status().isNoContent());

    this.mockMvc
        .perform(
            get("/api/pets/3")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.id").value(3))
        .andExpect(jsonPath("$.name").value("Rosy I"));
  }

  @Test
  @WithMockUser(roles = "OWNER_ADMIN")
  public void testUpdatePetError() throws Exception {
    Pet newPet = pets.get(0);
    newPet.setName("");
    ObjectMapper mapper = new ObjectMapper();
    String newPetAsJSON = mapper.writeValueAsString(newPet);
    this.mockMvc
        .perform(
            put("/api/pets/3")
                .content(newPetAsJSON)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "OWNER_ADMIN")
  public void testDeletePetSuccess() throws Exception {
    Pet newPet = pets.get(0);
    ObjectMapper mapper = new ObjectMapper();
    String newPetAsJSON = mapper.writeValueAsString(newPet);
    given(this.clinicService.findPetById(3)).willReturn(pets.get(0));
    this.mockMvc
        .perform(
            delete("/api/pets/3")
                .content(newPetAsJSON)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(roles = "OWNER_ADMIN")
  public void testDeletePetError() throws Exception {
    Pet newPet = pets.get(0);
    ObjectMapper mapper = new ObjectMapper();
    String newPetAsJSON = mapper.writeValueAsString(newPet);
    given(this.clinicService.findPetById(-1)).willReturn(null);
    this.mockMvc
        .perform(
            delete("/api/pets/-1")
                .content(newPetAsJSON)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isNotFound());
  }
}
