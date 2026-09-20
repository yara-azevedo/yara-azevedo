package com.example.mylist.model;

import java.util.List;

public class Conteudo {
    private String id;
    private String uid; // ID do usuário do Firebase
    private String posterUrl;
    private String tipo;
    private String titulo;
    private String saga;
    private String volume;
    private String comentario;
    private String dataInicio;
    private String dataConclusao;
    private boolean concluido;
    private boolean largado;
    private String formato;
    private float nota;
    private List<String> frases;

    public Conteudo() {
        // Necessário para o Firestore
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getSaga() { return saga; }
    public void setSaga(String saga) { this.saga = saga; }

    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getDataInicio() { return dataInicio; }
    public void setDataInicio(String dataInicio) { this.dataInicio = dataInicio; }

    public String getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(String dataConclusao) { this.dataConclusao = dataConclusao; }

    public boolean isConcluido() { return concluido; }
    public void setConcluido(boolean concluido) { this.concluido = concluido; }

    public boolean isLargado() { return largado; }
    public void setLargado(boolean largado) { this.largado = largado; }

    public String getFormato() { return formato; }
    public void setFormato(String formato) { this.formato = formato; }

    public float getNota() { return nota; }
    public void setNota(float nota) { this.nota = nota; }

    public List<String> getFrases() { return frases; }
    public void setFrases(List<String> frases) { this.frases = frases; }
}
