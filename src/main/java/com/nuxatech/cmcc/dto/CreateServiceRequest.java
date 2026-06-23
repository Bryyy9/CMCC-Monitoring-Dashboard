package com.nuxatech.cmcc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateServiceRequest {

    @NotBlank(message = "must not be blank")
    private String name;

    @NotBlank(message = "must not be blank")
    @Pattern(
        regexp = "^https?://.*",
        message = "must be a valid HTTP/HTTPS URL"
    )
    private String url;

    @NotBlank(message = "must not be blank")
    private String category;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
