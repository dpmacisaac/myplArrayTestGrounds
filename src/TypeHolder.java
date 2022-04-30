public class TypeHolder {
    public Token type = null;
    public boolean isArray = false;
    public TypeHolder(){}
    public TypeHolder(Token type){
        this.type = type;
    }

    public TypeHolder(Token type, boolean isArray){
        this.type = type;
        this.isArray = isArray;
    }
}
