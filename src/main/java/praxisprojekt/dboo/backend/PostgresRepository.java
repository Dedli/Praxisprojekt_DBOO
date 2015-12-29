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

public class PostgresRepository {

    private Logger lgr = Logger.getLogger(this.getClass().getName());
    Connection conn = null;
    private long nextId = 0;

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

    public void insert(String table, String values) {
        // TODO
    }

    public void update(String table, String values) {
        // TODO
    }

    public void delete(String table, String values) {
        // TODO
    }

    public HashMap standardSearch(String table){
        long startTime = System.nanoTime();
        HashMap<Long, Movie> queriedMovies = new HashMap<>();
        try {
            Statement s = conn.createStatement();
            ResultSet r = s.executeQuery("SELECT id, title, year FROM " + table + ";");
            while (r.next()) {
                Long id = (long) r.getInt(1);
                String title = r.getString(2);
                int year = r.getInt(3);
                Movie thisMovie = new Movie();
                // set movie values
                thisMovie.setFilmname(title);
                thisMovie.setSchauspieler("Michael Jackson " + "Brad Pitt");
                thisMovie.setRegisseur("Stanley Kubrick");
                thisMovie.setJahr("" + year);

                queriedMovies.put(id, thisMovie);
                nextId++;
            }
            s.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long estimatedTime = System.nanoTime() - startTime;
        final double seconds = ((double) estimatedTime / 1000000000);

        System.out.println("Query ended in " + new DecimalFormat("#.########").format(seconds) + " s.");
        return queriedMovies;
    }
}
