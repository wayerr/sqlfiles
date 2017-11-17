/*
 * Copyright (C) 2017 wayerr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package wayerr.co.sql.files;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
        
        public Field(String name, String type) {
            super(name, type);
        }

        @Override
        public String toString() {
            return "Field{" +
                    "name=" + name +
                    ", type=" + type +
                    '}';
        }
    }

    /**
     * Paramerter - the description of sql statement paraemeter. Usually has IN direction,
     * but may be other (for example in procedures).
     */
    public static class Param extends NamedChunk {
        private final Direction direction;

        public Param(String name, String type, Direction direction) {
            super(name, type);
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
        private List<Field> fields;
        private List<Param> params;
        private String query;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder query(String query) {
            this.query = query;
            return this;
        }

        public Builder addField(String name, String type) {
            return addField(new Field(name, type));
        }

        public Builder addField(Field field) {
            if(fields == null) {
                fields = new ArrayList<>();
            }
            fields.add(field);
            return this;
        }

        public Builder addParam(String name, String type, Direction direction) {
            return addParam(new Param(name, type, direction));
        }

        public Builder addParam(Param param) {
            if(params == null) {
                params = new ArrayList<>();
            }
            params.add(param);
            return this;
        }

        public SqlTemplate build() {
            return new SqlTemplate(name, query, copy(fields), copy(params));
        }

        private <T> List<T> copy(List<T> list) {
            if(list == null) {
                return Collections.emptyList();
            }
            return new ArrayList<>(list);
        }
    }

    private final String name;
    private final List<Field> fields;
    private final List<Param> params;
    private final String query;

    SqlTemplate(String name, String query, List<Field> fields, List<Param> params) {
        this.name = name;
        this.query = query;
        this.fields = immutable(fields);
        this.params = immutable(params);
    }

    public static Builder builder() {
        return new Builder();
    }

    private static <T> List<T> immutable(List<T> list) {
        // yes i known about 'guava'
        return Collections.unmodifiableList(new ArrayList<>(list));
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
                ", query=" + query +
                '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + Objects.hashCode(this.fields);
        hash = 53 * hash + Objects.hashCode(this.params);
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
        return true;
    }

    
}
