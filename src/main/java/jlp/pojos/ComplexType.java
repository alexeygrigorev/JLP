package jlp.pojos;

public class ComplexType {

    private String fullName;
    private boolean parametrized = false;
    private String parameters;
    private boolean array = false;
    private String arrayType;

    public ComplexType() {
    }

    public ComplexType(String fullName) {
        this.fullName = fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setParametrized(boolean parametrized) {
        this.parametrized = parametrized;
    }

    public boolean isParametrized() {
        return parametrized;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getParameters() {
        return parameters;
    }

    public String getArrayType() {
        return arrayType;
    }

    public void setArrayType(String arrayType) {
        this.arrayType = arrayType;
    }

    public boolean isArray() {
        return array;
    }

    public void setArray(boolean array) {
        this.array = array;
    }

    @Override
    public String toString() {
        if (array) {
            return fullName + arrayType;
        }
        if (parametrized) {
            return fullName + parameters;
        }
        return fullName;
    }

}
