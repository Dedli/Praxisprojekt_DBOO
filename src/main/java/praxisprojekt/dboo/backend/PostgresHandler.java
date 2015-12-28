package praxisprojekt.dboo.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostgresHandler {

    private Logger lgr = Logger.getLogger(this.getClass().getName());
    Connection conn = null;

    PostgresHandler() {
        String url = "jdbc:postgresql://pgsql.hrz.tu-chemnitz.de";
        String user = "praxisprojekt_dboo_rw";
        String password = "ex9Oochide";
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized List<Movie> findAll(String stringFilter) {
        ArrayList arrayList = new ArrayList();


            //TODO get info from DB
        long startTime = System.nanoTime();
        try {
            Statement s = conn.createStatement();
            ResultSet r = s.executeQuery("SELECT * FROM movies;");
            while (r.next()) {
                int year = r.getInt(7);
                String title = r.getString(4);

                System.out.println("title: " + title + "year: " + year);
            }
            s.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        long estimatedTime = System.nanoTime() - startTime;
        final double seconds = ((double) estimatedTime / 1000000000);

        System.out.println("Query ended in " + new DecimalFormat("#.########").format(seconds) + " s.");

        // TODO change sort
        Collections.sort(arrayList, new Comparator<Movie>() {

            @Override
            public int compare(Movie o1, Movie o2) {
                return (int) (o2.getId() - o1.getId());
            }
        });
        return arrayList;
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
}
