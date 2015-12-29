package praxisprojekt.dboo.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Logger;

public class PostgresRepository {

    private Logger lgr = Logger.getLogger(this.getClass().getName());
    Connection conn = null;

    void PostgresHandler() {
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

    public void insert(String table, String values) {
        // TODO
    }

    public void update(String table, String values) {
        // TODO
    }

    public void delete(String table, String values) {
        // TODO
    }

    public void search(String table, String searchTerm, String searchColumn){
       String sqlString = "SELECT title, year from movie;";
    }
}
