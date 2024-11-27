package com.itranswarp.exchange.db;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class AccessibleProperty {
    private final Field field;

    final Class<?> propertyType;

    final Function<Object,Object> javaToSqlMapper;

    final Function<Object,Object> sqlToJavaMapper;

    final String propertyName;

    final String columnDefinition;
    private AccessibleProperty gv;

    public Object get(Object bean) throws ReflectiveOperationException{
        Object obj = this.field.get(bean);
        if (this.javaToSqlMapper !=null){
            obj = this.javaToSqlMapper.apply(obj);
        }
        return obj;
    }

    public void set(Object bean,Object value) throws ReflectiveOperationException{
        if (this.sqlToJavaMapper !=null){
            value = this.sqlToJavaMapper.apply(value);
        }
        this.field.set(bean,value);
    }

    boolean isId(){return this.field.getAnnotation(Id.class) !=null;}

    boolean isIdentityId(){
        if (!isId()){
            return false;
        }
        GeneratedValue gv = this.field.getAnnotation(GeneratedValue.class);
        return gv !=null && gv.strategy() == GenerationType.IDENTITY;
    }

    boolean isUpdatable(){
        if (isId()){
            return false;
        }
        Column col = this.field.getAnnotation(Column.class);
        return col == null || col.updatable();
    }

    public AccessibleProperty(Field f){
        this.field = f;
        this.propertyType = f.getType();
        this.propertyName = f.getName();
        this.columnDefinition = getColumnDefinition(this.propertyType);
        boolean isEnum = f.getType().isEnum();
        this.javaToSqlMapper  = isEnum ? (obj) -> ((Enum<?>) obj).name() : null;
        this.sqlToJavaMapper = isEnum ? (obj) -> Enum.valueOf((Class<? extends Enum>) this.propertyType, (String) obj) : null;
    }

    private String getColumnDefinition(Class<?> type){
        Column col = this.field.getAnnotation(Column.class);
        if (col == null){
            throw  new IllegalArgumentException("@Column not found on: " + this.field.toString());
        }
        if (!col.name().isEmpty()){
            throw new IllegalArgumentException(
                    "@Column(name=\"" + col.name() + "\") is not supported: " + this.field.toString());
        }
        String colDef = null;
        if (col == null || col.columnDefinition().isEmpty()){
            if (type.isEnum()){
                colDef = "VARCHAR(32)";
            }else {
                colDef = getDefaultColumnType(type,col);
            }
        }else {
            colDef = col.columnDefinition().toUpperCase();
        }
        boolean nullable = col == null ? true : col.nullable();
        colDef = colDef + " " + (nullable ? "NULL" : "NOT NULL");

        if (isIdentityId()){
            colDef = colDef + " AUTO_INCREMENT";
        }
        if (!isId() && col !=null && col.unique()){
            colDef = colDef + " UNIQUE";
        }

        return colDef;
    }

    private static String getDefaultColumnType(Class<?> type,Column col){
        String ddl = DEFAULT_COLUMN_TYPE_MAP.get(type);
        if (ddl.equals("VARCHAR($1)")){
            ddl = ddl.replace("$1",String.valueOf(col == null ? 255 : col.length()));
        }
        if (ddl.equals("DECIMAL($1,$2)")){
            int preci = col == null ? 0 : col.precision();
            int scale = col == null ? 0 : col.scale();
            if (preci == 0){
                preci = 10;
            }
        }
        return ddl;
    }

    static final Map<Class<?>,String> DEFAULT_COLUMN_TYPE_MAP = new HashMap<>();
    static {
        DEFAULT_COLUMN_TYPE_MAP .put(String.class,"VARCHAR($1)");

        DEFAULT_COLUMN_TYPE_MAP.put(boolean.class,"BIT");
        DEFAULT_COLUMN_TYPE_MAP.put(Boolean.class,"BIT ");

        DEFAULT_COLUMN_TYPE_MAP.put(byte.class,"TINYINT");
        DEFAULT_COLUMN_TYPE_MAP.put(Byte.class,"TINYINT");
        DEFAULT_COLUMN_TYPE_MAP.put(short.class,"SMALLINT");
        DEFAULT_COLUMN_TYPE_MAP.put(Short.class,"SMALLINT");
        DEFAULT_COLUMN_TYPE_MAP.put(int.class,"INTEGER");
        DEFAULT_COLUMN_TYPE_MAP.put(Integer.class,"INTEGER");
        DEFAULT_COLUMN_TYPE_MAP.put(long.class,"BIGINT");
        DEFAULT_COLUMN_TYPE_MAP.put(Long.class,"BIGINT");
        DEFAULT_COLUMN_TYPE_MAP.put(float.class,"REAL");
        DEFAULT_COLUMN_TYPE_MAP.put(Float.class,"REAL");
        DEFAULT_COLUMN_TYPE_MAP.put(double.class,"DOUBLE");
        DEFAULT_COLUMN_TYPE_MAP.put(Double.class,"DOUBLE");

        DEFAULT_COLUMN_TYPE_MAP.put(BigDecimal.class,"DECIMAL($1,$2)");

    }
    @Override
    public String toString() {
        return "AccessibleProperty [propertyName=" + propertyName + ", propertyType=" + propertyType
                + ", columnDefinition=" + columnDefinition + "]";
    }
}
