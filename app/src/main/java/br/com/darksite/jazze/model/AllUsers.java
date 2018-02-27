package br.com.darksite.jazze.model;



public class AllUsers {

    public String nom_Utilisateur;
    public String user_Image;
    public String user_Status;
    public String user_Thumb;


    public AllUsers() {
    }

    public AllUsers(String nom_Utilisateur, String user_Image, String user_Status, String user_Thumb) {
        this.nom_Utilisateur = nom_Utilisateur;
        this.user_Image = user_Image;
        this.user_Status = user_Status;
        this.user_Thumb = user_Thumb;
    }

    public String getNom_Utilisateur() {
        return nom_Utilisateur;
    }

    public void setNom_Utilisateur(String nom_Utilisateur) {
        this.nom_Utilisateur = nom_Utilisateur;
    }

    public String getUser_Image() {
        return user_Image;
    }

    public void setUser_Image(String user_Image) {
        this.user_Image = user_Image;
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
