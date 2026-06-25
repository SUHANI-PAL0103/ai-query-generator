package com.suhani.aiquerygenerator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DbSchemaTable {
    private String name;
    private List<DbSchemaColumn> columns;
    private List<DbSchemaForeignKey> foreignKeys;
}

