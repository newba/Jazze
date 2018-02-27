package br.com.darksite.jazze.model;



public class Requests {
    private String nom_Utilisateur, user_Status, user_Thumb;

    public Requests() {
    }

    public Requests(String nom_Utilisateur, String user_Status, String user_Thumb) {
        this.nom_Utilisateur = nom_Utilisateur;
        this.user_Status = user_Status;
        this.user_Thumb = user_Thumb;
    }

    public String getNom_Utilisateur() {
        return nom_Utilisateur;
    }

    public void setNom_Utilisateur(String nom_Utilisateur) {
        this.nom_Utilisateur = nom_Utilisateur;
    }

    public String getUser_Status() {
        return user_Status;
    }

    public void setUser_Status(String user_Status) {
        this.user_Status = user_Status;
    }

    public String getUser_Thumb() {
        return user_Thumb;
    }

    public void setUser_Thumb(String user_Thumb) {
        this.user_Thumb = user_Thumb;
    }
}
