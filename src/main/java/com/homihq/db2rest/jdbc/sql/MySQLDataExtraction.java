package com.homihq.db2rest.jdbc.sql;

import com.homihq.db2rest.core.model.DbColumn;
import com.homihq.db2rest.core.model.DbTable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class MySQLDataExtraction implements MetaDataExtraction {

   List<String> excludedCatalogs = Arrays.asList("mysql", "sys", "information_schema","performance_schema");

    @Override
    public boolean canHandle(String database) {
        return StringUtils.equalsIgnoreCase(database, "MySQL");
    }


    @Override
    public List<DbTable> getTables(DatabaseMetaData databaseMetaData, boolean includeAllCatalogs,
                                   List<String> includeCatalogs) {
        try {
            List<String> includedCatalogs = includeCatalogs;

            if(includeAllCatalogs) includedCatalogs = getAllCatalogs(databaseMetaData, excludedCatalogs);

            List<DbTable> dbTables = new ArrayList<>();

            for(String catalog : includedCatalogs) {

                log.info("Loading meta tables for catalog - {}", catalog);

                List<MetaDataTable> metaDataTables =
                        getMetaTables(databaseMetaData, catalog, null);

                List<DbTable> tables =
                        metaDataTables.parallelStream()
                                .map(metaDataTable -> getDbTable(
                                        databaseMetaData,
                                        metaDataTable

                                ))
                                .toList();

                dbTables.addAll(tables);
            }


            return dbTables;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<DbColumn> getAllColumns(DatabaseMetaData databaseMetaData, String catalog, String schema,
                                         String tableName, String tableAlias) throws SQLException {

        List<String> pkColumns = getAllPrimaryKeys(databaseMetaData, catalog, schema, tableName);

        List<DbColumn> dbColumns = new ArrayList<>();

        try(ResultSet columns = databaseMetaData
                .getColumns(catalog,schema, tableName, null)){
            while(columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                int datatype = columns.getInt("DATA_TYPE");
                String isAutoIncrement = columns.getString("IS_AUTOINCREMENT");
                String isGenerated = "N";
                String typeName = columns.getString("TYPE_NAME");

                Class<?> javaType = JdbcTypeJavaClassMappings.INSTANCE.determineJavaClassForJdbcTypeCode(datatype);

                DbColumn dbColumn =
                        new DbColumn(tableName, columnName,
                                "",
                                tableAlias,
                                pkColumns.contains(columnName),
                                typeName,
                                StringUtils.equalsAnyIgnoreCase(isGenerated,"YES"),
                                StringUtils.equalsAnyIgnoreCase(isAutoIncrement,"YES"),
                                javaType
                        );

                dbColumns.add(dbColumn);
            }
        }

        return dbColumns;

    }

    private DbTable getDbTable(DatabaseMetaData databaseMetaData, MetaDataTable metaDataTable) {
        try {
            List<DbColumn> columns = getAllColumns(databaseMetaData,
                                            metaDataTable.catalog(),
                                            metaDataTable.schema(),
                                            metaDataTable.tableName(),
                                            metaDataTable.tableAlias());

            String schemaName = StringUtils.isNotBlank(metaDataTable.schema()) ?
                    metaDataTable.schema() : metaDataTable.catalog();

            return new DbTable(
                    schemaName, metaDataTable.tableName() ,schemaName + "." + metaDataTable.tableName(),
                    metaDataTable.tableAlias(),columns, metaDataTable.tableType());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }



}
