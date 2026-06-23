package com.nuxatech.cmcc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuxatech.cmcc.dto.CreateServiceRequest;
import com.nuxatech.cmcc.entity.ServiceStatus;
import com.nuxatech.cmcc.exception.GlobalExceptionHandler;
import com.nuxatech.cmcc.exception.ServiceNotFoundException;
import com.nuxatech.cmcc.service.HealthCheckService;
import com.nuxatech.cmcc.service.ServiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ServiceControllerTest {

    @Mock
    private ServiceService serviceService;
    @Mock
    private HealthCheckService healthCheckService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ServiceController controller = new ServiceController(serviceService, healthCheckService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void getAllServices_ReturnsList() throws Exception {
        var serviceId = UUID.randomUUID();
        var now = Instant.now();

        var response = new com.nuxatech.cmcc.dto.ServiceResponse() {
            public UUID getId() { return serviceId; }
            public String getName() { return "Test Service"; }
            public String getUrl() { return "https://example.com/health"; }
            public String getCategory() { return "Internal"; }
            public ServiceStatus getStatus() { return ServiceStatus.UP; }
            public Instant getLastCheckedAt() { return now; }
            public Long getLatencyMs() { return 100L; }
            public Instant getCreatedAt() { return now; }
        };

        when(serviceService.getAllServices()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/services"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is("Test Service")));
    }

    @Test
    void getService_WhenExists_ReturnsService() throws Exception {
        UUID id = UUID.randomUUID();
        var response = new com.nuxatech.cmcc.dto.ServiceResponse() {
            public UUID getId() { return id; }
            public String getName() { return "Test"; }
            public String getUrl() { return "https://example.com"; }
            public String getCategory() { return "Internal"; }
            public ServiceStatus getStatus() { return ServiceStatus.UP; }
            public Instant getLastCheckedAt() { return null; }
            public Long getLatencyMs() { return null; }
            public Instant getCreatedAt() { return Instant.now(); }
        };

        when(serviceService.getServiceById(id)).thenReturn(response);

        mockMvc.perform(get("/api/services/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Test")));
    }

    @Test
    void getService_WhenNotExists_Returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(serviceService.getServiceById(id)).thenThrow(new ServiceNotFoundException(id));

        mockMvc.perform(get("/api/services/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status", is(404)))
            .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    void createService_WithValidData_Returns201() throws Exception {
        var request = new CreateServiceRequest();
        request.setName("New Service");
        request.setUrl("https://example.com/health");
        request.setCategory("External");

        UUID id = UUID.randomUUID();
        var response = new com.nuxatech.cmcc.dto.ServiceResponse() {
            public UUID getId() { return id; }
            public String getName() { return "New Service"; }
            public String getUrl() { return "https://example.com/health"; }
            public String getCategory() { return "External"; }
            public ServiceStatus getStatus() { return ServiceStatus.UNKNOWN; }
            public Instant getLastCheckedAt() { return null; }
            public Long getLatencyMs() { return null; }
            public Instant getCreatedAt() { return Instant.now(); }
        };

        when(serviceService.createService(any())).thenReturn(response);

        mockMvc.perform(post("/api/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name", is("New Service")))
            .andExpect(jsonPath("$.status", is("UNKNOWN")));
    }

    @Test
    void createService_WithBlankName_Returns400() throws Exception {
        var request = new CreateServiceRequest();
        request.setName("");
        request.setUrl("https://example.com/health");
        request.setCategory("External");

        mockMvc.perform(post("/api/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void createService_WithInvalidUrl_Returns400() throws Exception {
        var request = new CreateServiceRequest();
        request.setName("Test");
        request.setUrl("invalid-url");
        request.setCategory("External");

        mockMvc.perform(post("/api/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void deleteService_WhenNotExists_Returns404() throws Exception {
        UUID id = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new ServiceNotFoundException(id)).when(serviceService).deleteService(id);

        mockMvc.perform(delete("/api/services/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void deleteService_WhenExists_Returns204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/services/{id}", id))
            .andExpect(status().isNoContent());
    }

    @Test
    void forceCheck_WhenNotExists_Returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(serviceService.getServiceEntity(id)).thenThrow(new ServiceNotFoundException(id));

        mockMvc.perform(post("/api/services/{id}/check", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status", is(404)));
    }
}
