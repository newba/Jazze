package br.com.darksite.jazze.model;



public class Messages {

    private String message, type, from;
    private long horaire;
    private boolean vu;


    public Messages() {
    }

    public Messages(String message, String type, String from, long horaire, boolean vu) {
        this.message = message;
        this.type = type;
        this.from = from;
        this.horaire = horaire;
        this.vu = vu;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public long getHoraire() {
        return horaire;
    }

    public void setHoraire(long horaire) {
        this.horaire = horaire;
    }

    public boolean isVu() {
        return vu;
    }

    public void setVu(boolean vu) {
        this.vu = vu;
    }
}
