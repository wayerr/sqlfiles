/*
 *    Copyright 2017 wayerr
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */
package wayerr.co.sql.files;

import java.util.*;

/**
 * Contains sql template with descripotion of fileds and parameters.
 * Fields is optional. Note that parameter may appear many times with
 * same name but dufferent type & etc.
 * @author wayerr
 */
public final class SqlTemplate {

    public enum Direction {
        IN(true, false), OUT(false, true), INOUT(true, true);

        public static Direction from(String val) {
            if(val == null) {
                return null;
            }
            //val.toUpperCase() will copy string that may
            //  be redundant in some cases
            switch(val) {
                case "in":
                case "IN":
                    return IN;
                case "out":
                case "OUT":
                    return OUT;
                case "inout":
                case "INOUT":
                    return INOUT;
            }
            return null;
        }

        private final boolean out;
        private final boolean in;

        Direction(boolean in, boolean out) {
            this.in = in;
            this.out = out;
        }

        public boolean isIn() {
            return in;
        }

        public boolean isOut() {
            return out;
        }
    }

    /**
     * Field - the description of result set column. Not affect on query.
     */
    public static class Field extends NamedChunk {
        
        public Field(String name, String type, Map<String, String> attributes) {
            super(name, type, attributes);
        }

        @Override
        public String toString() {
            return "Field{" +
                    "name=" + name +
                    ", type=" + type +
                    ", attributes=" + getAttributes() +
                    '}';
        }
    }

    /**
     * Paramerter - the description of sql statement paraemeter. Usually has IN direction,
     * but may be other (for example in procedures).
     */
    public static class Param extends NamedChunk {
        private final Direction direction;

        public Param(String name, String type, Map<String, String> attributes, Direction direction) {
            super(name, type, attributes);
            this.direction = direction == null? Direction.IN : direction;
        }

        /**
         * Direction of parameter.
         * @see Direction
         * @return direction, newer return null
         */
        public Direction getDirection() {
            return direction;
        }

        @Override
        public String toString() {
            return "Param{" +
                    "name=" + name +
                    ", type=" + type +
                    ", direction=" + direction +
                    ", attributes=" + getAttributes() +
                    '}';
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 37 * hash + Objects.hashCode(this.direction);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj) {
                return true;
            }
            if(obj == null) {
                return false;
            }
            if(getClass() != obj.getClass()) {
                return false;
            }
            if(!super.equals(obj)) {
                return false;
            }
            final Param other = (Param)obj;
            if(this.direction != other.direction) {
                return false;
            }
            return true;
        }


    }

    public static class Builder {
        private String name;
        private final List<Field> fields = new ArrayList<>();
        private final List<Param> params = new ArrayList<>();
        private final Map<String, String> attributes = new HashMap<>();
        private String query;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public Builder query(String query) {
            this.query = query;
            return this;
        }

        public List<Field> getFields() {
            return fields;
        }

        public Builder addField(Field field) {
            fields.add(field);
            return this;
        }

        public List<Param> getParams() {
            return params;
        }

        public Builder addParam(Param param) {
            params.add(param);
            return this;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public Builder putAttribute(String key, String value) {
            attributes.put(key, value);
            return this;
        }

        public void clear() {
            this.name = null;
            this.query = null;
            this.fields.clear();
            this.params.clear();
            this.attributes.clear();
        }

        public SqlTemplate build() {
            return new SqlTemplate(name, query, fields, params, attributes);
        }
    }

    private final String name;
    private final List<Field> fields;
    private final List<Param> params;
    private final String query;
    private final Map<String, String> attributes;

    SqlTemplate(String name, String query, List<Field> fields, List<Param> params, Map<String, String> attributes) {
        this.name = name;
        this.query = query;
        this.fields = Utils.immutableCopy(fields);
        this.params = Utils.immutableCopy(params);
        this.attributes = Utils.immutableCopy(attributes);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    /**
     * Unmodifiable list of fields.
     * @return unmodifiable list of fields
     */
    public List<Field> getFields() {
        return fields;
    }

    /**
     * Unmodifiable list of parameters.
     * @return unmodifiable list of parameters
     */
    public List<Param> getParams() {
        return params;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return "SqlTemplate{" +
                "name=" + name +
                ", fields=" + fields +
                ", params=" + params +
                ", attributes=" + attributes +
                ", query=" + query +
                '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + Objects.hashCode(this.fields);
        hash = 53 * hash + Objects.hashCode(this.params);
        hash = 53 * hash + Objects.hashCode(this.attributes);
        hash = 53 * hash + Objects.hashCode(this.query);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final SqlTemplate other = (SqlTemplate)obj;
        if(!Objects.equals(this.name, other.name)) {
            return false;
        }
        if(!Objects.equals(this.query, other.query)) {
            return false;
        }
        if(!Objects.equals(this.fields, other.fields)) {
            return false;
        }
        if(!Objects.equals(this.params, other.params)) {
            return false;
        }
        if(!Objects.equals(this.attributes, other.attributes)) {
            return false;
        }
        return true;
    }

    
}
