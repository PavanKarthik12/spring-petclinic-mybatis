/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.rest.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.samples.petclinic.model.Visit;

/** @author Vitaliy Fedoriv */
public class JacksonCustomVisitDeserializer extends StdDeserializer<Visit> {

  public JacksonCustomVisitDeserializer() {
    this(null);
  }

  public JacksonCustomVisitDeserializer(Class<Visit> t) {
    super(t);
  }

  @Override
  public Visit deserialize(JsonParser parser, DeserializationContext context)
      throws IOException, JsonProcessingException {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
    Visit visit = new Visit();
    Date visitDate = null;
    JsonNode node = parser.getCodec().readTree(parser);
    int visitId = node.get("id").asInt();
    int petId = node.get("petId").asInt();
    String visitDateStr = node.get("date").asText(null);
    String description = node.get("description").asText(null);
    try {
      visitDate = formatter.parse(visitDateStr);
    } catch (ParseException e) {
      e.printStackTrace();
      throw new IOException(e);
    }

    if (visitId != 0) {
      visit.setId(visitId);
    }
    visit.setDate(visitDate);
    visit.setDescription(description);
    visit.setPetId(petId);
    return visit;
  }
}
