package com.pragma.webflux.api.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "images")
public class Image {

    @Id
    private String id;

    private String base64;

    public Image(String base64) {
        this.base64 = base64;
    }

    @Override
    public String toString() {
        return "Image{" +
                "id='" + id + '\'' +
                ", base64='" + base64 + '\'' +
                '}';
    }
}
