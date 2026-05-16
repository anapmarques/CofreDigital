package br.com.cofredigital.model;

public class Message {
    private int mid;
    private String conteudo;

    public Message() {}

    public Message(int mid, String conteudo) {
        this.mid = mid;
        this.conteudo = conteudo;
    }

    public int getMid() { return mid; }
    public void setMid(int mid) { this.mid = mid; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }
}
