// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.model;

public class KeyStoreEntry {
    private int kid;
    private Integer uid;
    private String certificadoDigital;
    private byte[] chavePrivada;

    public KeyStoreEntry() {}

    public KeyStoreEntry(int kid, Integer uid, String certificadoDigital, byte[] chavePrivada) {
        this.kid = kid;
        this.uid = uid;
        this.certificadoDigital = certificadoDigital;
        this.chavePrivada = chavePrivada;
    }

    public int getKid() { return kid; }
    public void setKid(int kid) { this.kid = kid; }

    public Integer getUid() { return uid; }
    public void setUid(Integer uid) { this.uid = uid; }

    public String getCertificadoDigital() { return certificadoDigital; }
    public void setCertificadoDigital(String certificadoDigital) { this.certificadoDigital = certificadoDigital; }

    public byte[] getChavePrivada() { return chavePrivada; }
    public void setChavePrivada(byte[] chavePrivada) { this.chavePrivada = chavePrivada; }
}
