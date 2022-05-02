public class TypeHolder {
    public String type = null;
    public boolean isArray = false;
    public TypeHolder(){}
    public TypeHolder(String type){
        this.type = type;
        this.isArray = false;
    }

    public TypeHolder(String type, boolean isArray){
        this.type = type;
        this.isArray = isArray;
    }
}
