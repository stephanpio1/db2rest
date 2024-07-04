package com.homihq.db2rest.rest.oracle;

import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import com.adelean.inject.resources.junit.jupiter.WithJacksonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.homihq.db2rest.OracleBaseIntegrationTest;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.homihq.db2rest.jdbc.rest.RdbmsRestApi.VERSION;
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@Order(211)
@TestWithResources
@Disabled
class OracleCrossJoinControllerTest extends OracleBaseIntegrationTest {

    @WithJacksonMapper
    ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @GivenJsonResource("/testdata/CROSS_JOIN_USERS_ORACLE.json")
    List<Map<String,Object>> CROSS_JOIN;

    @GivenJsonResource("/testdata/CROSS_JOIN_TOPS_ORACLE.json")
    List<Map<String,Object>> CROSS_JOIN_TOPS;


    @Test
    @DisplayName("Test cross Join - Users")
    void testCrossJoin() throws Exception {


        mockMvc.perform(post(VERSION + "/oradb/USERS/_expand")
                        .contentType(APPLICATION_JSON).accept(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CROSS_JOIN))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*").isArray())
                .andExpect(jsonPath("$.*", hasSize(16)))
                .andExpect(jsonPath("$[0].*", hasSize(10)))
                .andExpect(jsonPath("$[0].AUID", equalTo(1)))
                .andExpect(jsonPath("$[0].APID", equalTo(1)))
                .andExpect(jsonPath("$[0].FIRSTNAME", equalTo("Jack")))

                .andExpect(jsonPath("$[1].AUID", equalTo(2)))
                .andExpect(jsonPath("$[1].APID", equalTo(1)))
                .andExpect(jsonPath("$[1].FIRSTNAME", equalTo("Jack")))

                .andDo(document("oracle-cross-join-users"));


    }

    @Test
    @DisplayName("Test cross Join - Tops")
    void testCrossJoinTops() throws Exception {


        mockMvc.perform(post(VERSION + "/oradb/TOPS/_expand")
                        .contentType(APPLICATION_JSON).accept(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CROSS_JOIN_TOPS))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*").isArray())
                .andExpect(jsonPath("$.*", hasSize(9)))
                .andExpect(jsonPath("$[0].*", hasSize(6)))

                .andExpect(jsonPath("$[0].TOP_ITEM", equalTo("sweater")))
                .andExpect(jsonPath("$[0].BOTTOM_ITEM", equalTo("jeans")))
                .andExpect(jsonPath("$[0].COLOR", equalTo("red")))
                .andExpect(jsonPath("$[0].botColor", equalTo("blue")))


                .andDo(document("oracle-cross-join-tops"));


    }

}
