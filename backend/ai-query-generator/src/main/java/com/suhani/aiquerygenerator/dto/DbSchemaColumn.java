package com.suhani.aiquerygenerator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DbSchemaColumn {
    private String name;
    private String type;
    private boolean primaryKey;
}

