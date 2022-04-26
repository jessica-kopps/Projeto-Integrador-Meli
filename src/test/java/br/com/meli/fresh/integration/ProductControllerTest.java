package br.com.meli.fresh.integration;

import br.com.meli.fresh.dto.request.ProductRequest;
import br.com.meli.fresh.dto.response.productResponse.ProductResponse;
import br.com.meli.fresh.model.Product;
import br.com.meli.fresh.services.impl.ProductServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;
import org.json.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductServiceImpl service;

    private final String BASE_URL = "http://localhost:8080/api/v1/fresh-products/products/";

    @Test
    public void testCreateProduct() throws Exception {
        ProductRequest preq = new ProductRequest();
        preq.setName("Pizza");
        ObjectWriter writer = new ObjectMapper()
                .configure(SerializationFeature.WRAP_ROOT_VALUE, false)
                .writer().withDefaultPrettyPrinter();

        String payloadRequest = writer.writeValueAsString(preq);

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadRequest))
                        .andDo(print()).andExpect(status().isCreated()).andReturn();

        String locationUrl = mvcResult.getResponse().getHeader("Location");

        assertNotNull(locationUrl);
    }

    @Test
    public void testGetProduct() throws Exception {
        Product p = new Product();
        p.setName("Ice cream");
        Product pSaved = service.create(p);

        MvcResult mvcResultGet = this.mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + pSaved.getId())).andReturn();

        String jsonObjectReturned = mvcResultGet.getResponse().getContentAsString();
        ProductResponse pres = new ObjectMapper().readValue(jsonObjectReturned, ProductResponse.class);

        assertEquals("Ice cream", pres.getName());
    }

    @Test
    public void testGetAllProduct() throws Exception {
        Product p1 = new Product();
        Product p2 = new Product();
        p1.setName("Cheese");
        p2.setName("Ham");
        service.create(p1);
        service.create(p2);

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL)).andReturn();
        String jsonObjectReturned = mvcResult.getResponse().getContentAsString();
        JSONObject obj = new JSONObject(jsonObjectReturned);
        Integer totalElements = obj.getInt("totalElements");
        System.out.println(totalElements);
        assertEquals(2, totalElements);
    }

    @Test
    public void testUpdateProduct() throws Exception {
        Product p = new Product();
        p.setName("Lasagna");
        Product pSaved = service.create(p);

        Product pToUpdate = new Product();
        pToUpdate.setName("Popsicle");


        System.out.println("Checking what I saved in DB.");
        MvcResult mvcResultGetWithoutUpdated = this.mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + pSaved.getId())).andReturn();
        String objectReturned = mvcResultGetWithoutUpdated.getResponse().getContentAsString();
        ProductResponse pWithoutUpdated = new ObjectMapper().readValue(objectReturned, ProductResponse.class);

        ObjectWriter writer = new ObjectMapper()
                .configure(SerializationFeature.WRAP_ROOT_VALUE, false)
                .writer().withDefaultPrettyPrinter();

        String jsonObjectToBeUpdated = writer.writeValueAsString(pToUpdate);

        System.out.println("Updating my object");
        MvcResult mvcUpdated = this.mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + pSaved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObjectToBeUpdated))
                .andDo(print()).andExpect(status().isOk()).andReturn();

        MvcResult MvcResultGetUpdated = this.mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + pSaved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObjectToBeUpdated))
                .andDo(print()).andExpect(status().isOk()).andReturn();

        String objectUpdatedReturned = MvcResultGetUpdated.getResponse().getContentAsString();
        ProductResponse pUpdated = new ObjectMapper().readValue(objectUpdatedReturned, ProductResponse.class);

        assertEquals(pWithoutUpdated.getName(), "Lasagna");
        assertEquals(pUpdated.getName(), "Popsicle");
    }

    @Test
    public void testDeleteProduct() throws Exception {
        Product p = new Product();
        p.setName("Cheddar");
        Product pSaved = service.create(p);

        this.mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + pSaved.getId()))
                .andExpect(status().isOk()).andReturn();

        MvcResult mvcResultDelete = this.mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + pSaved.getId()))
                .andExpect(status().isNotFound()).andReturn();

        String errorMessage = mvcResultDelete.getResponse().getContentAsString();

        assertEquals("Product was not found for the given ID! ID:" + pSaved.getId(), errorMessage);
    }

    @Test
    public void testSaveAlreadyExistsProduct() throws Exception {
        Product p = new Product();
        p.setName("Maionese");
        service.create(p);

        ProductRequest preq = new ProductRequest();
        preq.setName(p.getName());

        ObjectWriter writer = new ObjectMapper()
                .configure(SerializationFeature.WRAP_ROOT_VALUE, false)
                .writer().withDefaultPrettyPrinter();

        String payloadRequest = writer.writeValueAsString(preq);

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadRequest))
                .andDo(print()).andExpect(status().isConflict()).andReturn();

        String errorMessage = mvcResult.getResponse().getContentAsString();

        assertEquals("This product already exists in our database", errorMessage);

    }




}
