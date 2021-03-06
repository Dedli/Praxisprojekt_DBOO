package praxisprojekt.dboo.backend;

import praxisprojekt.dboo.MovieUI;

import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.logging.Logger;
/** Das Repository stellt die Verbindung zur Datenbank her und führt alle Anfragen aus **/
public class PostgresRepository {

    private Logger lgr = Logger.getLogger(this.getClass().getName());
    Connection conn = null;

    public PostgresRepository() {
        String url = "jdbc:postgresql://pgsql.hrz.tu-chemnitz.de/praxisprojekt_dboo";
        String user = "praxisprojekt_dboo_rw";
        String password = "ex9Oochide";
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insert(Movie entry) {
        System.out.println(entry);
        if(entry.getJahr() == ""){
            entry.setJahr("0");
        }
        String sqlString = "SELECT insertMovie('"+entry.getFilmname()+"',"+entry.getJahr()+",ARRAY['"+formatString(entry.getSchauspieler())+"']::character varying[],ARRAY['"+formatString(entry.getRegisseur())+"']::character varying[],ARRAY['"+formatString(entry.getGenre())+"']::character varying[]);";
        System.out.println(sqlString);
        long startTime = System.nanoTime();
        String result = "";
        try {
            Statement s = conn.createStatement();
            boolean r = s.execute(sqlString);
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long estimatedTime = System.nanoTime() - startTime;
        final double seconds = ((double) estimatedTime / 1000000000);

        System.out.println("Query ended in " + new DecimalFormat("#.########").format(seconds) + " s.");
    }

    public void update(Movie entry) {
        System.out.println(entry);
        String sqlString = "SELECT updateMovie("+entry.getId()+",'"+entry.getFilmname()+"',"+entry.getJahr()+",ARRAY['"+ formatString(entry.getSchauspieler())+"']::character varying[],ARRAY['"+formatString(entry.getRegisseur())+"']::character varying[],ARRAY['"+formatString(entry.getGenre())+"']::character varying[]);";
        System.out.println(sqlString);
        long startTime = System.nanoTime();
        String result = "";
        try {
            Statement s = conn.createStatement();
            boolean r = s.execute(sqlString);
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long estimatedTime = System.nanoTime() - startTime;
        final double seconds = ((double) estimatedTime / 1000000000);

        System.out.println("Query ended in " + new DecimalFormat("#.########").format(seconds) + " s.");
    }

    public void delete(Movie entry) {
        System.out.println(entry);
        String sqlString = "SELECT deleteMovie("+entry.getId()+");";
        System.out.println(sqlString);
        long startTime = System.nanoTime();
        String result = "";
        try {
            Statement s = conn.createStatement();
            boolean r = s.execute(sqlString);
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long estimatedTime = System.nanoTime() - startTime;
        final double seconds = ((double) estimatedTime / 1000000000);

        System.out.println("Query ended in " + new DecimalFormat("#.########").format(seconds) + " s.");
    }

    // runs query with the given operator parameter and the movie values
    public String operatorQuery(Movie movie1, Movie movie2, String column, String table, String operator){
        System.out.println("movie1: " + movie1 + " movie2: " + movie2);
        long startTime = System.nanoTime();
        String result = "";
        try {
            Statement s = conn.createStatement();
            String sqlString = "SELECT (SELECT "+column+" FROM "+table+" WHERE mapper_id=" + movie1.getId() + ") "+operator+" (SELECT "+column+" FROM "+table+" WHERE mapper_id=" + movie2.getId() + ");";
            ResultSet r = s.executeQuery(sqlString);
            System.out.println(sqlString);
            while (r.next()) {
                result = r.getString(1);
            }
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long estimatedTime = System.nanoTime() - startTime;
        final double seconds = ((double) estimatedTime / 1000000000);

        System.out.println("Query ended in " + new DecimalFormat("#.########").format(seconds) + " s.");
        return result;
    }

    // returns everything in the database
    public HashMap standardSearch(){
        long startTime = System.nanoTime();
        HashMap<Integer, Movie> queriedMovies = new HashMap<>();
        try {
            Statement s = conn.createStatement();
            //ResultSet r = s.executeQuery("SELECT title, year, mapper_id FROM " + table + ";");
            ResultSet r = s.executeQuery("SELECT movie.title, movie.year, movie.mapper_id, director, actor, genre FROM movie JOIN director_nest_director ON director_nest_director.mapper_id= movie.mapper_id JOIN actor_nest_actor ON actor_nest_actor.mapper_id= movie.mapper_id JOIN genre_nest_genre ON genre_nest_genre.mapper_id= movie.mapper_id;");
            while (r.next()) {
                System.out.println("--NEW MOVIE--");
                String title = r.getString(1);
                int year = r.getInt(2);
                int mapper_id = r.getInt(3);
                String director = normalizeString(r.getString(4));
                String actor = normalizeString(r.getString(5));
                String genre = normalizeGenreString(r.getString(6));
                // create and set movie values
                Movie thisMovie = new Movie();
                thisMovie.setMovie(title, "" + year, new Integer(mapper_id), director, actor, genre);

                queriedMovies.put(mapper_id, thisMovie);
            }
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long estimatedTime = System.nanoTime() - startTime;
        final double seconds = ((double) estimatedTime / 1000000000);

        System.out.println("Query ended in " + new DecimalFormat("#.########").format(seconds) + " s.");
        return queriedMovies;
    }

    // brings the nf2-formated strings to a more readable format
    public String normalizeString(String enumeration) {
        String result = "";
        String separatedBy = ",";
        System.out.println("INPUT: " + enumeration);
        String[] splittedValues = enumeration.split(separatedBy);
        int i = 0;
        while(i < splittedValues.length) {
            System.out.println(splittedValues[i]);
            String c = "";
            if(i == 0) {
                // first value
                c = splittedValues[i].substring(2,splittedValues[i].length()-1);
                // if the first is the last value
                if((i+1) == splittedValues.length) {
                    c = c.substring(0,c.length()-1);
                }
            } else {
                // if not the first value
                c = splittedValues[i].substring(1, splittedValues[i].length() - 1);
                if ((i+1) == splittedValues.length) {
                    // if the value is the last value
                    c = c.substring(0, c.length() - 1);
                }
                // insert comma
                result = result + ", ";
            }
            result = result + c;
            System.out.println(c);
            i++;
        }
        System.out.println("RESULTAT: " + result);
        return result;
    }

    // brings the nf2-formated strings to a more readable format
    public String normalizeGenreString(String enumeration) {
        String result = "";
        String separatedBy = ",";
        System.out.println("INPUT: " + enumeration);
        String[] splittedValues = enumeration.split(separatedBy);
        int i = 0;
        while(i < splittedValues.length) {
            System.out.println(splittedValues[i]);
            String c = "";
            if(i == 0) {
                // first value
                c = splittedValues[i].substring(1,splittedValues[i].length());
                // if the first is the last value
                if((i+1) == splittedValues.length) {
                    c = c.substring(0,c.length()-1);
                }
            } else if ((i+1) == splittedValues.length) {
                // if the value is the last value
                c = splittedValues[i].substring(0, splittedValues[i].length() - 1);
                // insert comma
                result = result + ", ";
            } else {
                // if the value is not the first value
                c = splittedValues[i];
                // insert comma
                result = result + ", ";
            }
            result = result + c;
            i++;
        }
        System.out.println("RESULTAT: " + result);
        return result;
    }

    public String formatString(String enumeration) {
        String result = "";
        String separatedBy = ",";
        System.out.println("INPUT: " + enumeration);
        String[] splittedValues = enumeration.split(separatedBy);
        int i = 0;
        while(i < splittedValues.length) {
            String c = "";
            if(i == 0) {
                c = splittedValues[i];
            } else {
                // if not first value
                // insert comma
                result = result + "','";
                c = splittedValues[i];
                c = c.trim();
            }
            result = result + c;
            i++;
        }
        System.out.println("RESULTAT: " + result);
        return result;
    }
}
