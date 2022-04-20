public class PathHolder {
    public Object first = null;
    public TokenType second = null;

    public PathHolder(Object first, TokenType second){
        this.first = first;
        this.second = second;
    }

    public void replace(Object first, TokenType second){
        this.first = first;
        this.second = second;
    }
}
