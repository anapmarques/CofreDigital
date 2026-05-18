// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.model;

public class Group {
    private int gid;
    private String nome;

    public Group() {}

    public Group(int gid, String nome) {
        this.gid = gid;
        this.nome = nome;
    }

    public int getGid() { return gid; }
    public void setGid(int gid) { this.gid = gid; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
