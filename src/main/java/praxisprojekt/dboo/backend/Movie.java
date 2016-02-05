package praxisprojekt.dboo.backend;

import org.apache.commons.beanutils.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * contains all information about movie, mostly as strings
 */
public class Movie implements Serializable, Cloneable {

    private Integer mapper_id;

    private String filmname = "";
    private String jahr = "";
    private String regisseur = "";
    private String schauspieler = "";
    private String genre = "";

    public void setMovie(String titel, String jahr, Integer id, String director, String actor, String genre) {
        setFilmname(titel);
        setJahr(jahr);
        setId(id);
        setRegisseur(director);
        setSchauspieler(actor);
        setGenre(genre);
    }
    public Integer getId() {
        return mapper_id;
    }

    public void setId(Integer id) { mapper_id = id; }

    public String getFilmname() {
        return filmname;
    }

    public void setFilmname(String filmname) {
        this.filmname = filmname;
    }

    public String getRegisseur() {
        return regisseur;
    }

    public void setRegisseur(String regisseur) {
        this.regisseur = regisseur;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getJahr() {
        return jahr;
    }

    public void setJahr(String jahr) {
        this.jahr = jahr;
    }

    public String getSchauspieler() {
        return schauspieler;
    }

    public void setSchauspieler(String schauspieler) {
        this.schauspieler = schauspieler;
    }

    @Override
    public Movie clone() throws CloneNotSupportedException {
        try {
            return (Movie) BeanUtils.cloneBean(this);
        } catch (Exception ex) {
            throw new CloneNotSupportedException();
        }
    }

    @Override
    public String toString() {
        return filmname;
    }

}
