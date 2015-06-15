package jlp.pojos;

import java.util.List;

public class ClassIdentifiers {

    private String packageName;
    private String className;
    private List<Identifier> ids;

    public ClassIdentifiers() {
    }

    public ClassIdentifiers(String packageName, String className, List<Identifier> ids) {
        this.packageName = packageName;
        this.className = className;
        this.ids = ids;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<Identifier> getIds() {
        return ids;
    }

    public void setIds(List<Identifier> ids) {
        this.ids = ids;
    }

    @Override
    public String toString() {
        return "ClassIdentifiers [packageName=" + packageName + ", className=" + className + ", ids=" + ids
                + "]";
    }

}
