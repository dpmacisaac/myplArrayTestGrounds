public class PathHolder {
    public Object first = null;
    public boolean isArray = false;

    public PathHolder (Object first){
        this.first = first;
    }
    public PathHolder(Object first, boolean isArray){
        this.first = first;
        this.isArray = isArray;
    }

    public void replace(Object first, boolean isArray){
        this.first = first;
        this.isArray = isArray;
    }
}
