// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.model;

public class Record {
    private int rid;
    private int mid;
    private Integer uid;
    private String arquivo;
    private String timestamp;

    public Record() {}

    public Record(int rid, int mid, Integer uid, String arquivo, String timestamp) {
        this.rid = rid;
        this.mid = mid;
        this.uid = uid;
        this.arquivo = arquivo;
        this.timestamp = timestamp;
    }

    public int getRid() { return rid; }
    public void setRid(int rid) { this.rid = rid; }

    public int getMid() { return mid; }
    public void setMid(int mid) { this.mid = mid; }

    public Integer getUid() { return uid; }
    public void setUid(Integer uid) { this.uid = uid; }

    public String getArquivo() { return arquivo; }
    public void setArquivo(String arquivo) { this.arquivo = arquivo; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
