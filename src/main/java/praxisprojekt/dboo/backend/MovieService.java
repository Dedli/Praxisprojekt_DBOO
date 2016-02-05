package praxisprojekt.dboo.backend;

import org.apache.commons.beanutils.BeanUtils;

import java.security.Key;
import java.sql.ResultSet;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Separate Java service class.
 * Backend implementation for the address book application, with "detached entities"
 * simulating real world DAO. Typically these something that the Java EE
 * or Spring backend services provide.
 */
// Backend service class. This is just a typical Java backend implementation
// class and nothing Vaadin specific.
public class MovieService {

    private static MovieService instance;
    private static PostgresRepository repo;

    public static MovieService createDemoService() {
        if (instance == null) {
            final MovieService movieService = new MovieService();
            repo = new PostgresRepository();
            HashMap hashMap = repo.standardSearch("movie");

            for (Object value : hashMap.values())
            {
                // save values to local hashmap
                movieService.save(((Movie) value));
            }
            instance = movieService;
        }
        return instance;
    }

    private HashMap<Integer, Movie> movies = new HashMap<>();

    public HashMap<Integer, Movie> getMovies() {
        return movies;
    }

    public synchronized List<Movie> findAll(String stringFilter) {
        ArrayList arrayList = new ArrayList();
        for (Movie movie : movies.values()) {
            try {
                boolean passesFilter = (stringFilter == null || stringFilter.isEmpty())
                        || movie.toString().toLowerCase()
                                .contains(stringFilter.toLowerCase());
                if (passesFilter) {
                    arrayList.add(movie.clone());
                }
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(MovieService.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
        //Collections.sort(arrayList, new Comparator<Movie>() {

         //   @Override
         //   public int compare(Movie o1, Movie o2) {
         //       return (int) (o2.getId() - o1.getId());
         //   }
       // });
        return arrayList;
    }

    public synchronized long count() {
        return movies.size();
    }

    public synchronized void delete(Movie value) {
        movies.remove(value.getId());
    }

    public synchronized void save(Movie entry) {
        //if (entry.getId() == null) {
        //    entry.setId(nextId++);
        //}
        try {
            entry = (Movie) BeanUtils.cloneBean(entry);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        movies.put(entry.getId(), entry);
    }

    public synchronized void saveToDb(Movie entry) {
        repo.insert(entry);
    }

    public String nf2_query(String functionality, String column, Movie movie1, Movie movie2) {
        System.out.println(functionality + movie1 + movie2);
        String resultString = "";
        resultString = repo.operatorQuery(movie1, movie2, tableColumnString(column), tableString(column), operatorString(functionality));
        return resultString;
    }

    public String tableString(String column){
        String table = "";
        if(column == "Genre"){
            table = "genre_nest_genre";
        } else if(column == "Schauspieler"){
            table = "actor_nest_actor";
        } else if(column == "Regisseur"){
            table = "director_nest_director";
        }
        return table;
    }

    public String tableColumnString(String column){
        String table = "";
        if(column == "Genre"){
            table = "genre";
        } else if(column == "Schauspieler"){
            table = "actor";
        } else if(column == "Regisseur"){
            table = "director";
        }
        return table;
    }

    public String operatorString(String functionality){
        String operator = "";
        if(functionality == "Union"){
            operator = "|";
        } else if(functionality == "Difference"){
            operator = "/";
        } else if(functionality == "Intersection"){
            operator = "&";
        } else if(functionality == "Subset"){
            operator = "<<";
        } else if(functionality == "Proper Subset"){
            operator = "<<=";
        } else if(functionality == "Equal"){
            operator = "==";
        } else if(functionality == "Not Equal"){
            operator = "!==";
        }
        return operator;
    }
}
