package com.example.monstrin;

public class Monster {
    private String id;
    private String nome;
    private String ota_value;
    private String yra_value;
    private String image_base64;

    public Monster() {
        // Required for Firestore
    }

    public Monster(String id, String nome, String ota_value, String yra_value, String image_base64) {
        this.id = id;
        this.nome = nome;
        this.ota_value = ota_value;
        this.yra_value = yra_value;
        this.image_base64 = image_base64;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getOta_value() { return ota_value; }
    public void setOta_value(String ota_value) { this.ota_value = ota_value; }

    public String getYra_value() { return yra_value; }
    public void setYra_value(String yra_value) { this.yra_value = yra_value; }

    public String getImage_base64() { return image_base64; }
    public void setImage_base64(String image_base64) { this.image_base64 = image_base64; }
}
