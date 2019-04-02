package org.redhat.bpm.wid;

import java.io.Serializable;
import java.util.Objects;

public class VariableHolder implements Serializable {

    private String name;
    private String value;

    public VariableHolder(){}

    public VariableHolder(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        VariableHolder variableHolder = (VariableHolder) o;

        // field comparison
        return Objects.equals(name, variableHolder.getName())
                && Objects.equals(value, variableHolder.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }


}
