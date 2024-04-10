package com.homihq.db2rest.jdbc.sql;


import com.homihq.db2rest.core.model.DbTable;
import org.apache.commons.lang3.StringUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.homihq.db2rest.schema.AliasGenerator.getAlias;

public interface MetaDataExtraction {
    boolean canHandle(String database);


    List<DbTable> getTables(DatabaseMetaData databaseMetaData, boolean includeAllSchemas, List<String> includedSchemas);

    default boolean include(String schemaOrCatalog, List<String> excludedSchemasOrCatalogs) {
        return excludedSchemasOrCatalogs.stream().noneMatch(i -> StringUtils.equalsIgnoreCase(schemaOrCatalog, i));
    }

    default List<String> getAllCatalogs(DatabaseMetaData databaseMetaData, List<String> excludedCatalogs) throws Exception{
        List<String> includedCatalogs = new ArrayList<>();
        try(ResultSet resultSet = databaseMetaData.getCatalogs()){

            while(resultSet.next()) {

                String catalog = resultSet.getString("TABLE_CAT");

                if(include(catalog, excludedCatalogs)) {
                    includedCatalogs.add(catalog);
                }

            }
        }

        return includedCatalogs;
    }

    default List<String> getAllSchemas(DatabaseMetaData databaseMetaData, List<String> excludedSchemas) throws Exception{
        List<String> includedSchemas = new ArrayList<>();
        try(ResultSet resultSet = databaseMetaData.getSchemas()){

            while(resultSet.next()) {
                String schema = resultSet.getString("TABLE_SCHEM");

                if(include(schema, excludedSchemas)) {
                    includedSchemas.add(schema);
                }

            }
        }

        return includedSchemas;
    }


    default List<MetaDataTable>
        getMetaTables(DatabaseMetaData databaseMetaData, String catalogPattern, String schemaPattern) throws SQLException {

        List<MetaDataTable> tables = new ArrayList<>();

        try(ResultSet resultSet = databaseMetaData.getTables(
                catalogPattern,
                schemaPattern,
                null,
                new String[]{"TABLE", "VIEW"})){
            while(resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                String catalog = resultSet.getString("TABLE_CAT");
                String schema = resultSet.getString("TABLE_SCHEM");
                String tableType = resultSet.getString("TABLE_TYPE");
                String tableAlias = getAlias(tableName);
                MetaDataTable metaDataTable = new MetaDataTable(tableName, catalog, schema, tableType, tableAlias);

                tables.add(metaDataTable);

            }
        }
        return tables;
    }

    default List<String> getAllPrimaryKeys(DatabaseMetaData databaseMetaData, String catalog, String schema, String tableName) throws SQLException {
        List<String> pkColumns = new ArrayList<>();

        try(ResultSet primaryKeys = databaseMetaData.getPrimaryKeys(catalog, schema, tableName)){
            while(primaryKeys.next()){
                String primaryKeyColumnName = primaryKeys.getString("COLUMN_NAME");

                pkColumns.add(primaryKeyColumnName);
            }
        }
        return pkColumns;
    }
}
