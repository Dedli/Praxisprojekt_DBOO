package praxisprojekt.dboo.backend;

import org.apache.commons.beanutils.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * A simple DTO for the address book example.
 *
 * Serializable and cloneable Java Object that are typically persisted
 * in the database and can also be easily converted to different formats like JSON.
 */
public class Movie implements Serializable, Cloneable {

    private Long id;

    private String filmname = "";
    private String jahr = "";
    private String regisseur = "";
    private String schauspieler = "";
    private Date birthDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
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
        return "Movie{" + /*"id=" + id +*/ "filmname=" + filmname
                + ", regisseur=" + regisseur + ", jahr=" + jahr + ", schauspieler="
                + schauspieler + ", birthDate=" + birthDate + '}';
    }

}
